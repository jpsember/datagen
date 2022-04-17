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
 * Datatype for longs (and boxed version)
 */
public class JavaPrimitiveLongDataType extends JavaDataType {

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "java.lang.long";
  }

  @Override
  public final String compilerInitialValue() {
    return "0L";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    long value = Scanner.ensureIntegerValue(scanner.read(NUMBER).text(), Long.MIN_VALUE, Long.MAX_VALUE);
    return Long.toString(value) + "L";
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    if (f.optional())
      s.a("r = r * 37 + m", f.sourceName(), ".intValue();");
    else
      s.a("r = r * 37 + (int)m", f.sourceName(), ";");
  }

  @Override
  public DataType optionalVariant() {
    return new Boxed();
  }

  @Override
  public DataType listVariant() {
    return new JavaLongArrayDataType();
  }

  private static class Boxed extends JavaPrimitiveLongDataType {

    @Override
    protected String provideQualifiedClassNameExpr() {
      return "java.lang.Long";
    }

    @Override
    public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
      s.a("m", f.sourceName(), " = m.optLong(", f.nameStringConstantQualified(), ");");
    }

  }

}
