package datagen.datatype;

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.SourceBuilder;
import js.data.DataUtil;
import js.json.JSMap;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;
import static datagen.Utils.*;

import datagen.QualifiedName;
import datagen.RustDataType;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class RustContractDataType extends RustDataType {

  @Override
  public DataType withQualifiedName(QualifiedName qn) {
    loadUtils();
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
    s.a(
        "/* we could optimize this by using existing (empty) n.array */",CR, //
        
        OPEN, //
        "let x = m.opt(", f.nameStringConstantQualified(), ").or_empty_list()?.extract_list_elements()?;", CR, //
        "let mut z = Vec::with_capacity(x.len());", CR, //
        "for y in x ", OPEN, "z.push(parse_", qualifiedName(NAME_HUMAN).className(), "(y)?)", CLOSE, //
        "z", CLOSE //
    );
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
    s.a(targetExpr, " = ", f.dataType().wrapInBuildExpression(f.instanceName()), ";", CR);
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    //   notFinished();
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
    s.a("encode_data_list_as_json(&self.", f.instanceName(), ", &to_json_",
        qualifiedName(NAME_HUMAN).className(), ")");
  }

  @Override
  public String wrapInBuildExpression(String expr) {
    return expr;
  }
}
