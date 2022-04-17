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

import datagen.DataType;
import datagen.FieldDef;
import datagen.ParseTools;
import datagen.PythonDataType;
import datagen.SourceBuilder;
import js.parsing.Scanner;

public class PythonListDataType extends PythonDataType {

  public PythonListDataType(DataType wrappedType) {
    mWrappedType = wrappedType;
    setQualifiedClassName(ParseTools
        .parseQualifiedName("java.util.List<" + wrappedType.qualifiedClassName().className() + ">", null));
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
    return "[]";
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    // We need special code to handle the case where user supplies None to a setter, and it's an optional list;
    // in that case, we don't want to attempt to construct None.copy()
    //
    if (f.optional()) {
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
    todo(
        "revisit whether we want to construct copies of things, and whether we want to enforce immutablility of compound types");
    return valueExpression + ".copy()";
  }

  @Override
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    if (fieldDef.optional()) {
      s.a("x = ", valueExpression, CR);
      s.a("if x is not None:", IN);
      s.a(targetExpression, " = ", valueExpression, ".copy()", OUT);
    } else {
      s.a(targetExpression, " = ", valueExpression, ".copy()");
    }
  }

  public DataType wrappedType() {
    return mWrappedType;
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    todo("!for Python, we may want to convert individual items for other types, as we are doing for enums");
    if (wrappedType() instanceof EnumDataType) {
      s.a("m[", f.nameStringConstantQualified(), "] = [x.value for x in self._", f.sourceName(), "]", CR);
    } else if (wrappedType() instanceof ContractDataType) {
      s.a("m[", f.nameStringConstantQualified(), "] = [x.to_json() for x in self._", f.sourceName(), "]", CR);
    } else
      s.a("m[", f.nameStringConstantQualified(), "] = self._", f.sourceName(), ".copy()", CR);
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
    s.a("for x in self._", f.sourceName(), ":", IN);
    s.a("if x is not None:", IN);
    s.a("r = r * 37 + hash(x)", OUT);
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

    String constName = "DEF" + fieldDef.nameStringConstantUnqualified();
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
    return sb.toString();
  }

  private final DataType mWrappedType;
}
