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

    // This is an example of what we generate for a wrapped type = contract:
    //
    //    {
    //      let x = new_list();
    //      for v in &self.galaxy {
    //        x.push(v.to_json());
    //      }
    //      m.put(KEY_GALAXY, x);
    //    }

    //  m.put(KEY_GALAXY, encode_data_list_as_json(&self.galaxy, &encode_Saturn));

    s.a("m.put(", f.nameStringConstantQualified(), ", ");
    wrappedType().generateSerializeListOf(s, f);
    s.a(");",CR);

    //    s.comment("This could be a utility function");
    //      
    //    s.a(OPEN, //
    //        "let x = new_list();", CR, //
    //        "for v in &self.", f.instanceName(), OPEN, //
    //        "x.push(", wrappedType().buildRustJsonValueFrom("v"), ");", CLOSE, //
    //        "m.put(", f.nameStringConstantQualified(), ", x);", //
    //        CLOSE, CR);
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
