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
import datagen.DataType;
import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.SourceBuilder;
import js.json.JSList;
import js.json.JSMap;
import js.json.JSUtils;

public class JavaListDataType extends JavaDataType {

  public JavaListDataType(DataType wrappedType) {
    mWrappedType = wrappedType;
    with("java.util.List<" + wrappedType.qualifiedName().className() + ">");
  }

  @Override
  public String provideSourceDefaultValue() {
    return Context.pt.PKG_DATAUTIL + ".EMPTY_LIST";
  }

  @Override
  public String provideSourceDefaultValueForBuilder() {
    return "new " + Context.pt.PKG_ARRAYLIST + "(0)";
  }

  public DataType wrappedType() {
    return mWrappedType;
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a(OPEN, //
        Context.pt.PKG_JSLIST, " j = new ", Context.pt.PKG_JSLIST, "();", CR, //
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
    sb.a("  private static final ", typeName(), " ", fieldDef.constantName(), " = ", Context.pt.PKG_TOOLS,
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
      expr = "(x == null) ? " + f.defaultValueSourceForBuilder() + " : x";
    }

    s.a(targetExpr, " = ", expr);
  }

  @Override
  public String staticToBuilder(String expr) {
    return Context.pt.PKG_DATAUTIL + ".mutableCopyOf(" + expr + ")";
  }

  @Override
  public String builderToStatic(String expr) {
    return Context.pt.PKG_DATAUTIL + ".immutableCopyOf(" + expr + ")";
  }

  private final DataType mWrappedType;
}
