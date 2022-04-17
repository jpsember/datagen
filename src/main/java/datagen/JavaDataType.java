package datagen;

import static js.base.Tools.*;

public class JavaDataType extends DataType {


  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    if (f.optional() || isPrimitive()) {
      s.a(targetExpr, " = ", //
          sourceExpressionToMutable("x"));
    } else {
      s.a(targetExpr, " = ", //
          sourceExpressionToMutable("(x == null) ? " + f.defaultValueOrNull() + " : x"));
    }
    s.a(";");
  }
  

  /**
   * Generate source code to deserialize a JSON value to a value to be stored in
   * a map
   * 
   * @param jsonValue
   *          representation of a JSMap value (from a key/value pair), or a
   *          JSList value
   */
  public String deserializeJsonToMapValue(String jsonValue) {
    if (isPrimitive())
      return jsonValue;
    throw notSupported("deserializeJsonToJavaValue for dataType:" + getClass());
  }

}
