/**
 * MIT License
 *
 * Copyright (c) 2021 Jeff Sember
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/
package datagen.datatype;

import static js.base.Tools.*;
import static datagen.Context.*;

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.SourceBuilder;
import js.data.DataUtil;
import js.json.JSMap;

public final class RustFileDataType extends RustStringDataType {

  public static final DataType TYPE = new RustFileDataType();

  private RustFileDataType() {
    with("JFile");
  }

  public static String nullFileMethod() {
    return pt.PKG_RUST_JFILE + "null_file";
  }

  @Override
  public String provideSourceDefaultValue() {
    return nullFileMethod() + "()";
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {
    String text = json.get("");
    return "new_file(" + DataUtil.escapeChars(text, true) + ")";
  }

  /**
   * The default value is expressed as rust code. Convert it to a string literal
   */
  private String getStrPtrFromDefaultValue(String s) {

    // NOTE: this whole method is very brittle; if user has default values that
    // include these bits of code, we're in big trouble

    if (s.contains("null_file()")) {
      return "\"\"";
    }

    var target = "new_file(";
    int j = s.indexOf(target);
    if (j >= 0) {
      var content = chomp(s.substring(j + target.length()), ")");
      return content;
    }
    throw badArg("unexpected arg for getStrPtrFromDefaultValue:", quote(s));
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(),
        Context.pt.PKG_RUST_JFILE,
        " = new_file(&m.opt(", f.nameStringConstantQualified(), ").or_str(",
        getStrPtrFromDefaultValue(f.defaultValueSource()), ")?);", CR);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return "&" + valueExpression + ".to_string()";
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    return "new " + typeName() + "((String) " + jsonValue + ")";
  }

  @Override
  public String setterArgSignature(String expr) {
    return "JFile";
  }

  @Override
  public String setterArgUsage(String expr) {
    return expr;
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = m.opt(", f.nameStringConstantQualified(),
        ").or_empty_list()?.parse_jfile_list()?;");
  }

  @Override
  public void generateSerializeListOf(SourceBuilder s, FieldDef f) {
    s.a("encode_jfile_list(&self.", f.instanceName(), ")");
  }

  public String getterReturnTypeExpr() {
    return "&JFile";
  }

}
