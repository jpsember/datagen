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

import datagen.Context;
import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.SourceBuilder;
import js.data.DataUtil;
import js.json.JSMap;

public class JavaMapDataType extends JavaDataType {

  public JavaMapDataType(JavaDataType keyType, JavaDataType valueType) {
    mWrappedKeyType = keyType;
    mWrappedValueType = valueType;
    with("java.util.Map<" + keyType.qualifiedName().className() + ", " + valueType.qualifiedName().className()
        + ">");
  }

  public JavaDataType wrappedKeyType() {
    return mWrappedKeyType;
  }

  public JavaDataType wrappedValueType() {
    return mWrappedValueType;
  }

  @Override
  public String provideSourceDefaultValue() {
    return Context.pt.PKG_DATAUTIL + ".emptyMap()";
  }

  /**
   * Constructs a mutable copy of a map. Note that while it creates a copy of
   * the map, it doesn't create copies of its elements; the references to those
   * elements are stored in the new map unchanged.
   */
  @Override
  public String sourceExpressionToMutable(String valueExpression) {
    if (!Context.generatedTypeDef.classMode())
      return Context.pt.mutableCopyOfMap(valueExpression);
    return valueExpression;
  }

  @Override
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    if (!Context.generatedTypeDef.classMode()) {
      s.a(targetExpression, " = ", Context.pt.immutableCopyOfMap(valueExpression));
      return;
    }
    if (Context.debugMode()) {
      s.a(targetExpression, " = ", Context.pt.immutableCopyOfMap(valueExpression));
      return;
    }
    super.sourceExpressionToImmutable(s, fieldDef, targetExpression, valueExpression);
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a(OPEN, //
        Context.pt.PKG_JSMAP, " j = new ", Context.pt.PKG_JSMAP, "();", CR, //
        "for (Map.Entry<", wrappedKeyType().typeName(), ", ", wrappedValueType().typeName(), "> e : ",
        f.instanceName(), ".entrySet())", IN, //
        "j.put(", wrappedKeyType().sourceGenerateSerializeToObjectExpression("e.getKey()"), ", ",
        wrappedValueType().sourceGenerateSerializeToObjectExpression("e.getValue()"), ");", OUT, //
        "m.put(", f.nameStringConstantQualified(), ", j);", //
        CLOSE, CR);
    sourceEndIf(s);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.open();
    if (!f.optional())
      s.a(f.instanceName(), " = ", f.defaultValueOrNull(), ";", CR);

    s.a(OPEN, //
        "JSMap m2 = m.optJSMap(", QUOTE, f.name(), ");", CR, //
        "if (m2 != null && !m2.isEmpty())", OPEN, //
        "Map<", wrappedKeyType().typeName(), ", ", wrappedValueType().typeName(), "> mp = new ",
        Context.pt.PKG_CONCURRENT_MAP, "<>();", CR, //
        "for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())", IN, //
        "mp.put(", wrappedKeyType().deserializeStringToMapKey("e.getKey()"), ", ",
        wrappedValueType().deserializeJsonToMapValue("e.getValue()"), ");", OUT //
    );
    String expr = "mp";
    if (Context.debugMode())
      expr = Context.pt.immutableCopyOfMap(expr);
    s.a(f.instanceName(), " = ", expr, ";", CLOSE, //
        CLOSE);

    s.close();
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a("r = r * 37 + ", f.instanceName(), ".hashCode();");
  }

  @Override
  public String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDefOrNull, JSMap json) {
    FieldDef fieldDef = fieldDefOrNull;
    SourceBuilder s = classSpecificSource;
    s.in();
    s.a("private static final ", typeName(), " ", fieldDef.constantName(), " = new ",
        Context.pt.PKG_CONCURRENT_MAP, "<>();", CR, BR);
    s.a("static ", OPEN, //
        "JSMap m = new JSMap(", DataUtil.escapeChars(json.toString(), true), ");", CR, //
        "for (Map.Entry<String, Object> e : m.wrappedMap().entrySet())", IN, //
        fieldDef.constantName(), ".put(", wrappedKeyType().deserializeStringToMapKey("e.getKey()"), ", ",
        wrappedValueType().deserializeJsonToMapValue("e.getValue()"), ");", OUT, //
        "// will the cast to (Integer) always work?", CR, //
        CLOSE //
    );
    s.out();
    return fieldDef.constantName();
  }

  private final JavaDataType mWrappedKeyType;
  private final JavaDataType mWrappedValueType;
}
