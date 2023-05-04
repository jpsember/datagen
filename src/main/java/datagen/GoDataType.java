package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

public abstract class GoDataType extends DataType {

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
    s.a(ParseTools.notSupportedMessage());
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a(ParseTools.notSupportedMessage());
  }

  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    String arg = f.instanceName();
    if (isPrimitive()) {
      s.a(targetExpr, " = ", arg, CR);
    } else {
      s.a("if x == nil {", IN, //
          "x = ", f.defaultValueOrNull(), OUT, //
          targetExpr, " = x", CR);
    }
  }

  @Override
  public void sourceIfNotNull(SourceBuilder s, String expr) {
    s.a(ParseTools.notSupportedMessage());
  }

  @Override
  public void sourceIfNotNull(SourceBuilder s, FieldDef f) {
    s.a(ParseTools.notSupportedMessage());
  }

  @Override
  public SourceBuilder sourceEndIf(SourceBuilder s) {
    return s.endIf(CLOSE);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    //    var jslist = s.OptList("kids")
    //    if jslist != nil {
    //      var length = jslist.Length()
    //      var z = make([]string, length)
    //      for i := 0; i < length; i++ {
    //        z[i] = jslist.Get(i).ToString()
    //      }
    //      n.kids = z
    //    }
    s.a(OPEN, //
        "var jslist = s.OptList(\"", f.instanceName(), "\")", CR, //
        "if jslist != nil ", OPEN, //
        "var length = jslist.Length()", CR, "var z = make(", f.dataType().typeName(), ", length)", CR, //
        "for i := 0; i < length; i++ ", OPEN, //
        "z[i] = ", parseElementFromJsonValue(f, "jslist.Get(i)"), CLOSE, //
        "n.", f.instanceName(), " = z", CLOSE, //
        CLOSE);
  }

  /**
   * Construct go source code to extract a datatype's value from a JSEntity
   */
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    return ParseTools.notSupportedMessage();
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.a("m.Put(", f.nameStringConstantQualified(), ", ", //
        sourceGenerateSerializeToObjectExpression("v." + f.instanceName()), ")", CR);
  }
}
