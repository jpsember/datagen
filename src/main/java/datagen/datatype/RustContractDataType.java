package datagen.datatype;

import datagen.DataType;
import datagen.FieldDef;
import datagen.GoDataType;
import datagen.SourceBuilder;
import js.data.DataUtil;
import js.json.JSMap;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import datagen.QualifiedName;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class RustContractDataType extends GoDataType {

  @Override
  public DataType withQualifiedName(QualifiedName qualifiedName) {

    // Have the 'main' type name be the interface, e.g. Cat
    with(NAME_MAIN, qualifiedName);

    // Have the 'alternate' type name be the static name, e.g. Cat
    // ...but preceded by a small s, so it is not exported
    String x = qualifiedName.combined();
    int i = x.lastIndexOf('.');
    checkArgument(i > 0, qualifiedName);
    with(NAME_ALT, x.substring(0, i + 1) + "s" + x.substring(i + 1));

    // The human name can be the same as the main name
    //  with(NAME_HUMAN, qualifiedName);
    return this;
  }

  @Override
  public String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDefOrNull, JSMap json) {
    FieldDef fieldDef = fieldDefOrNull;
    SourceBuilder s = classSpecificSource;
    s.a("var ", fieldDef.constantName(), " = ", sourceDefaultValue(), ".Parse(JSMapFromStringM(",
        DataUtil.escapeChars(json.toString(), true), ")).(", typeName(), ")", CR);
    return fieldDef.constantName();
  }

  @Override
  public String provideSourceDefaultValue() {
    return "Default" + qualifiedName(NAME_HUMAN).className();
  }

  // Make this final for now to avoid unintended overriding
  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a(OPEN, "var x = s.OptUnsafe(\"", f.name(), "\")", CR, //
        "if x != nil ", OPEN, //
        "n.", f.instanceName(), " = Default", qualifiedName(NAME_HUMAN).className(), ".Parse(x.(JSEntity)).(",
        typeName(), ")", //
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
    var cn = qualifiedName(NAME_HUMAN).className();
    String arg = f.instanceName();
    s.a("if ", arg, " == nil ", OPEN, //
        targetExpr, " = Default", cn, //
        CR, OUT, "} else {", IN, CR, targetExpr, " = ", arg, ".Build()", //,
        CLOSE);
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    return "Default" + qualifiedName(NAME_HUMAN).className() + ".Parse(" + jsentityExpression + ").("
        + typeName() + ")";
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    return provideSourceDefaultValue() + ".Parse(" + jsonValue + ").(" + qualifiedName(NAME_HUMAN).className()
        + ")";
  }
}
