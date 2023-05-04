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

import datagen.FieldDef;
import datagen.GoDataType;
import datagen.SourceBuilder;
import js.json.JSMap;

public final class GoBooleanDataType extends GoDataType {

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public String compilerInitialValue() {
    return "false";
  }

  @Override
  protected String provideQualifiedClassNameExpr() {
    loadTools();
    return "bool";
  }

  @Override
  public final String provideSourceDefaultValue() {
    return "false";
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap map) {
    return Boolean.toString(map.getBoolean(""));
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = s.OptBool(", f.nameStringConstantQualified(), ", ",
        f.defaultValueOrNull(), ")");
  }

  //  @Override
  //  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
  //    s.a("x = obj.get(", f.nameStringConstantQualified(), ", ", f.nullIfOptional("[]"), ")", CR);
  //    s.doIf(f.optional(), "if x is not None:", OPEN);
  //    s.a("inst.", f.instanceName(), " = x.copy()");
  //    s.endIf(CLOSE);
  //  }

}
