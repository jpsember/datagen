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

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import datagen.Context;
import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.SourceBuilder;
import js.data.DataUtil;
import js.json.JSMap;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class JavaContractDataType extends JavaDataType {

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a("m.", getStoreInJsonMapMethodName(), "(", f.nameStringConstantQualified(), ", ",
        sourceGenerateSerializeToObjectExpression("" + f.instanceName()), ");");
    sourceEndIf(s).cr();
  }

  @Override
  public String provideSourceDefaultValue() {
    return typeName() + ".DEFAULT_INSTANCE";
  }

  public String getConstructFromX() {
    return provideSourceDefaultValue() + ".parse(x)";
  }

  // Make this final for now to avoid unintended overriding
  @Override
  public final void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.open();
    s.a(f.instanceName(), " = ", f.defaultValueSource(), ";", CR);
    s.a(Context.pt.PKG_OBJECT, " x = m.optUnsafe(", f.nameStringConstantQualified(), ");", CR);
    sourceIfNotNull(s, "x");
    s.a(f.instanceName(), " = ", getConstructFromX(), ";");
    sourceEndIf(s);
    s.close();
  }

  /**
   * Get Python expression to deserialize a (json/dict) value
   */
  public String pythonDeserializeExpr(FieldDef f, String expr) {
    return f.defaultValueSource() + ".parse(" + expr + ")";
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a(f.instanceName(), " = ", Context.pt.PKG_DATAUTIL, ".parseListOfObjects(", typeName(),
        ".DEFAULT_INSTANCE, m.optJSList(", f.nameStringConstantQualified(), "), ", false, ");");
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return getSerializeToJSONValue(valueExpression);
  }

  public String getSerializeToJSONValue(String value) {
    return value + ".toJson()";
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    s.a(targetExpr, " = ", "(x == null) ? ", f.defaultValueSource(), " : x.build()");
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    return provideSourceDefaultValue() + ".parse((JSMap) " + jsonValue + ")";
  }

  @Override
  public String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDefOrNull, JSMap json) {
    FieldDef fieldDef = fieldDefOrNull;
    SourceBuilder s = classSpecificSource;
    s.a(IN, "private static final ", typeName(), " ", fieldDef.constantName(), " = ", sourceDefaultValue(),
        ".parse(new JSMap(", DataUtil.escapeChars(json.toString(), true), "));", OUT);
    return fieldDef.constantName();
  }

}
