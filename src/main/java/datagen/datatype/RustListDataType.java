package datagen.datatype;

import static js.base.Tools.*;
import static datagen.SourceBuilder.*;

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
    s.a(OPEN, //
        "let j = m.opt(", f.nameStringConstantQualified(), ");", CR);
    s.a("n.", f.instanceName(), " = ");
    wrappedType().sourceDeserializeFromList(s, f);
    s.a(";", CLOSE);
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.a("m.put(", f.nameStringConstantQualified(), ", ");
    wrappedType().generateSerializeListOf(s, f);
    s.a(");",CR);
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
    s.a(targetExpr, " = ", argName, ".to_vec();");
  }

  @Override
  public String setterArgSignature(String expr) {
    return "&[" + wrappedType().typeName() + "]";
  }

  @Override
  public String getInitInstanceFieldExpr(FieldDef f) {
    return "Vec::new()";
  }

  public String setterArgUsage(String expr) {
    checkArgument(!expr.contains("Vec::"));
    return expr + ".to_vec()";
  }

  @Override
  public String getterReturnTypeExpr() {
    return "&[" + wrappedType().typeName() + "]";
  }

  @Override
  public void getterBody(SourceBuilder s, FieldDef f) {
    s.a("&self.", f.instanceName());
  }

  private final DataType mWrappedType;
}
