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

import datagen.gen.Language;
import datagen.gen.QualifiedName;

public final class Utils {

  public static final String GEN_SUBDIR_NAME = "gen";
  
  /**
   * Throw UnsupportedOperationException due to an unsupported target language
   */
  public static UnsupportedOperationException languageNotSupported(Object... messages) {
    throw notSupported(insertStringToFront("Language not supported:", messages));
  }

  public static Language language() {
    return Context.config.language();
  }

  public static boolean python() {
    return language() == Language.PYTHON;
  }

  public static String sourceFileExtension() {
    return sourceFileExtension(language());
  }

  public static String sourceFileExtension(Language language) {
    switch (language) {
    default:
      throw languageNotSupported();
    case JAVA:
      return "java";
    case PYTHON:
      return "py";
    }
  }

  /**
   * Get the language-specific expression for "null" (i.e., "None" if Python)
   */
  public static String nullExpr() {
    switch (Context.config.language()) {
    default:
      throw languageNotSupported();
    case PYTHON:
      return "None";
    case JAVA:
      return "null";
    }
  }

  public static boolean packageContainsElement(String packagePath, String element) {
    return ("." + packagePath + ".").contains("." + element + ".");
  }

  public static boolean packageContainsGen(String packagePath) {
    return packageContainsElement(packagePath,GEN_SUBDIR_NAME);
  }

  public static QualifiedName updateForPython(QualifiedName qualifiedName) {
    QualifiedName ret = qualifiedName;
    do {
      if (!python())
        break;

      // Append filename to package if appropriate; see PythonSourceGen.generateImports()
      // for a discussion.

      if (!packageContainsGen(qualifiedName.packagePath()))
        break;

      String pkgElement = "." + convertCamelToUnderscore(qualifiedName.className());
      if (qualifiedName.packagePath().endsWith(pkgElement)) {
        pr("*** package path seems to already include class name, which is unexpected:", INDENT,
            qualifiedName);
        break;
      }

      ret = qualifiedName.toBuilder().packagePath(qualifiedName.packagePath() + pkgElement);
      pr("modified qualifiedClassName from:", CR, qualifiedName, CR, "to:", CR, ret);
    } while (false);
    return ret.build();
  }

  @Deprecated
  public static void verifyPythonGenPath(String q) {
    alert("temporary(?) verification code");
    if (!python())
      return;
if (true) return;

    QualifiedName qn = ParseTools.parseQualifiedName(q, null);
    //  pr("...verifying for Python:", INDENT, qn);

    String packageName = qn.packagePath();
    String className = qn.className();

    todo("have special symbol and utilities for 'gen' package element");
    String pathExpr = "." + packageName + ".";
    String classExpr = "." + convertCamelToUnderscore(className) + ".";
    boolean pathContainsClassFile = packageContainsElement(pathExpr,classExpr); //pathExpr.contains(classExpr);
    boolean pathContainsGen = packageContainsGen(packageName);

    //    if (false) {
    //      pr("pathExpr:", pathExpr);
    //      pr("classExp:", classExpr);
    //      pr("pathCtCf:", pathContainsClassFile);
    //      pr("pathCtGn:", pathContainsGen);
    //    }

    if (pathContainsGen && !pathContainsClassFile) {
      die("unexpected Python qualified class name:", INDENT, qn);
    }
  }

}
