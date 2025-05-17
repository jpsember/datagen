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

import java.util.List;

import datagen.FieldDef;
import datagen.PythonDataType;
import datagen.SourceBuilder;

public class PythonEnumDataType extends PythonDataType implements EnumDataType {

  @Override
  public String provideSourceDefaultValue() {
    return typeName() + ".default_instance";
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("x = obj.get(", f.nameStringConstantQualified(), ", ", f.defaultValueSource(), ")", CR);
    s.a("inst.", f.instanceName(), " = x", CR);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("x = obj.get(", f.nameStringConstantQualified(), ", ",  "[]" , ")", CR);
    s.doIf(f.optional(), "if x is not None:", OPEN);
    s.a("inst.", f.instanceName(), " = [", typeName(), "(z) for z in x]", CR);
    s.endIf(CLOSE);
  }

  // ------------------------------------------------------------------
  // EnumDataType interface
  // ------------------------------------------------------------------

  @Override
  public void addLabel(String label) {
    label = label.toUpperCase();
    checkArgument(!mLabels.contains(label), "duplicate label:", label);
    mLabels.add(label);
  }

  @Override
  public List<String> labels() {
    return mLabels;
  }

  private List<String> mLabels = arrayList();

}
