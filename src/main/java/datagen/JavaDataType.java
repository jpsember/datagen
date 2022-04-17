package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import js.data.DataUtil;

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
   * Generate source code for deserializing a value from a JSMap
   */
  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("m", f.sourceName(), " = m.opt(", f.nameStringConstantQualified(), ", ", f.defaultValueOrNull(), ");");
  }

  /**
   * Generate source code to continue the calculation of a value for hashCode().
   *
   * Default implementation assumes value is a non-null Object reference, and
   * calls its hashCode() method
   */
  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a("r = r * 37 + m", f.sourceName(), ".hashCode();");
  }

  @Override
  public final void sourceIfNotNull(SourceBuilder s, FieldDef f) {
    s.doIf(f.optional(), "if (m", f.sourceName(), " != null)", OPEN);
  }

  @Override
  public final SourceBuilder sourceEndIf(SourceBuilder s) {
    return s.endIf(CLOSE);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("m", f.sourceName(), " = js.data.DataUtil.parse", typeName(), "List(m, ", f.nameStringConstantQualified(),
        ", ", f.optional(), ");");
  }

  /**
   * Generate source code to convert a string to a value to be used as a map key
   */
  public String deserializeStringToMapKey(String stringValueExpression) {
    if (isPrimitive())
      return typeName() + ".parse" + DataUtil.capitalizeFirst(typeName()) + "(" + stringValueExpression + ")";
    throw notSupported("deserializeStringToMapKey for dataType:", getClass());
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
