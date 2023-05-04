package datagen.datatype;

import datagen.FieldDef;
import datagen.GoDataType;
import datagen.SourceBuilder;
import datagen.gen.QualifiedName;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import datagen.ParseTools;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class GoContractDataType extends GoDataType implements ContractDataType {

  @Override
  public void setQualifiedClassName(QualifiedName qualifiedName) {
    QualifiedName.Builder b = qualifiedName.toBuilder();
    b.className(b.className() + "OrBuilder");
    ParseTools.assignCombined(b);
    super.setQualifiedClassName(b.build());
  }

  @Override
  public String provideSourceDefaultValue() {
    return "Default" + ParseTools.importExprWithClassName(alternateQualifiedClassName());
  }

  @Override
  protected String provideAlternateTypeName() {
    String n = typeName();

    // Delete any occurrences of 'OrBuilder' if they are followed by | or }
    //
    // i.e., an example is: "{{exp.CatOrBuilder|CatOrBuilder}}"
    //
    n = n.replace("OrBuilder|", "|");
    n = n.replace("OrBuilder}", "}");
    return n;
  }

  public String getSerializeDataType() {
    return ParseTools.notSupportedMessage();
  }

  // Make this final for now to avoid unintended overriding
  @Override
  public final void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    // If there is a value for this key, use the type's default instance to parse that value
    // and store that parsed value.
    // Otherwise, if there is no value, leave the current value alone (which may be None, e.g. if value is optional)
    //
    s.a(OPEN, "var x = s.OptMap(\"", f.name(), "\")", CR, //
        "if x != nil ", OPEN, //
        "n.", f.instanceName(), " = Default", alternateTypeName(), ".Parse(x).(", typeName(), ")", //
        CLOSE, //
        CLOSE);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return getSerializeToJSONValue(valueExpression);
  }

  public String getSerializeToJSONValue(String value) {
    return value + ".ToJson()";
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    String arg = f.instanceName();
    s.a("if ", arg, " == nil ", OPEN, //
        "v.m.", f.instanceName(), " = Default", alternateTypeName(), //
        CR, "} else {", CR, "v.m.", f.instanceName(), " = ", arg, ".Build()", //,
        CLOSE);
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public String getConstructFromX() {
    return ParseTools.notSupportedMessage();
  }

  @Override
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    return "Default" + alternateTypeName() + ".Parse(" + jsentityExpression + ").(" + typeName() + ")";
  }
}
