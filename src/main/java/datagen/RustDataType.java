package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

public abstract class RustDataType extends DataType {

  /**
   * Generate source code for deserializing a value from a dict
   */
  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    notFinished();
    s.a("inst.", f.instanceName(), " = obj.get(", f.nameStringConstantQualified(), ", ",
        f.defaultValueOrNull(), ")");
  }

  @Override
  public void sourceGenerateEquals(SourceBuilder s, String a, String b) {
    s.a(Utils.notSupportedMessage());
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a(Utils.notSupportedMessage());
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    String arg = f.instanceName();
    s.a(targetExpr, " = ", setterArgUsage(arg), ";", CR);
  }

  @Override
  public void sourceIfNotNull(SourceBuilder s, String expr) {
    s.a(Utils.notSupportedMessage());
  }

  @Override
  public void sourceIfNotNull(SourceBuilder s, FieldDef f) {
    s.a(Utils.notSupportedMessage());
  }

  @Override
  public SourceBuilder sourceEndIf(SourceBuilder s) {
    return s.endIf(CLOSE);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    
    
//    {
//      let jslist = m.opt(KEY_FOO).or_list()?;
//      let len = jslist.len();
//      let mut y = Vec::with_capacity(len);
//      for x in 0..len {
//        let w = jslist.get_item_at(x);
//        y.push(w.as_number()? /* we want a datatype fn call here for cvt from json */);
//      }
//      n.foo = y;
//    }
//    
    
    
    s.a(OPEN, //
        "let jslist = m.opt(",f.nameStringConstantQualified(),").or_list()?;",CR, //
        "let len = jslist.len();",CR, //
        "let mut y = Vec::with_capacity(len);",CR,//
        "for x in 0..len {",CR, //
       // "let w = jslist.get(x);",CR,//
        "y.push(",parseElementFromJsonValue(f,"jslist.get_item_at(x)"),");",comment("e.g. let w = jslist.get(x);"),CR, //
        CLOSE, //
        "n.",f.instanceName()," = y;", //
        CLOSE);
  }

  /**
   * Construct go source code to extract a datatype's value from a JSEntity
   */
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    return Utils.notSupportedMessage();
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.a("m.put(", f.nameStringConstantQualified(),comment("constantName?"), ", ", //
        sourceGenerateSerializeToObjectExpression("self." + f.instanceName()), ");", CR);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    var prefix = isPrimitive() ? "" : "&";
    return prefix + valueExpression;
  }

  // ------------------------------------------------------------------
  // For supporting GoMapDataType
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
   * @param jsonValue
   *          representation of a JSMap value (from a key/value pair), or a
   *          JSList value
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
