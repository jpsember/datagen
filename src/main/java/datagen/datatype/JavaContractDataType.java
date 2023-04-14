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
import static js.base.Tools.*;

import java.util.List;

import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.ParseTools;
import datagen.SourceBuilder;
import js.parsing.Scanner;
import js.parsing.Token;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public   class JavaContractDataType extends JavaDataType implements ContractDataType {

 
  @Override
  public  void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a("m.",getStoreInJsonMapMethodName(),"(", f.nameStringConstantQualified(), ", ",
        sourceGenerateSerializeToObjectExpression("" + f.instanceName()), ");");
    sourceEndIf(s).cr();
  }

  @Override
  public String provideSourceDefaultValue() {
    return ParseTools.importExprWithClassName(qualifiedClassName()) + ".DEFAULT_INSTANCE";
  }

  public String getConstructFromX() {
    return provideSourceDefaultValue() + ".parse(x)";
  }

  public String getSerializeDataType() {
    return "Object";
  }

  // Make this final for now to avoid unintended overriding
  @Override
  public final void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.open();
    if (!f.optional())
      s.a(f.instanceName(), " = ", f.defaultValueOrNull(), ";", CR);

    String typeExpr = getSerializeDataType();
    if (typeExpr.equals(ParseTools.PKG_JSMAP)) {
      s.a(ParseTools.PKG_JSMAP, " x = m.optJSMap(", f.nameStringConstantQualified(), ");", CR);
    } else if (typeExpr.equals(ParseTools.PKG_JSLIST)) {
      s.a(ParseTools.PKG_JSLIST, " x = m.optJSList(", f.nameStringConstantQualified(), ");", CR);
    } else {
      String castExpr = "m.optUnsafe";
      // No cast is necessary if the type is Object
      if (!typeExpr.equals(ParseTools.PKG_OBJECT))
        castExpr = "(" + typeExpr + ") " + castExpr;
      s.a(getSerializeDataType(), " x = ", castExpr, "(", f.nameStringConstantQualified(), ");", CR);
    }
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
    s.a(f.instanceName(), " = ", ParseTools.PKG_DATAUTIL, ".parseListOfObjects(", typeName(),
        ".DEFAULT_INSTANCE, m.optJSList(", f.nameStringConstantQualified(), "), ", f.optional(), ");");
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
    s.a(targetExpr, " = ", "(x == null) ? ", f.defaultValueOrNull(), " : x.build()");
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    return provideSourceDefaultValue() + ".parse((JSMap) " + jsonValue + ")";
  }

  @Override
  public String parseDefaultValue(Scanner scanner, SourceBuilder sb, FieldDef fieldDef) {

    // Attempt to infer from the tokens how to parse a default value

    Token t = scanner.peek();

    // If it looks like [ ...., ...]
    //
    // then scan a sequence of comma-delimited strings, and pass as arguments to a constructor
    // 
    if (t.id(SQOP)) {
      scanner.read();
      List<String> exprs = arrayList();
      boolean commaExp = false;
      while (scanner.readIf(SQCL) == null) {
        if (commaExp) {
          scanner.read(COMMA);
          commaExp = false;
          continue;
        }
        exprs.add(scanner.read().text());
        commaExp = true;
      }

      sb.a("  private static final ", fieldDef.dataType().typeName(), " ", fieldDef.constantName(),
          "  = new ", typeName(), "(");
      int i = INIT_INDEX;
      for (String expr : exprs) {
        i++;
        if (i > 0)
          sb.a(", ");
        sb.a(expr);
      }
      sb.a(");", CR);
      return fieldDef.constantName();
    }
    throw notSupported("can't parse default value for token:", t);
  }

}
