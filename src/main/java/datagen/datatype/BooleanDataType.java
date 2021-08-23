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

import datagen.DataType;
import datagen.FieldDef;
import datagen.SourceBuilder;
import datagen.parsing.Scanner;
import js.data.DataUtil;

public class BooleanDataType extends DataType {

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "java.lang.boolean";
  }

  @Override
  public DataType optionalVariant() {
    return BOXED_SINGLETON;
  }

  @Override
  public final String compilerInitialValue() {
    if (python())
      return "False";
    return "false";
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    if (python()) {
      s.a("r = r * 37 + int(self._", f.javaName(), ")", CR);
      return;
    }
    s.a("r = r * 37 + (m", f.javaName(), " ? 1 : 0);");
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    String label = scanner.read(BOOL).text();
    if (python())
      return DataUtil.capitalizeFirst(label);
    return label;
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    if (python()) {
      s.a("inst._", f.javaName(), " = obj.get(", f.nameStringConstant(), ", ");
      s.a(f.defaultValueOrNull());
      s.a(")", CR);
      return;
    }

    s.a("m", f.javaName(), " = m.opt(", f.nameStringConstant(), ", ");
    if (!f.optional())
      s.a(f.defaultValueOrNull());
    else
      s.a("(Boolean) null");
    s.a(");", CR);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    if (python()) {
      super.sourceDeserializeFromList(s, f);
      return;
    }

    s.a("m", f.javaName(), " = ", PKG_DATAUTIL, ".parseListOfObjects(m.optJSList(", f.nameStringConstant(),
        "), ", f.optional(), ");", CR);
  }

  private static class Boxed extends BooleanDataType {

    @Override
    protected String provideQualifiedClassNameExpr() {
      return "java.lang.Boolean";
    }

    @Override
    public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
      if (python()) {
        s.a("inst._", f.javaName(), " = obj.get(", f.nameStringConstant(), ", None)");
        return;
      }

      s.a("m", f.javaName(), " = m.opt(", f.nameStringConstant(), ", ");
      if (!f.optional())
        s.a(f.defaultValueOrNull());
      else
        s.a("(Boolean) null");
      s.a(");");
    }

  }

  public static final DataType SINGLETON = new BooleanDataType();
  private static final DataType BOXED_SINGLETON = new Boxed();

}
