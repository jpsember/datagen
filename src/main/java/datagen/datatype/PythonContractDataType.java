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
import datagen.PythonDataType;
import datagen.SourceBuilder;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class PythonContractDataType extends PythonDataType {

  @Override
  public String provideSourceDefaultValue() {
    return Context.pt.importExprWithClassName(qualifiedName()) + ".default_instance";
  }

  public String getConstructFromX() {
    todo("!this won't work for non-primitive data types");
    return "x.copy()";
  }

  // Make this final for now to avoid unintended overriding
  @Override
  public final void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    // If there is a value for this key, use the type's default instance to parse that value
    // and store that parsed value.
    // Otherwise, if there is no value, leave the current value alone (which may be None, e.g. if value is optional)
    //
    s.a("x = obj.get(", f.nameStringConstantQualified(), ")", CR);
    sourceIfNotNull(s, "x");
    s.a("inst.", f.instanceName(), " = ", pythonDeserializeExpr(f, "x"));
    sourceEndIf(s);
  }

  /**
   * Get Python expression to deserialize a (json/dict) value
   */
  private String pythonDeserializeExpr(FieldDef f, String expr) {
    return f.defaultValueSource() + ".parse(" + expr + ")";
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("inst.", f.instanceName(), " = ", Context.pt.PKGPY_DATAUTIL, ".parse_list_of_objects(", typeName(),
        ".default_instance, obj.get(", f.nameStringConstantQualified(), "), ",
        f.optional() ? "True" : "False", ")");
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return getSerializeToJSONValue(valueExpression);
  }

  @Deprecated
  public String getSerializeToJSONValue(String value) {
    return value + ".to_json()";
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    if (!f.optional()) {
      s.a("if x is None:", OPEN);
      s.a("x = ", f.defaultValueOrNull(), CLOSE);
      s.a(targetExpr, " = x.build()");
    } else {
      sourceIfNotNull(s, "x");
      s.a("x = x.build()");
      sourceEndIf(s);
      s.a(targetExpr, " = x");
    }
  }

  //  @Override
  //  public String parseDefaultValue(Scanner scanner, SourceBuilder sb, FieldDef fieldDef) {
  //
  //    // Attempt to infer from the tokens how to parse a default value
  //
  //    Token t = scanner.peek();
  //
  //    // If it looks like [ ...., ...]
  //    //
  //    // then scan a sequence of comma-delimited strings, and pass as arguments to a constructor
  //    // 
  //    if (t.id(SQOP)) {
  //      scanner.read();
  //      List<String> exprs = arrayList();
  //      boolean commaExp = false;
  //      while (scanner.readIf(SQCL) == null) {
  //        if (commaExp) {
  //          scanner.read(COMMA);
  //          commaExp = false;
  //          continue;
  //        }
  //        exprs.add(scanner.read().text());
  //        commaExp = true;
  //      }
  //
  //      String constName;
  //      constName = "DEF" + fieldDef.nameStringConstantUnqualified();
  //      sb.a(constName, "  = ", typeName(), "(");
  //      int i = INIT_INDEX;
  //      for (String expr : exprs) {
  //        i++;
  //        if (i > 0)
  //          sb.a(", ");
  //        sb.a(expr);
  //      }
  //      sb.a(")", CR);
  //
  //      return constName;
  //    }
  //    throw notSupported("can't parse default value for token:", t);
  //  }

}
