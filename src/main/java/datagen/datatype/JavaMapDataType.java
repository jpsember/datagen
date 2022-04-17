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

import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.ParseTools;
import datagen.SourceBuilder;

public class JavaMapDataType extends JavaDataType {

  public JavaMapDataType(JavaDataType wrappedKeyType, JavaDataType wrappedValueType) {
    mWrappedKeyType = wrappedKeyType;
    mWrappedValueType = wrappedValueType;
    setQualifiedClassName(
        ParseTools.parseQualifiedName("java.util.Map<" + wrappedKeyType.qualifiedClassName().className() + ","
            + wrappedValueType.qualifiedClassName().className() + ">", null));
  }

  public JavaDataType wrappedKeyType() {
    return mWrappedKeyType;
  }

  public JavaDataType wrappedValueType() {
    return mWrappedValueType;
  }

  @Override
  protected String provideTypeName() {
    // We need to generate import statements for the wrapped types as well as the wrapper; but we don't want 
    // to confuse the macro substitution process 
    String wrappedImport1 = ParseTools.importCodeExpr(wrappedKeyType().qualifiedClassName().combined(), "");
    String wrappedImport2 = ParseTools.importCodeExpr(wrappedValueType().qualifiedClassName().combined(), "");
    String wrapperImport = ParseTools.PKG_MAP + "<" + wrappedKeyType().qualifiedClassName().className() + ","
        + wrappedValueType().qualifiedClassName().className() + ">";
    return wrappedImport1 + wrappedImport2 + wrapperImport;
  }

  @Override
  public String provideSourceDefaultValue() {
    return ParseTools.PKG_DATAUTIL + ".emptyMap()";
  }

  /**
   * Constructs a mutable copy of a map. Note that while it creates a copy of
   * the map, it doesn't create copies of its elements; the references to those
   * elements are stored in the new map unchanged.
   */
  @Override
  public String sourceExpressionToMutable(String valueExpression) {
    return ParseTools.mutableCopyOfMap(valueExpression);
  }

  @Override
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    s.a(targetExpression, " = ", ParseTools.immutableCopyOfMap(valueExpression));
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a(OPEN, //
        ParseTools.PKG_JSMAP, " j = new ", ParseTools.PKG_JSMAP, "();", CR, //
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
        ParseTools.PKG_CONCURRENT_MAP, "<>();", CR, //
        "for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())", IN, //
        "mp.put(", wrappedKeyType().deserializeStringToMapKey("e.getKey()"), ", ",
        wrappedValueType().deserializeJsonToMapValue("e.getValue()"), ");", OUT, //
        f.instanceName(), " = ", "mp", ";", CLOSE, //
        CLOSE);

    s.close();
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a("r = r * 37 + ", f.instanceName(), ".hashCode();");
  }

  private final JavaDataType mWrappedKeyType;
  private final JavaDataType mWrappedValueType;
}
