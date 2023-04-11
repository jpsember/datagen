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
import datagen.ParseTools;
import datagen.SourceBuilder;
import datagen.gen.QualifiedName;

public class JavaSetDataType extends JavaDataType {

  public JavaSetDataType(JavaDataType wrappedValueType) {
    mWrappedValueType = wrappedValueType;
    setQualifiedClassName(ParseTools.parseQualifiedName(
        "java.util.Set<" + wrappedValueType.qualifiedClassName().className() + ">", null));
  }

  public JavaDataType wrappedValueType() {
    return mWrappedValueType;
  }

  @Override
  protected String provideTypeName() {
    QualifiedName valName = wrappedValueType().qualifiedClassName();
    return ParseTools.PKG_SET + "<" + ParseTools.importExprWithClassName(valName) + ">";
  }

  @Override
  public String provideSourceDefaultValue() {
    return ParseTools.PKG_DATAUTIL + ".emptySet()";
  }

  /**
   * Constructs a mutable copy of a set. Note that while it creates a copy of
   * the set, it doesn't create copies of its elements; the references to those
   * elements are stored in the new set unchanged.
   */
  @Override
  public String sourceExpressionToMutable(String valueExpression) {
    if (!Context.generatedTypeDef.classMode())
      return ParseTools.mutableCopyOfList(valueExpression);
    return valueExpression;
  }

  @Override
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    if (!Context.generatedTypeDef.classMode()) {
      s.a(targetExpression, " = ", ParseTools.immutableCopyOfSet(valueExpression));
      return;
    }
    if (Context.debugMode()) {
      s.a(targetExpression, " = ", ParseTools.immutableCopyOfSet(valueExpression), ParseTools.debugComment());
      return;
    }
    super.sourceExpressionToImmutable(s, fieldDef, targetExpression, valueExpression);
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a(OPEN, //
        ParseTools.PKG_JSLIST, " j = new ", ParseTools.PKG_JSLIST, "();", CR, //
        "for (", wrappedValueType().typeName(), " e : ", f.instanceName(), ")", IN, //
        "j.add(", wrappedValueType().sourceGenerateSerializeToObjectExpression("e"), ");", OUT, //
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
        "JSList m2 = m.optJSList(", QUOTE, f.name(), ");", CR, //
        "if (m2 != null && !m2.isEmpty())", OPEN, //
        "Set<", wrappedValueType().typeName(), "> mp = new ", ParseTools.PKG_HASH_SET, "<>();", CR, //
        "for (Object e : m2.wrappedList())", IN, //
        "mp.add(", wrappedValueType().deserializeJsonToMapValue("e"), ");", OUT);
    String expr = "mp";
    if (Context.debugClassMode())
      expr = ParseTools.immutableCopyOfSet(expr);
    s.a(f.instanceName(), " = ", expr, ";", CLOSE, //
        CLOSE);
    s.close();
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a("r = r * 37 + ", f.instanceName(), ".hashCode();");
  }

  private final JavaDataType mWrappedValueType;
}
