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
import static datagen.Utils.*;
import static js.base.Tools.*;

import java.util.List;

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
        .parseQualifiedName("java.util.List<" + wrappedType.qualifiedClassName().className() + ">",null));
  }

  @Override
  protected String provideTypeName() {
    // We need to generate import statements for the wrapped type as well as the wrapper; but we don't want 
    // to confuse the macro substitution process 
    String wrappedImport = ParseTools.importCodeExpr(wrappedType().qualifiedClassName().combined(), "");
    String wrapperImport = ParseTools.PKG_LIST + "<" + wrappedType().qualifiedClassName().className() + ">";
    return wrappedImport + wrapperImport;
  }

  @Override
  public String provideSourceDefaultValue() {
    switch (language()) {
    default:
      throw languageNotSupported();
    case PYTHON:
      return "[]";
    case JAVA:
      return ParseTools.PKG_DATAUTIL + ".emptyList()";
    }
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    // We need special code to handle the case where user supplies None to a setter, and it's an optional list;
    // in that case, we don't want to attempt to construct None.copy()
    //
    if (python() && f.optional()) {
      s.a(targetExpr, " = x if x is None else ", sourceExpressionToMutable("x"));
      return;
    }
    super.sourceSetter(s, f, targetExpr);
  }

  /**
   * Constructs a mutable copy of a list. Note that while it creates a copy of
   * the list, it doesn't create copies of its elements; the references to those
   * elements are stored in the new list unchanged.
   */
  @Override
  public String sourceExpressionToMutable(String valueExpression) {
    if (python()) {
      return valueExpression + ".copy()";
    }
    todo("revisit whether we want to construct copies of things, and whether we want to enforce immutablility of compound types");
    return ParseTools.mutableCopyOfList(valueExpression);
  }

  @Override
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    if (python()) {
      if (fieldDef.optional()) {
        s.a("x = ", valueExpression, CR);
        s.a("if x is not None:", IN);
        s.a(targetExpression, " = ", valueExpression, ".copy()", OUT);
      } else {
        s.a(targetExpression, " = ", valueExpression, ".copy()");
      }
    } else
      s.a(targetExpression, " = ", ParseTools.immutableCopyOfList(valueExpression));
  }

  public DataType wrappedType() {
    return mWrappedType;
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    if (python()) {
      todo("!for Python, we may want to convert individual items for other types, as we are doing for enums");
      if (wrappedType() instanceof EnumDataType) {
        s.a("m[", f.nameStringConstant(true), "] = [x.value for x in self._", f.sourceName(), "]", CR);
      } else if (wrappedType() instanceof ContractDataType) {
        s.a("m[", f.nameStringConstant(true), "] = [x.to_json() for x in self._", f.sourceName(), "]", CR);
      } else
        s.a("m[", f.nameStringConstant(true), "] = self._", f.sourceName(), ".copy()", CR);
    } else {
      s.a(OPEN, //
          ParseTools.PKG_JSLIST, " j = new ", ParseTools.PKG_JSLIST, "();", CR, //
          "for (", wrappedType().typeName(), " x : m", f.sourceName(), ")", IN, //
          "j.add(", wrappedType().sourceGenerateSerializeToObjectExpression("x"), ");", OUT, //
          "m.put(", f.nameStringConstant(), ", j);", //
          CLOSE, CR);
    }
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
    if (python()) {
      s.a("for x in self._", f.sourceName(), ":", IN);
      s.a("if x is not None:", IN);
      s.a("r = r * 37 + hash(x)", OUT);
      s.a(OUT);
    } else {
      s.a("for (", wrappedType().typeName(), " x : m", f.sourceName(), ")", IN);
      s.a("if (x != null)", IN);
      s.a("r = r * 37 + x.hashCode();", OUT);
      s.a(OUT);
    }
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

    if (python()) {
      String constName = "DEF" + fieldDef.nameStringConstant(false);
      sb.a(constName, "  = [");
      int index = INIT_INDEX;
      for (String expr : parsedExpressions) {
        index++;
        if (index > 0) {
          sb.a(",");
        }
        sb.a(expr);
      }
      sb.a("]", CR);
      return constName;
    }

    String constName = "DEF_" + fieldDef.nameStringConstant();
    sb.a("  private static final ", typeName(), " ", constName, " = ", ParseTools.PKG_TOOLS, ".arrayList(");
    int index = INIT_INDEX;
    for (String expr : parsedExpressions) {
      index++;
      if (index > 0) {
        sb.a(",");
      }
      sb.a(expr);
    }
    sb.a(");").cr();

    return constName;
  }

  private final DataType mWrappedType;
}
