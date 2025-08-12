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

import static js.base.Tools.*;

import datagen.DataType;
import datagen.FieldDef;
import datagen.RustDataType;
import datagen.SourceBuilder;
import js.json.JSMap;

public final class RustBoolDataType extends RustDataType {

  public static final DataType TYPE = new RustBoolDataType().with("bool");

  private RustBoolDataType() {
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public final String provideSourceDefaultValue() {
    return "false";
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {
    return json.getBoolean("") ? "true" : "false";
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.a("m.put(", f.nameStringConstantQualified(), ", ", //
        sourceGenerateSerializeToObjectExpression("self." + f.instanceName()), ");", CR);
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = m.opt(", f.nameStringConstantQualified(), ").or_bool(", f.defaultValueSource(), ")?");
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return "new_bool(" + valueExpression + ")";
  }

  @Override
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    var x = jsentityExpression + ".as_bool()?";
    return x;
  }

  @Override
  public String deserializeStringToMapKey(String stringValueExpression) {
    return stringValueExpression;
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    return jsonValue + ".as_bool()?";
  }

  @Override
  public String setterArgSignature(String expr) {
    return "bool";
  }

  @Override
  public String setterArgUsage(String expr) {
    return expr;
  }

  @Override
  public String buildRustJsonValueFrom(String expr) {
    return "new_bool(*" + expr + ")";
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = m.opt(", f.nameStringConstantQualified(),
        ").or_empty_list()?.parse_bool_list()?;");
  }

  @Override
  public void generateSerializeListOf(SourceBuilder s, FieldDef f) {
    s.a("encode_bool_list(&self.", f.instanceName(), ")");
  }

}
