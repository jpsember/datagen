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
import js.json.JSMap;

/**
 * Datatype for primitive short integers, i.e. "short x;"
 */
public class JavaPrimitiveShortDataType extends JavaDataType {

  public static final DataType PRIM_TYPE = new JavaPrimitiveShortDataType().with("java.lang.short");
  private static final DataType BOXED_TYPE = new BoxedDataType().with("java.lang.Short");

  private JavaPrimitiveShortDataType() {
  }

  @Override
  public final String compilerInitialValue() {
    return "(short) 0";
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {
    return Integer.toString(json.getInt(""));
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a("r = r * 37 + ", f.instanceName(), ";");
  }

  @Override
  public DataType optionalVariant() {
    return BOXED_TYPE;
  }

  @Override
  public DataType listVariant() {
    return new JavaShortArrayDataType();
  }

  private static class BoxedDataType extends JavaPrimitiveShortDataType {

    @Override
    public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
      s.a(f.instanceName(), " = m.optShort(", f.nameStringConstantQualified(), ");");
    }

    @Override
    public String deserializeJsonToMapValue(String jsonValue) {
      return "(Short) " + jsonValue;
    }
  }

}
