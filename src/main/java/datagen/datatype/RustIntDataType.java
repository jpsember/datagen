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
//import js.data.DataUtil;
import js.json.JSMap;

public final class RustIntDataType extends RustDataType {

  public static final DataType TYPE = new RustIntDataType().with("i32");

  private RustIntDataType() {
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

//  @Override
//  public String compilerInitialValue() {
//    return "\"\"";
//  }

  @Override
  public final String provideSourceDefaultValue() {
    return "0";
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {
    return Integer.toString(json.getInt(""));
  }
  
  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.a("m.put_int(", f.nameStringConstantQualified(), ", ", //
        sourceGenerateSerializeToObjectExpression("self." + f.instanceName()), ");", CR);
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    var x = f.defaultValueOrNull();
    s.a("n.", f.instanceName(), " = m.get(", f.nameStringConstantQualified(), ").or_int(", x, ")? as i32 /* do I need a semicolon here? */");
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return  valueExpression + " as i64";
  }

  @Override
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    return jsentityExpression + ".as_int()? as i32";
  }

  @Override
  public String deserializeStringToMapKey(String stringValueExpression) {
    return stringValueExpression;
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    return jsonValue + ".as_int()?";
  }

  @Override
  public String setterArgSignature(String expr) {
    return "i32";
  }

  @Override
  public String setterArgUsage(String expr) {
    return expr; // + ".to_string()";
  }

}
