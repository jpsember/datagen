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

  public  String verifyImportExpr(String importExpr) {
    if (python() && alert("temporary method to verify things")
        && !(importExpr.startsWith("java") || importExpr.startsWith("js"))
        && !(importExpr.startsWith("from") || importExpr.startsWith("impor"))) {
      throw badArg("import expr:", importExpr);
    }
    return importExpr;
  }

  public String constructImportExpression(QualifiedName nm) {
    if (python()) {
      pr(".........returning 'from' expr");
      return "from " + nm.packagePath() + " import " + nm.className();
    } else
      return nm.combined();
  }

  public void includeImport(String importExpr) {
    verifyImportExpr(importExpr);
    mImportedClasses.add(importExpr);
  }

  public Set<String> getImports() {
    return mImportedClasses;
  }

  private final Set<String> mImportedClasses = hashSet();

}
