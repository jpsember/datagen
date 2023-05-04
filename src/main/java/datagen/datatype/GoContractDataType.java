package datagen.datatype;

import datagen.FieldDef;
import datagen.GoDataType;
import datagen.SourceBuilder;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;


import datagen.ParseTools;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class GoContractDataType extends GoDataType implements ContractDataType {

  @Override
  protected String provideTypeName() {
    // We will consider the typeName() to be "FooOrBuilder", and the builtTypeName() to be "Foo".
    String expr = super.provideTypeName();
    mBuiltTypeName = expr;
    return expr + "OrBuilder";
  }

  private String builtTypeName() {
    typeName();
    return mBuiltTypeName;
  }

  private String mBuiltTypeName;

  @Override
  public String provideSourceDefaultValue() {
    return "Default" + ParseTools.importExprWithClassName(qualifiedClassName());
  }

  public String getSerializeDataType() {
    return "??Object??";
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
        "n.", f.instanceName(), " = Default", builtTypeName(), ".Parse(x).(*", builtTypeName(), ")", //
        CLOSE, //
        CLOSE);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("inst.", f.instanceName(), " = ", ParseTools.PKGPY_DATAUTIL, ".parse_list_of_objects(",
        builtTypeName(), ".default_instance, obj.get(", f.nameStringConstantQualified(), "), ",
        f.optional() ? "True" : "False", ")");
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
        "v.m.", f.instanceName(), " = Default", builtTypeName(), //
        CR, "} else {", CR, "v.m.", f.instanceName(), " = ", arg, ".Build()", //,
        CLOSE);
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public String getConstructFromX() {
    throw notSupported("GoContractDataType.getConstructFromX");
  }

}
