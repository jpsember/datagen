package datagen.datatype;

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.SourceBuilder;
import js.base.Pair;
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
    s.a(OPEN, "let x = m.opt(", f.nameStringConstantQualified(), ");", comment(""), CR, //
        "if !x.is_null()", OPEN, //
        "n.", f.instanceName(), " = parse_", qualifiedName(NAME_HUMAN).className(), "(x.clone())?;", CLOSE, //
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
    String arg = f.instanceName();
    if (RUST_COMMENTS) {
      s.a(comment("the import should be something like: 'use crate::gen::saturn::Saturn;'"));
    }
    s.a(targetExpr, " = ", arg, ".build();", CR);
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
    return expr + ".clone()" + comment("");
  }

  @Override
  public String getterReturnTypeExpr() {
    return typeName() + comment("package path:" + this.qualifiedName().packagePath());
  }

  @Override
  public void getterBody(SourceBuilder s, FieldDef f) {
    s.a("self.", f.instanceName(), ".clone()", comment(""));
  }

  @Override
  public String buildRustJsonValueFrom(String expr) {
   return  "parse_" + qualifiedName(NAME_HUMAN).className() + "(" + expr + ")";
  }
  
  @Override
  public Pair<String, String> buildSerializeFromListVariable(String varName) {
    return pair("&" + varName,  varName  + ".clone()");
  }
}
