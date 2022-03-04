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
import datagen.SourceBuilder;
import js.parsing.Scanner;
import static datagen.ParseTools.*;

public class PrimitiveDoubleDataType extends DataType {

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "java.lang.double";
  }

  @Override
  public final String compilerInitialValue() {
    return "0.0";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    double value = parseDoubleValue(scanner.read(NUMBER).text());
    return Double.toString(value);
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    if (python()) {
      if (f.optional()) {
        notSupported("optional Python floats");
      } else {
        s.a("r = r * 37 + int(self._", f.javaName(), ")");
      }
      return;
    }

    if (f.optional()) {
      s.a("r = r * 37 + m", f.javaName(), ".intValue();");
    } else
      s.a("r = r * 37 + (int) m", f.javaName(), ";");
  }

  @Override
  public DataType optionalVariant() {
    return BOXED_SINGLETON;
  }

  @Override
  public DataType listVariant() {
    return DoubleArrayDataType.SINGLETON;
  }

  private PrimitiveDoubleDataType() {
    loadTools();
  }

  private static class Boxed extends PrimitiveDoubleDataType {

    @Override
    protected String provideQualifiedClassNameExpr() {
      return "java.lang.Double";
    }

    @Override
    public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
      s.a("m", f.javaName(), " = m.optDouble(", f.nameStringConstant(), ");");
    }

  }

  public static final DataType SINGLETON = new PrimitiveDoubleDataType();
  private static final DataType BOXED_SINGLETON = new Boxed();

}
