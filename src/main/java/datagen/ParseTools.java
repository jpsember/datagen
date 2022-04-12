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
package datagen;

import static js.base.Tools.*;

import java.util.List;
import java.util.regex.Pattern;

import datagen.gen.Language;
import datagen.gen.QualifiedName;
import js.file.Files;
import js.parsing.DFA;
import js.parsing.MacroParser;
import js.parsing.RegExp;
import static datagen.Utils.*;

public final class ParseTools {

  public static final boolean SHOW_STACK_TRACES = false && alert("showing full stack traces");

  public static final String EXT_DATA_DEFINITION = "dat";
  public static final String DOT_EXT_DATA_DEFINITION = "." + EXT_DATA_DEFINITION;

  // Have the compiler calculate the sequence of values
  private static int x = 0;

  public static final int WS = x++;
  public static final int STRING = x++;
  public static final int EQUALS = x++;
  public static final int ID = x++;
  public static final int NUMBER = x++;
  public static final int SEMI = x++;
  public static final int BROP = x++;
  public static final int BRCL = x++;
  public static final int SQOP = x++;
  public static final int SQCL = x++;
  public static final int COMMA = x++;
  public static final int COLON = x++;
  public static final int BOOL = x++;
  public static final int EXCLAIM = x++;
  public static final int FIELDS = x++;
  public static final int ENUM = x++;
  public static final int OPTIONAL = x++;
  public static final int REPEATED = x++;
  public static final int MAP = x++;
  public static final int SET = x++;
  public static final int EXTERN = x++;

  private static DFA sDFA;

  public static DFA dfa() {
    if (sDFA == null) {
      sDFA = new DFA(Files.readString(Datagen.class, "tokens.dfa"));
    }
    return sDFA;
  }

  public static float parseFloatValue(String numberString) {
    try {
      float value = Float.parseFloat(numberString);
      return value;
    } catch (Throwable t) {
      throw badArg("expected a float, not:", quote(numberString));
    }
  }

  public static double parseDoubleValue(String numberString) {
    try {
      double value = Double.parseDouble(numberString);
      return value;
    } catch (Throwable t) {
      throw badArg("expected a double, not:", quote(numberString));
    }
  }

  public static QualifiedName parseQualifiedName(String expr) {
    return parseQualifiedName(expr, null);
  }

  public static QualifiedName parseQualifiedName(String expr, String defaultPackage) {
    int nameStartPos = expr.lastIndexOf('.');
    String pkg = expr.substring(0, Math.max(0, nameStartPos));
    pkg = ifNullOrEmpty(pkg, nullToEmpty(defaultPackage));
    String className = expr.substring(1 + nameStartPos);
    return buildQualifiedName(pkg, className);
  }

  public static QualifiedName buildQualifiedName(String packagePath, String typeName) {
    String combined = typeName;
    if (!packagePath.isEmpty())
      combined = packagePath + "." + typeName;
    return QualifiedName.newBuilder()//
        .packagePath(packagePath)//
        .className(typeName)//
        .combined(combined)//
        .build();
  }

  public static String escapeJavaString(String s) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
      case '"':
        b.append('\\');
        b.append(c);
        break;
      case '\n':
        b.append('\\');
        b.append('n');
        break;
      default:
        if (c < 20)
          throw die("unsupported character at: " + s.substring(i));
        b.append(c);
        break;
      }
    }
    return b.toString();
  }

  public static String adjustLinefeeds(String content, Language language) {
    List<String> lines = arrayList();
    int cursor = 0;
    while (cursor < content.length()) {
      int n = content.indexOf('\n', cursor);
      if (n < 0)
        n = content.length();
      String line = content.substring(cursor, n);
      // Strip any *trailing* whitespace
      line = ("|" + line).trim().substring(1);
      lines.add(line);
      cursor = n + 1;
    }

    StringBuilder sb = new StringBuilder();

    switch (language) {
    default:
      throw languageNotSupported();
    case JAVA: {
      int blanks = 1;
      for (String line : lines) {
        if (line.isEmpty()) {
          if (blanks == 0)
            sb.append('\n');
          blanks++;
        } else {
          blanks = 0;
          sb.append(line);
          sb.append('\n');
        }
      }
    }
      break;
    case PYTHON: {
      // todo("!this can be simplified no doubt");
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
          for (int i = 0; i < blankRequest; i++) {
            sb.append('\n');
          }
        }
        blankRequest = 0;
        sb.append(line);
        sb.append('\n');
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

  public static String importExpression(String importText, String sourceText) {
    return "{{" + importText + "|" + sourceText + "}}";
  }

  private static String javaImportExpression(String arg) {
    int cursor = arg.lastIndexOf('.');
    checkArgument(cursor > 0);
    return importExpression(arg, arg.substring(cursor + 1));
  }

  private static String pythonImportExpression(String arg) {
    int cursor = arg.lastIndexOf(' ');
    checkArgument(cursor > 0);
    return importExpression(arg, arg.substring(cursor + 1));
  }

  // Qualified class names for some of my data types (and Java's), in case they change or get substituted in future
  //
  public static final String PKG_TOOLS = javaImportExpression("js.base.Tools");
  public static final String PKG_DATAUTIL = javaImportExpression("js.data.DataUtil");
  public static final String PKG_JSMAP = javaImportExpression("js.json.JSMap");
  public static final String PKG_JSLIST = javaImportExpression("js.json.JSList");
  public static final String PKG_FILES = javaImportExpression("js.file.Files");
  public static final String PKG_LIST = javaImportExpression("java.util.List");
  public static final String PKG_MAP = javaImportExpression("java.util.Map");
  public static final String PKG_CONCURRENT_MAP = javaImportExpression(
      "java.util.concurrent.ConcurrentHashMap");
  public static final String PKG_STRING = javaImportExpression("java.lang.String");
  public static final String PKG_ARRAYS = javaImportExpression("java.util.Arrays");
  public static final String PKG_ARRAYLIST = javaImportExpression("java.util.ArrayList");
  public static final String PKG_JSOBJECT = javaImportExpression("js.json.JSObject");
  public static final String PKG_MUTABLELIST = "new " + PKG_ARRAYLIST + "<>()";
  public static final String PKG_SHORT_ARRAY = javaImportExpression("js.data.ShortArray");
  public static final String PKG_FLOAT_ARRAY = javaImportExpression("js.data.FloatArray");
  public static final String PKG_DOUBLE_ARRAY = javaImportExpression("js.data.DoubleArray");

  public static final String PKGPY_DATAUTIL = pythonImportExpression("from pycore.datautil import DataUtil");

  public static Pattern IMPORT_REGEXP = RegExp.pattern("\\{\\{([^\\}]*)\\}\\}");

  public static final String immutableCopyOfList(String expr) {
    return PKG_DATAUTIL + ".immutableCopyOf(" + expr + ")";
  }

  public static final String mutableCopyOfList(String expr) {
    return PKG_DATAUTIL + ".mutableCopyOf(" + expr + ")";
  }

  public static final String mutableCopyOfMap(String expr) {
    return PKG_DATAUTIL + ".mutableCopyOf(" + expr + ")";
  }

  public static final String immutableCopyOfMap(String expr) {
    return PKG_DATAUTIL + ".mutableCopyOf(" + expr + ")";
  }

}
