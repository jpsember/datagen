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
import datagen.SourceBuilder;
import js.parsing.Scanner;
import static datagen.ParseTools.*;

public class PrimitiveFloatDataType extends DataType {

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "java.lang.float";
  }

  @Override
  public final String compilerInitialValue() {
    return "0f";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    float value = parseFloatValue(scanner.read(NUMBER).text());
    return Float.toString(value) + "f";
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    if (f.optional())
      s.a("r = r * 37 + m", f.javaName(), ".hashCode();");
    else
      s.a("r = r * 37 + (int)m", f.javaName(), ";");
  }

  @Override
  public DataType optionalVariant() {
    return new Boxed();
  }

  @Override
  public DataType listVariant() {
    return new Boxed();
  }

  private static class Boxed extends PrimitiveFloatDataType {

    @Override
    protected String provideQualifiedClassNameExpr() {
      return "java.lang.Float";
    }

    @Override
    public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
      s.a("m", f.javaName(), " = m.optFloat(", f.nameStringConstant(), ");");
    }

  }

}
