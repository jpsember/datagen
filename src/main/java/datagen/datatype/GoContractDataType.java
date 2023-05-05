package datagen.datatype;

import datagen.FieldDef;
import datagen.GoDataType;
import datagen.SourceBuilder;
import js.json.JSMap;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import datagen.ParseTools;
import datagen.QualifiedName;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class GoContractDataType extends GoDataType {

  @Override
  public void setQualifiedClassName(QualifiedName qualifiedName) {
    super.setQualifiedClassName(qualifiedName.withClassName("I" + qualifiedName.className()));
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap map) {
    throw notFinished("add default values for contract types");
  }

  private QualifiedName alternateQualifiedClassName() {
    if (mAlternateClassWithPackage == null) {
      String alt = alternateTypeName();
      // If this looks like an import expression, just use the class name
      // (this stuff is getting complicated... refactor at some point)
      if (alt.startsWith("{{")) {
        int i = alt.indexOf('|');
        alt = alt.substring(i + 1, alt.length() - 2);
      }
      mAlternateClassWithPackage = qualifiedClassName().withClassName(alt);
    }
    return mAlternateClassWithPackage;
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
    // i.e., an example is: "{{exp.ICat|ICat}}"
    //
    n = n.replace("OrBuilder|", "|");
    n = n.replace("OrBuilder}", "}");
    return n;
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
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    return "Default" + alternateTypeName() + ".Parse(" + jsentityExpression + ").(" + typeName() + ")";
  }

  private QualifiedName mAlternateClassWithPackage;

}
