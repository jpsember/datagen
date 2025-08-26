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
 **/
package datagen.datatype;

import static js.base.Tools.*;

import datagen.DataType;
import datagen.FieldDef;
import datagen.RustDataType;
import datagen.SourceBuilder;
import js.json.JSMap;

public final class RustIntDataType extends RustDataType {

  public static final DataType BYTE_TYPE = new RustIntDataType(8).with("i8");
  public static final DataType SHORT_TYPE = new RustIntDataType(16).with("i16");
  public static final DataType INT_TYPE = new RustIntDataType(32).with("i32");
  public static final DataType LONG_TYPE = new RustIntDataType(64).with("i64");

  private int mBitSize;

  private RustIntDataType(int bitSize) {
    mBitSize = bitSize;
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public final String provideSourceDefaultValue() {
    return "0";
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {
    return Integer.toString(json.getInt(""));
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.a("m.put_int(", f.nameStringConstantQualified(), ", ", //
        sourceGenerateSerializeToObjectExpression("self." + f.instanceName()), ");", CR);
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = m.opt(", f.nameStringConstantQualified(), ").or_int(", f.defaultValueSource(), ")?");
    if (mBitSize != 64)
      s.a(" as i", mBitSize);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    var x = valueExpression;
    if (mBitSize != 64) {
      x = "i64::from(" + x + ")";
    }
    return x;
  }

  @Override
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    var x = jsentityExpression + ".as_int()?";
    if (mBitSize != 64)
      x += " as i" + mBitSize;
    return x;
  }

  @Override
  public String deserializeStringToMapKey(String stringValueExpression) {
    return stringValueExpression;
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    return jsonValue + ".as_int()?";
  }

  @Override
  public String setterArgSignature(String expr) {
    return "i" + mBitSize;
  }

  @Override
  public String setterArgUsage(String expr) {
    return expr;
  }

  @Override
  public String buildRustJsonValueFrom(String expr) {
    return "new_int(*" + expr + ((mBitSize != 64) ? " as i64" : "") + ")";
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = m.opt(", f.nameStringConstantQualified(), ").or_empty_list()?.parse_i",
        mBitSize, "_list()?;");
  }

  @Override
  public void generateSerializeListOf(SourceBuilder s, FieldDef f) {
    s.a("encode_i", mBitSize, "_list(&self.", f.instanceName(), ")");
  }

}
