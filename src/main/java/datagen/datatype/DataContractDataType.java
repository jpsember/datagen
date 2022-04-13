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
import static datagen.Utils.*;

import java.util.List;

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.ParseTools;
import datagen.SourceBuilder;
import datagen.gen.Language;
import datagen.gen.QualifiedName;
import js.data.DataUtil;
import js.parsing.Scanner;
import js.parsing.Token;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class DataContractDataType extends DataType {

  @Override
  public String provideSourceDefaultValue() {
    switch (language()) {
    default:
      throw languageNotSupported();
    case PYTHON:
      todo("I suspect there is a problem with the parentheses here");
      return ParseTools.importExpression(constructImportExpression(),
          qualifiedClassName().className() + ".default_instance");
    case JAVA:
      return ParseTools.importExpression(constructImportExpression(), qualifiedClassName().className())
          + ".DEFAULT_INSTANCE";
    }
  }

  public String getConstructFromX() {
    if (python()) {
      todo("!this won't work for non-primitive data types");
      return "x.copy()";
    } else
      return provideSourceDefaultValue() + ".parse(x)";
  }

  public String getSerializeDataType() {
    return "Object";
  }

  // Make this final for now to avoid unintended overriding
  @Override
  public final void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {

    if (python()) {

      // If there is a value for this key, use the type's default instance to parse that value
      // and store that parsed value.
      // Otherwise, if there is no value, leave the current value alone (which may be None, e.g. if value is optional)
      //
      s.a("x = obj.get(", f.nameStringConstant(), ")", CR);
      s.a("if x is not None:", IN);
      s.a("inst._", f.sourceName(), " = ", pythonDeserializeExpr(f, "x"), OUT);
      return;
    }

    s.open();
    if (!f.optional())
      s.a("m", f.sourceName(), " = ", f.defaultValueOrNull(), ";", CR);

    String typeExpr = getSerializeDataType();
    if (typeExpr.equals(ParseTools.PKG_JSMAP)) {
      s.a(ParseTools.PKG_JSMAP, " x = m.optJSMap(", f.nameStringConstant(), ");", CR);
    } else if (typeExpr.equals(ParseTools.PKG_JSLIST)) {
      s.a(ParseTools.PKG_JSLIST, " x = m.optJSList(", f.nameStringConstant(), ");", CR);
    } else {
      String castExpr = "m.optUnsafe";
      if (!typeExpr.equals("Object"))
        castExpr = "(" + typeExpr + ") " + castExpr;
      s.a(getSerializeDataType(), " x = ", castExpr, "(", f.nameStringConstant(), ");", CR);
    }
    s.a("if (x != null)", OPEN, //
        "m", f.sourceName(), " = ", getConstructFromX(), ";", CLOSE //
    );

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
    if (python()) {
      s.a("inst._", f.sourceName(), " = ", ParseTools.PKGPY_DATAUTIL, ".parse_list_of_objects(", typeName(),
          ".default_instance, obj.get(", f.nameStringConstant(), "), ", f.optional() ? "True" : "False", ")");
      return;
    }
    s.a("m", f.sourceName(), " = ", ParseTools.PKG_DATAUTIL, ".parseListOfObjects(", typeName(),
        ".DEFAULT_INSTANCE, m.optJSList(", f.nameStringConstant(), "), ", f.optional(), ");");
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return getSerializeToJSONValue(valueExpression);
  }

  public String getSerializeToJSONValue(String value) {
    if (python())
      return value + ".to_json()";
    else
      return value + ".toJson()";
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    if (python()) {
      if (!f.optional()) {
        s.a("if x is None:", OPEN);
        s.a("x = ", f.defaultValueOrNull(), CLOSE);
        s.a(targetExpr, " = x.build()");
      } else {
        s.a("if x is not None:", OPEN);
        s.a("x = x.build()", CLOSE);
        s.a(targetExpr, " = x");
      }
      return;
    }
    String defaultValue = f.defaultValueOrNull();
    s.a(targetExpr, " = ", "(x == null) ? ", defaultValue, " : x.build();");
  }

  @Override
  public String deserializeJsonToJavaValue(String jsonValue) {
    return provideSourceDefaultValue() + ".parse((JSMap) " + jsonValue + ")";
  }

  public void parseQualifiedName(String typeName) {
    // We may not yet have a generated type to provide a default package
    String defaultPackageName = null;
    if (Context.config.language() == Language.PYTHON)
      defaultPackageName = "pycore";

    if (Context.generatedTypeDef != null) {
      defaultPackageName = Context.generatedTypeDef.packageName();
      if (Context.config.language() == Language.PYTHON) {
        // If typeName is Xyz, make sure default package ends with xyz, adding if necessary
        String packageSuff = "." + DataUtil.convertCamelCaseToUnderscores(typeName);
        if (!("." + defaultPackageName).endsWith(packageSuff)) {
          defaultPackageName = defaultPackageName + packageSuff;
        }
      }
    }

    QualifiedName qn = ParseTools.parseQualifiedName(typeName, defaultPackageName);
    setQualifiedClassName(qn);
  }

  @Override
  public String parseDefaultValue(Scanner scanner, SourceBuilder sb, FieldDef fieldDef) {
    if (!python())
      return super.parseDefaultValue(scanner, sb, fieldDef);

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

      String constName;
      if (python()) {
        constName = "DEF" + fieldDef.nameStringConstant(false);
        sb.a(constName, "  = ", typeName(), "(");
        int i = INIT_INDEX;
        for (String expr : exprs) {
          i++;
          if (i > 0)
            sb.a(", ");
          sb.a(expr);
        }
        sb.a(")", CR);
      } else {
        constName = "DEF_" + fieldDef.nameStringConstant();
        sb.a("  private static final ", fieldDef.dataType().typeName(), " ", constName, "  = new ",
            typeName(), "(");
        int i = INIT_INDEX;
        for (String expr : exprs) {
          i++;
          if (i > 0)
            sb.a(", ");
          sb.a(expr);
        }
        sb.a(");", CR);
      }
      return constName;
    }
    throw notSupported("can't parse default value for token:", t);
  }

}
