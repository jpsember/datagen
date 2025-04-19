package datagen.datatype;

import static js.base.Tools.*;
import static datagen.SourceBuilder.*;
import static datagen.Utils.*;

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.RustDataType;
import datagen.SourceBuilder;
import js.json.JSList;
import js.json.JSMap;
import js.json.JSUtils;

public class RustListDataType extends RustDataType {

  public RustListDataType(DataType wrappedType) {
    mWrappedType = wrappedType;
    with("Vec<" + wrappedType.qualifiedName().className() + ">");
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public String provideSourceDefaultValue() {
    return "Vec::new()"/* + wrappedType().typeName() + ">" */ + comment("");
  }

  public DataType wrappedType() {
    return mWrappedType;
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    wrappedType().sourceDeserializeFromList(s, f);
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.comment("This could be a utility function");
    var exprs = wrappedType().buildSerializeFromListVariable("v");
    s.a(OPEN, //
        "let x = new_list();", CR, "for ", exprs.first, " in self.", f.instanceName(), OPEN, //
        "x.push(", exprs.second, ");", CLOSE, //
        "m.put(", f.nameStringConstantQualified(), ", x);", //
        CLOSE, CR);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {

    JSList parsedExpressions = json.getList("");

    SourceBuilder sb = classSpecificSource;
    sb.a("var ", fieldDef.constantName(), " = ", typeName(), "{");

    for (int index = 0; index < parsedExpressions.size(); index++) {
      Object expr = parsedExpressions.getUnsafe(index);
      if (index > 0)
        sb.a(", ");
      sb.a(JSUtils.valueToString(expr));
    }
    sb.a("}", CR);

    return fieldDef.constantName();
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    String argName = f.instanceName();
    s.a(targetExpr, " = ", argName, ".to_vec();", comment());
  }

  @Override
  public String setterArgSignature(String expr) {
    return "&[" + wrappedType().typeName() + "]" + comment();
  }

  @Override
  public String getInitInstanceFieldExpr(FieldDef f) {
    return "Vec::new()" + comment();
  }

  public String setterArgUsage(String expr) {
    checkArgument(!expr.contains("Vec::"));
    return expr + ".to_vec()" + comment("");
  }

  @Override
  public String getterReturnTypeExpr() {
    return "&[" + wrappedType().typeName() + "]" + comment("");
  }

  @Override
  public void getterBody(SourceBuilder s, FieldDef f) {
    s.a("&self.", f.instanceName(), comment(""));
  }

  private final DataType mWrappedType;
}
