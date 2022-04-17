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

import datagen.DataType;
import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.SourceBuilder;
import js.parsing.Scanner;
import static datagen.ParseTools.*;

/**
 * Datatype for primitive short integers, i.e. "short x;"
 */
public class PrimitiveShortDataType extends JavaDataType {

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "java.lang.short";
  }

  @Override
  public final String compilerInitialValue() {
    return "(short) 0";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    int value = (int) Scanner.ensureIntegerValue(scanner.read(NUMBER).text(), Short.MIN_VALUE,
        Short.MAX_VALUE);
    return Integer.toString(value);
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a("r = r * 37 + m", f.sourceName(), ";");
  }

  @Override
  public DataType optionalVariant() {
    return new BoxedDataType();
  }

  @Override
  public DataType listVariant() {
    return new ShortArrayDataType();
  }

  private static class BoxedDataType extends PrimitiveShortDataType {

    @Override
    protected String provideQualifiedClassNameExpr() {
      return "java.lang.Short";
    }

    @Override
    public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
      s.a("m", f.sourceName(), " = m.optShort(", f.nameStringConstant(), ");");
    }

  }

}
