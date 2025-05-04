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
 * 
 **/
package datagen.datatype;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;
import static datagen.Utils.*;

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.SourceBuilder;
import js.data.DataUtil;
import js.json.JSMap;

public final class RustFileDataType extends RustStringDataType {

  public static final DataType TYPE = new RustFileDataType();

  private RustFileDataType() {
    with(RUST_IMPORT_ALL_PREFIX + "crate.files.JFile");
  }

  @Override
  public final String provideSourceDefaultValue() {
    return "null_file()";
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {
    String text = json.get("");
    return "new_file(&" + DataUtil.escapeChars(text, true) + ")";
  }

  // Kind of hacky... get the source within "new_file(......)"
  private String getStrPtrFromDefaultValue(String s) {
    return chomp(chompPrefix(s, "new_file("), ")");
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = new_file(&m.opt(", f.nameStringConstantQualified(), ").or_str(",
        getStrPtrFromDefaultValue(f.defaultValueSource()), ")?);", CR);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return valueExpression + ".toString()";
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a(OPEN, //
        Context.pt.PKG_LIST, "<", typeName(), "> result = ", //
        f.nullIfOptional("new " + Context.pt.PKG_ARRAYLIST + "<>()"), ";", CR, //
        Context.pt.PKG_JSLIST, " j = m.optJSList(", f.nameStringConstantQualified(), ");", CR);
    sourceIfNotNull(s, "j");
    s.a("result = ", Context.pt.PKG_DATAUTIL, ".parseFileListFrom(j);");
    s.a(CLOSE);
    s.a(f.instanceName(), " = ", Context.pt.assignToListExpr("result"), ";");
    sourceEndIf(s);
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    return "new " + typeName() + "((String) " + jsonValue + ")";
  }

}
