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

import datagen.DataType;
import datagen.FieldDef;
import datagen.PythonDataType;
import datagen.SourceBuilder;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

public class PythonListDataType extends PythonDataType {

  public PythonListDataType(DataType wrappedType) {
    mWrappedType = wrappedType;
    with("typing.List[" // 
        + wrappedType().qualifiedName().className() //
        + "]");
  }

  @Override
  public String provideSourceDefaultValue() {
    return "[]";
  }

  private DataType wrappedType() {
    return mWrappedType;
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    // TODO: for Python, we may want to convert individual items for other types, as we are doing for enums
    if (wrappedType() instanceof EnumDataType) {
      s.a("m[", f.nameStringConstantQualified(), "] = [x.value for x in self.", f.instanceName(), "]", CR);
    } else if (wrappedType() instanceof PythonContractDataType) {
      s.a("m[", f.nameStringConstantQualified(), "] = [x.to_json() for x in self.", f.instanceName(), "]",
          CR);
    } else
      s.a("m[", f.nameStringConstantQualified(), "] = self.", f.instanceName(), ".copy()", CR);
    sourceEndIf(s);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    wrappedType().sourceDeserializeFromList(s, f);
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a("for x in self.", f.instanceName(), ":", IN);
    s.a("r = r * 37 + hash(x)", CR);
    s.a(OUT);
    sourceEndIf(s);
  }

  @Override
  public String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {

    JSList parsedExpressions = json.getList("");

    SourceBuilder sb = classSpecificSource;
    sb.a(fieldDef.constantName(), "  = [");

    for (int index = 0; index < parsedExpressions.size(); index++) {
      Object expr = parsedExpressions.getUnsafe(index);
      if (index > 0)
        sb.a(",");
      sb.a(DataUtil.escapeChars(expr.toString(), true));
    }

    sb.a("]").cr();
    return fieldDef.constantName();
  }

  private final DataType mWrappedType;
}
