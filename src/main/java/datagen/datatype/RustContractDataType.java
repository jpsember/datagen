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
    var result = "default_" + Context.pt.importExprWithClassName(qualifiedName(NAME_HUMAN)) + "()";
    return result;
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a(OPEN, "let x = m.opt(", f.nameStringConstantQualified(), ");", CR, //
        "if !x.is_null()", OPEN, //
        "n.", f.instanceName(), " = parse_", qualifiedName(NAME_HUMAN).className(), "(x.clone())?;", CLOSE, //
        CLOSE);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    //    {
    //      let x = m.opt(KEY_FOXES).or_empty_list()?.extract_list_elements()?;
    //      for item in x {
    //          n.foxes.push(parse_Fox(item)?)
    //      }
    //  }
    var target = "n."+f.instanceName();
    s.a(
        OPEN, //
        "let x = m.opt(", f.nameStringConstantQualified(), ").or_empty_list()?.extract_list_elements()?;", CR, //
        "for y in x ", OPEN, target,".push(parse_", qualifiedName(NAME_HUMAN).className(), "(y)?)", CLOSE, //
        CLOSE //
    );
  }

  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return getSerializeToJSONValue(valueExpression);
  }

  public String getSerializeToJSONValue(String value) {
    return value + ".to_json()";
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    s.a(targetExpr, " = ", f.dataType().wrapInBuildExpression(f.instanceName()), ";", CR);
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    return "parse_" + qualifiedName(NAME_HUMAN).className() + "(" + jsentityExpression + ")";
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    notFinished();
    return provideSourceDefaultValue() + ".Parse(" + jsonValue + ").(" + qualifiedName(NAME_HUMAN).className()
        + ")";
  }

  @Override
  public String setterArgSignature(String expr) {
    return qualifiedName(NAME_HUMAN).className();
  }

  public String setterArgUsage(String expr) {
    return expr + ".clone()";
  }

  @Override
  public String getterReturnTypeExpr() {
    return typeName();
  }

  @Override
  public void getterBody(SourceBuilder s, FieldDef f) {
    s.a("self.", f.instanceName(), ".clone()");
  }

  @Override
  public String buildRustJsonValueFrom(String expr) {
    return "parse_" + qualifiedName(NAME_HUMAN).className() + "(" + expr + ")";
  }

  @Override
  public void generateSerializeListOf(SourceBuilder s, FieldDef f) {
    s.a(OPEN, //
        "let mut x = Vec::new();", CR, //
        "for y in &self.", f.instanceName(), " ", OPEN, //
        "x.push(y.to_json());", CLOSE, //
        "new_list_with(x)", CLOSE);
  }

  @Override
  public String wrapInBuildExpression(String expr) {
    return expr;
  }
}
