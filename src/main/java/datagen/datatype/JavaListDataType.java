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

import static datagen.ParseTools.*;
import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import java.util.List;

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.ParseTools;
import datagen.SourceBuilder;
import js.parsing.Scanner;

public class JavaListDataType extends JavaDataType {

  public JavaListDataType(DataType wrappedType) {
    mWrappedType = wrappedType;
    setQualifiedClassName(ParseTools
        .parseQualifiedName("java.util.List<" + wrappedType.qualifiedClassName().className() + ">", null));
  }

  @Override
  protected String provideTypeName() {
    return ParseTools.PKG_LIST + "<" + ParseTools.importExprWithClassName(wrappedType().qualifiedClassName())
        + ">";
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
    // In debug mode, we want to ensure this is an immutable form
    if (Context.debugMode()) {
      return PKG_DATAUTIL + ".immutableCopyOf(" + valueExpression + ")";
    }
    return super.sourceExpressionToMutable(valueExpression);
  }

  @Override
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    if (!Context.generatedTypeDef.classMode())
      s.a(targetExpression, " = ", ParseTools.immutableCopyOfList(valueExpression));
    else {
      if (Context.debugMode()) {
        todo("do we need debug mode code here?");
      }
      super.sourceExpressionToImmutable(s, fieldDef, targetExpression, valueExpression);
    }
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
  public String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource, FieldDef fieldDef) {

    List<String> parsedExpressions = arrayList();

    {
      scanner.read(SQOP);
      for (int index = 0;; index++) {
        if (scanner.readIf(SQCL) != null)
          break;
        if (index > 0) {
          scanner.read(COMMA);
          // Allow an extraneous trailing comma
          if (scanner.readIf(SQCL) != null)
            break;
        }
        String expr = wrappedType().parseDefaultValue(scanner, classSpecificSource, null);
        parsedExpressions.add(expr);
      }
    }

    SourceBuilder sb = classSpecificSource;
    sb.a("  private static final ", typeName(), " ", fieldDef.constantName(), " = ", ParseTools.PKG_TOOLS,
        ".arrayList(");
    int index = INIT_INDEX;
    for (String expr : parsedExpressions) {
      index++;
      if (index > 0)
        sb.a(",");
      sb.a(expr);
    }
    sb.a(");").cr();

    return fieldDef.constantName();
  }

  private final DataType mWrappedType;
}
