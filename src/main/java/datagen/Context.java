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

import java.util.Set;

import datagen.gen.DatWithSource;
import datagen.gen.DatagenConfig;
import datagen.gen.Language;
import datagen.gen.QualifiedName;
import js.file.Files;

public final class Context {

  public static final boolean WTF = true && alert("investigating a nasty bug");

  public static boolean sJava;
  public DatagenConfig config;
  public Files files;
  public GeneratedTypeDef generatedTypeDef;
  public DataTypeManager dataTypeManager;
  public DatWithSource datWithSource;

  public boolean python() {
    return language() == Language.PYTHON;
  }

  public Language language() {
    return config.language();
  }

  public String constructImportExpression(QualifiedName nm) {
    if (python()) {
      Context.assertNotJava();
      String imptExpr = "from " + nm.packagePath() + " import " + nm.className();
      if (WTF)
        pr("constructed impt expr:", imptExpr);
      return imptExpr;
    } else {
      return nm.combined();
    }
  }

  private static void assertNotJava() {
    if (sJava)
      throw die("sJava is true");
  }

  public void includeImport(String importExpr) {
    boolean wasNew = mImportedClasses.add(importExpr);
    if (WTF && wasNew)
      pr("...included import:", importExpr, "size:", mImportedClasses.size());
    checkWTF(importExpr);
  }

  public Set<String> getImports() {
    return mImportedClasses;
  }

  private final Set<String> mImportedClasses = hashSet();

  public static void checkWTF(String importText) {
    if (!WTF)
      return;
    if (!sJava)
      return;
    if (importText.contains("from"))
      die("checkWTF failed with:", importText);
  }

}
