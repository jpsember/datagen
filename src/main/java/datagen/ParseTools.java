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
package datagen;

import static js.base.Tools.*;

import java.util.List;
import java.util.regex.Pattern;

import datagen.gen.Language;
import js.file.Files;
import js.json.JSMap;
import js.parsing.DFA;
import js.parsing.MacroParser;
import js.parsing.RegExp;

import static datagen.Context.*;

public final class ParseTools {

  /**
   * The 'constants' PKG_xxx call various things, including (indirectly) other
   * ParseTools methods via the Context.pt field; so we defer the initialization
   * of these constants until *after* the ParseTools constructor has run (so the
   * Context.pt field has been given a value).
   */
  public ParseTools() {
    PKG_TOOLS = javaClassExpr("js.base.Tools");
    PKG_DATAUTIL = javaClassExpr("js.data.DataUtil");
    PKG_JSMAP = javaClassExpr("js.json.JSMap");
    PKG_JSLIST = javaClassExpr("js.json.JSList");
    PKG_FILES = javaClassExpr("js.file.Files");
    PKG_LIST = javaClassExpr("java.util.List");
    PKG_MAP = javaClassExpr("java.util.Map");
    PKG_SET = javaClassExpr("java.util.Set");
    PKG_HASH_SET = javaClassExpr("java.util.HashSet");
    PKG_CONCURRENT_MAP = javaClassExpr("java.util.concurrent.ConcurrentHashMap");
    PKG_STRING = javaClassExpr("java.lang.String");
    PKG_OBJECT = javaClassExpr("java.lang.Object");
    PKG_ARRAYS = javaClassExpr("java.util.Arrays");
    PKG_ARRAYLIST = javaClassExpr("java.util.ArrayList");
    PKG_JSOBJECT = javaClassExpr("js.json.JSObject");
    PKG_SHORT_ARRAY = javaClassExpr("js.data.ShortArray");
    PKG_FLOAT_ARRAY = javaClassExpr("js.data.FloatArray");
    PKG_DOUBLE_ARRAY = javaClassExpr("js.data.DoubleArray");

    PKGPY_DATAUTIL = pythonClassExpr("pycore.datautil.DataUtil");
    PKGPY_LIST = pythonClassExpr("typing.List");

    PKGGO_TOOLS = "{{github.com/jpsember/golang-base/base.DataClass|}}";
    PKGGO_DATA = PKGGO_TOOLS;
    PKGGO_JSON = PKGGO_TOOLS;
    PKGGO_FILE = PKGGO_TOOLS;

    PKG_RUST_TOOLS = "{{crate.tools.*|}}";
    PKG_RUST_JSON = "{{crate.json.*|}}";
    PKG_RUST_ERROR = "{{std.error|}}";
  }

  public static String goModuleExpr(String s) {
    return "github.com/jpsember/golang-base/base." + s;
  }

  public static String goModuleExprq(String s) {
    return quote(goModuleExpr(s));
  }

  public static final boolean SHOW_STACK_TRACES = DEBUG_RUST && alert("!showing full stack traces");

  // Token Ids generated by 'dev dfa' tool (DO NOT EDIT BELOW)
  public static final int WS = 0;
  public static final int STRING = 1;
  public static final int EQUALS = 2;
  public static final int ID = 3;
  public static final int NUMBER = 4;
  public static final int SEMI = 5;
  public static final int BROP = 6;
  public static final int BRCL = 7;
  public static final int SQOP = 8;
  public static final int SQCL = 9;
  public static final int PAROP = 10;
  public static final int PARCL = 11;
  public static final int COMMA = 12;
  public static final int COLON = 13;
  public static final int BOOL = 14;
  public static final int EXCLAIM = 15;
  public static final int OPTIONAL = 16;
  public static final int REPEATED = 17;
  public static final int DEPRECATION = 18;
  public static final int RESERVEDWORD = 19;
  // End of token Ids generated by 'dev dfa' tool (DO NOT EDIT ABOVE)

  private static DFA sDFA;

  public static DFA dfa() {
    if (sDFA == null) {
      sDFA = DFA.parse(Files.readString(Datagen.class, "tokens.dfa"));
    }
    return sDFA;
  }

  /**
   * Reformat lines to eliminate trailing whitespace and to adjust the number of
   * blank lines
   *
   * For Python, lines starting with \\n are interpreted as a request for n
   * blank lines (if n is omitted, it is assumed n = 1)
   */
  public String adjustLinefeeds(String content) {
    List<String> lines = arrayList();
    int cursor = 0;
    while (cursor < content.length()) {
      int n = content.indexOf('\n', cursor);
      if (n < 0)
        n = content.length();
      String line = content.substring(cursor, n);
      // Strip any *trailing* whitespace
      lines.add(trimRight(line));
      cursor = n + 1;
    }

    StringBuilder sb = new StringBuilder();

    switch (language()) {
      default:
        throw languageNotSupported();

      case JAVA:
      case GO:
      case RUST: {
        int blanks = 1;
        for (String line : lines) {
          if (line.isEmpty()) {
            if (blanks == 0)
              addCr(sb);
            blanks++;
          } else {
            blanks = 0;
            sb.append(line);
            addCr(sb);
          }
        }
      }
      break;
      case PYTHON: {
        int blankRequest = 0;
        for (String line : lines) {
          String trimmed = line.trim();
          if (trimmed.isEmpty())
            continue;
          final String prefix = "\\\\";
          if (trimmed.startsWith(prefix)) {
            String s = ifNullOrEmpty(chompPrefix(trimmed, prefix), "1");
            blankRequest = Integer.parseInt(s);
            continue;
          }
          if (sb.length() != 0) {
            for (int i = 0; i < blankRequest; i++)
              addCr(sb);
          }
          blankRequest = 0;
          sb.append(line);
          addCr(sb);
        }
      }
      break;
    }

    return sb.toString();
  }

  public static String processOptionalComments(String content, boolean includeComments) {
    MacroParser p = new MacroParser();
    p.withPattern(RegExp.pattern("@@([^\\x0a\\x0d]*)"));
    p.withTemplate(content);
    String result = p.content((key) -> {
      if (includeComments)
        return key;
      return "";
    });
    return result;
  }

  /**
   * Wrap a class name and some source code in delimeters to the class name is
   * imported, and the source code is generated (which can be an empty string)
   */
  public static String importExprWithCode(String qualifiedClassName, String sourceCode) {
    // If the qualified class name already starts with "{{", we're attempting to nest them; that's bad
    if (qualifiedClassName.startsWith("{{")) {
      pr("attempting to nest importExprWithCode; qualifiedClassName:", INDENT, qualifiedClassName, CR,
          "sourceCode:", INDENT, sourceCode);
      badArg("attempting to nest importExprWithCode; qualifiedClassName:", INDENT, qualifiedClassName, CR,
          "sourceCode:", INDENT, sourceCode);
    }
    return "{{" + qualifiedClassName + "|" + sourceCode + "}}";
  }

  public String importExprWithClassName(QualifiedName qualifiedName) {
    // Python primitive types (e.g. int) won't have packages, so don't wrap them in import expressions
    if (qualifiedName.packagePath().isEmpty())
      return qualifiedName.className();
    return importExprWithCode(qualifiedName.combined(), qualifiedName.className());
  }

  private Object javaClassExpr(String qualifiedClassName) {
    if (language() != Language.JAVA)
      return null;
    return importedClassExpr(qualifiedClassName);
  }

  private Object pythonClassExpr(String qualifiedClassName) {
    if (language() != Language.PYTHON)
      return null;
    return importedClassExpr(qualifiedClassName);
  }

  /**
   * Wrap a class name in delimeters so the class is imported, and the class
   * name (without its package) is generated
   */
  public static Object importedClassExpr(String classExpression) {
    QualifiedName qn = QualifiedName.parse(classExpression);
    return importExprWithCode(qn.combined(), qn.className());
  }

  public Object PKG_TOOLS, PKG_DATAUTIL, PKG_JSMAP, PKG_JSLIST, PKG_FILES, PKG_LIST, PKG_MAP, PKG_SET,
      PKG_HASH_SET, PKG_CONCURRENT_MAP, PKG_STRING, PKG_OBJECT, PKG_ARRAYS, PKG_ARRAYLIST, PKG_JSOBJECT,
      PKG_SHORT_ARRAY, PKG_FLOAT_ARRAY, PKG_DOUBLE_ARRAY;

  public Object PKGPY_DATAUTIL, PKGPY_LIST;

  public Object PKGGO_TOOLS, PKGGO_JSON, PKGGO_DATA, PKGGO_FILE;

  public Object PKG_RUST_TOOLS;
  public Object PKG_RUST_JSON;
  public Object PKG_RUST_ERROR;

  public static Pattern IMPORT_REGEXP = RegExp.pattern("\\{\\{([^\\}]*)\\}\\}");

}
