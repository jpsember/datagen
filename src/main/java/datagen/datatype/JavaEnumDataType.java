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
  protected void todoMarker() {
  }

  @Override
  public String provideSourceDefaultValue() {
    switch (language()) {
    default:
      throw languageNotSupported();
    case PYTHON:
      return typeName() + ".default_instance";
    case JAVA:
      return typeName() + ".DEFAULT_INSTANCE";
    }
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    todo("we can apparently calculate the hash of None in Python, so we can simplify some things?");
    if (python()) {
      s.a("r = r * 37 + hash(self._", f.sourceName(), ")");
    } else {
      s.doIf(f.optional(), "if (m", f.sourceName(), " != null)", OPEN);
      s.a("r = r * 37 + m", f.sourceName(), ".ordinal();");
      s.endIf(CLOSE);
    }
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    if (python())
      return valueExpression;
    return valueExpression + ".toString().toLowerCase()";
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    if (python()) {
      s.a("x = obj.get(", f.nameStringConstant(), ", ", f.defaultValueOrNull(), ")", CR);
      if (f.optional()) {
        s.a("if x is not None:", IN);
      }
      s.a("inst._", f.sourceName(), " = x", CR);
      if (f.optional()) {
        s.a(OUT);
      }
      return;
    } else {
      s.a(OPEN, //
          "String x = m.opt(", f.nameStringConstant(), ", \"\");", CR, //
          "m", f.sourceName(), " = x.isEmpty() ? ", typeName(), ".DEFAULT_INSTANCE : ", typeName(),
          ".valueOf(x.toUpperCase());", CLOSE);
    }
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    if (python()) {
      s.a("x = obj.get(", f.nameStringConstant(), ", ", f.nullIfOptional("[]"), ")", CR);
      s.doIf(f.optional(), "if x is not None:", OPEN);
      s.a("inst._", f.sourceName(), " = [", typeName(), "(z) for z in x]", CR);
      s.endIf(CLOSE);
    } else {
      throw languageNotSupported("deserializing list of Java enums from list");
    }
  }

  //------------------------------------------------------------------
  // EnumDataType interface
  // ------------------------------------------------------------------

  @Override
  public void addLabel(String label) {
    checkArgument(!mLabels.contains(label), "duplicate label:", label);
    mLabels.add(label);
  }

  @Override
  public List<String> labels() {
    return mLabels;
  }

  private List<String> mLabels = arrayList();
}
