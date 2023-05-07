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
import datagen.DataType;
import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.ParseTools;
import datagen.SourceBuilder;
import js.json.JSList;
import js.json.JSMap;
import js.json.JSUtils;

public class JavaListDataType extends JavaDataType {

  public JavaListDataType(DataType wrappedType) {
    mWrappedType = wrappedType;
    with("java.util.List<" + wrappedType.qualifiedName().className() + ">");
//    setTypeName(
//        ParseTools.PKG_LIST + "<" + ParseTools.importExprWithClassName(wrappedType().qualifiedName()) + ">");
  }

  @Override
  public String provideSourceDefaultValue() {
    return ParseTools.PKG_DATAUTIL + ".emptyList()";
  }

  /**
   * Constructs a mutable copy of a list. Note that while it creates a copy of
   * the list, it doesn't create copies of its elements; the references to those
   * elements are stored in the new list unchanged.
   * 
   * This does not apply unless 'old style' is in effect
   */
  @Override
  public String sourceExpressionToMutable(String valueExpression) {
    if (!Context.generatedTypeDef.classMode())
      return ParseTools.mutableCopyOfList(valueExpression);
    // In debug mode, this is already in immutable form; no need to modify it
    return valueExpression;
  }

  @Override
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    if (!Context.generatedTypeDef.classMode()) {
      s.a(targetExpression, " = ", ParseTools.immutableCopyOfList(valueExpression));
      return;
    }

    if (Context.debugMode()) {
      s.a(targetExpression, " = ", ParseTools.immutableCopyOfList(valueExpression));
      return;
    }
    super.sourceExpressionToImmutable(s, fieldDef, targetExpression, valueExpression);
  }

  public DataType wrappedType() {
    return mWrappedType;
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a(OPEN, //
        ParseTools.PKG_JSLIST, " j = new ", ParseTools.PKG_JSLIST, "();", CR, //
        "for (", wrappedType().typeName(), " x : ", f.instanceName(), ")", IN, //
        "j.add(", wrappedType().sourceGenerateSerializeToObjectExpression("x"), ");", OUT, //
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
    wrappedType().sourceDeserializeFromList(s, f);
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a("for (", wrappedType().typeName(), " x : ", f.instanceName(), ")", IN);
    s.a("if (x != null)", IN);
    s.a("r = r * 37 + x.hashCode();", OUT);
    s.a(OUT);
  }

  @Override
  public String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {

    JSList parsedExpressions = json.getList("");

    SourceBuilder sb = classSpecificSource;
    sb.a("  private static final ", typeName(), " ", fieldDef.constantName(), " = ", ParseTools.PKG_TOOLS,
        ".arrayList(");
    for (int index = 0; index < parsedExpressions.size(); index++) {
      Object expr = parsedExpressions.getUnsafe(index);
      if (index > 0)
        sb.a(",");
      sb.a(JSUtils.valueToString(expr));
    }
    sb.a(");").cr();

    return fieldDef.constantName();
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    String expr;
    if (f.optional() || isPrimitive()) {
      expr = "x";
    } else {
      expr = "(x == null) ? " + f.defaultValueOrNull() + " : x";
    }

    if (!Context.classMode()) {
      s.a(targetExpr, " = ", sourceExpressionToMutable(expr));
    } else {
      if (Context.debugMode()) {
        sourceExpressionToImmutable(s, f, targetExpr, expr);
      } else
        s.a(targetExpr, " = ", expr);
    }

  }

  private final DataType mWrappedType;
}
