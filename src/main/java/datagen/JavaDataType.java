package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import js.data.DataUtil;

public class JavaDataType extends DataType {

  @Override
  public final boolean isPrimitive() {
    // If the class name starts with a lower case letter, assume it's a primitive;
    // e.g. int, double, boolean
    // vs File, Integer, Double, Boolean
    //
    return qualifiedClassName().className().charAt(0) >= 'a';
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    String expr;
    if (f.optional() || isPrimitive()) {
      expr = "x";
    } else {
      expr = "(x == null) ? " + f.defaultValueOrNull() + " : x";
    }

    if (!Context.classMode()) {
      s.a(targetExpr, " = ", sourceExpressionToMutable(expr) );
    } else {
      if (Context.debugMode()) {
        sourceExpressionToImmutable(s, f, targetExpr, expr);
      } else
        s.a(targetExpr, " = ", expr );
    }
  }

  /**
   * Generate source code for deserializing a value from a JSMap
   */
  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a(f.instanceName(), " = m.opt(", f.nameStringConstantQualified(), ", ", f.defaultValueOrNull(), ");");
  }

  @Override
  public void sourceGenerateEquals(SourceBuilder s, String a, String b) {
    if (isPrimitive())
      s.a(a, " == ", b);
    else
      s.a(a, ".equals(", b, ")");
  }

  /**
   * Generate source code to continue the calculation of a value for hashCode().
   *
   * Default implementation assumes value is a non-null Object reference, and
   * calls its hashCode() method
   */
  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a("r = r * 37 + ", f.instanceName(), ".hashCode();");
  }

  @Override
  public void sourceIfNotNull(SourceBuilder s, String expr) {
    s.doIf(true, "if (", expr, " != null)", OPEN);
  }

  @Override
  public final void sourceIfNotNull(SourceBuilder s, FieldDef f) {
    s.doIf(f.optional(), "if (", f.instanceName(), " != null)", OPEN);
  }

  @Override
  public final SourceBuilder sourceEndIf(SourceBuilder s) {
    return s.endIf(CLOSE);
  }

  public String getStoreInJsonMapMethodName() {
    return "putUnsafe";
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a("m.", getStoreInJsonMapMethodName(), "(", f.nameStringConstantQualified(), ", ",
        sourceGenerateSerializeToObjectExpression("" + f.instanceName()), ");");
    sourceEndIf(s).cr();
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a(f.instanceName(), " = js.data.DataUtil.parse", typeName(), "List(m, ",
        f.nameStringConstantQualified(), ", ", f.optional(), ");");
  }

  // ------------------------------------------------------------------
  // For supporting JavaMapDataType
  // ------------------------------------------------------------------

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
