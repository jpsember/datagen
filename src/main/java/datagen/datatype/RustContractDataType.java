package datagen.datatype;

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.SourceBuilder;
import js.data.DataUtil;
import js.json.JSMap;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import datagen.QualifiedName;
import datagen.RustDataType;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class RustContractDataType extends RustDataType {

  @Override
  public DataType withQualifiedName(QualifiedName qn) {
    with(NAME_MAIN, qn);
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
    var result = "Default" + Context.pt.importExprWithClassName(qualifiedName(NAME_HUMAN));
    return result;
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a(OPEN, "let x = n.opt(", f.nameStringConstantQualified(), ");",
        comment("RustContractDataType: sourceDeserializeFromObject"), CR, //
        "if !x.is_null()", OPEN, //
        "n.", f.instanceName(), " = default_", qualifiedName(NAME_HUMAN).className(), "().parse(x.clone());",
        CLOSE, //
        CLOSE);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return getSerializeToJSONValue(valueExpression);
  }

  public String getSerializeToJSONValue(String value) {
    return value + ".to_json()";
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

  @Override
  public String setterArgSignature(String expr) {
    return Context.pt.PKG_RUST_ARC + "<" + expr + ">";
  }

  public String setterArgUsage(String expr) {
    return expr + ".clone()" + comment("RustContractDataType : setterArgUsage()");
  }

  @Override
  public String getterReturnTypeExpr() {
    return typeName() + comment("RustContractDataType : getterReturnTypeExpr()");
  }

  @Override
  public void getterBody(SourceBuilder s, FieldDef f) {
    s.a("self.", f.instanceName(), ".clone()", comment("RustContractDataType : getterBody()"));
  }
}
