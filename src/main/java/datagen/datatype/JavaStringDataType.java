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

import static datagen.ParseTools.*;
import static js.base.Tools.*;

import datagen.Context;
import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.ParseTools;
import datagen.SourceBuilder;
import js.data.DataUtil;
import js.json.JSMap;

public final class JavaStringDataType extends JavaDataType {

  {
    with("java.lang.String");
  }

  @Override
  public final String provideSourceDefaultValue() {
    return "\"\"";
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {
    return DataUtil.escapeChars(json.get(""), true);
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a(f.instanceName(), " = m.opt(", f.nameStringConstantQualified(), ", ");
    if (!f.optional())
      s.a(f.defaultValueOrNull());
    else
      s.a("(String) null");
    s.a(");", CR);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    String expr = PKG_DATAUTIL + ".parseListOfObjects(m.optJSList(" + f.nameStringConstantQualified() + "), "
        + f.optional() + ")";
    if (Context.debugMode()) {
      expr = ParseTools.immutableCopyOfList(expr);
    }
    s.a(f.instanceName(), " = ", expr, ";", CR);
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    return "(String) " + jsonValue;
  }

  @Override
  public String deserializeStringToMapKey(String jsonStringValue) {
    return jsonStringValue;
  }

}
