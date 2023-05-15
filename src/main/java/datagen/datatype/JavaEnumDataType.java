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
import datagen.JavaDataType;
import datagen.SourceBuilder;
import static datagen.Utils.*;

public class JavaEnumDataType extends JavaDataType implements EnumDataType {

  @Override
  public String provideSourceDefaultValue() {
    return typeName() + ".DEFAULT_INSTANCE";
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.doIf(f.optional(), "if (", f.instanceName(), " != null)", OPEN);
    s.a("r = r * 37 + ", f.instanceName(), ".ordinal();");
    s.endIf(CLOSE);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return valueExpression + ".toString().toLowerCase()";
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a(OPEN, //
        "String x = m.opt(", f.nameStringConstantQualified(), ", \"\");", CR, //
        f.instanceName(), " = x.isEmpty() ? ", typeName(), ".DEFAULT_INSTANCE : ", typeName(),
        ".valueOf(x.toUpperCase());", CLOSE);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    throw languageNotSupported("deserializing list of Java enums from list");
  }

  //------------------------------------------------------------------
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
