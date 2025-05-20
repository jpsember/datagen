package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;
import static datagen.Context.*;

public abstract class RustDataType extends DataType {

  /**
   * Generate source code for deserializing a value from a dict
   */
  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    notFinished();
    s.a("inst.", f.instanceName(), " = obj.get(", f.nameStringConstantQualified(), ", ",
        f.defaultValueSource(), ")");
  }

  @Override
  public void sourceGenerateEquals(SourceBuilder s, String a, String b) {
    s.a(notSupportedMessage());
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a(notSupportedMessage());
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    String arg = f.instanceName();
    s.a(targetExpr, " = ", setterArgUsage(arg), ";", CR);
  }

  @Override
  public void sourceIfNotNull(SourceBuilder s, String expr) {
    s.a(notSupportedMessage());
  }

  @Override
  public void sourceIfNotNull(SourceBuilder s, FieldDef f) {
    s.a(notSupportedMessage());
  }

  @Override
  public SourceBuilder sourceEndIf(SourceBuilder s) {
    return s.endIf(CLOSE);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a(OPEN, //
        "let j = m.opt(", f.nameStringConstantQualified(), ").or_empty_list()?.extract_list_elements()?;", CR, //
        "let mut x = Vec::with_capacity(j.len());", CR, //
        "for y in &j ", OPEN, //
        "x.push(", parseElementFromJsonValue(f, "y.clone()"), "?);", CLOSE, //
        "n.", f.instanceName(), " = x;", CLOSE //
    );
  }

  /**
   * Construct go source code to extract a datatype's value from a JSEntity
   */
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    return notSupportedMessage();
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.a("m.put(", f.nameStringConstantQualified(), ", ", //
        sourceGenerateSerializeToObjectExpression("self." + f.instanceName()), ");", CR);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    var prefix = isPrimitive() ? "" : "&";
    return prefix + valueExpression;
  }

  // ------------------------------------------------------------------
  // For supporting RustMapDataType
  // ------------------------------------------------------------------

  /**
   * Generate source code to convert a string to a value to be used as a map key
   */
  public String deserializeStringToMapKey(String stringValueExpression) {
    throw notSupported("deserializeStringToMapKey for dataType:", getClass());
  }

  /**
   * Generate source code to deserialize a JSON value to a value to be stored in
   * a map
   *
   * @param jsonValue representation of a JSMap value (from a key/value pair), or a
   *                  JSList value
   */
  public String deserializeJsonToMapValue(String jsonValue) {
    if (isPrimitive())
      return jsonValue;
    throw notSupported("deserializeJsonToMapValue for dataType:" + getClass());
  }

  @Override
  public String getterReturnTypeExpr() {
    var prefix = isPrimitive() ? "" : "&";
    return prefix + typeName();
  }

  @Override
  public void getterBody(SourceBuilder s, FieldDef f) {
    if (!isPrimitive())
      s.a("&");
    s.a("self.", f.instanceName());
  }

}
