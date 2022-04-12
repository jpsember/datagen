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

import datagen.gen.DatWithSource;
import datagen.gen.DatagenConfig;
import datagen.gen.Language;
import datagen.gen.QualifiedName;
import js.file.Files;

/**
 * This is sort of a 'god class'...
 */
public final class Context {

  public DatagenConfig config;
  public Files files;
  public GeneratedTypeDef generatedTypeDef;
  public DataTypeManager dataTypeManager;
  public DatWithSource datWithSource;

  @Deprecated // This ought to be unnecessary if we structure things right, using subclassing where appropriate...?
  public boolean python() {
    return config.language() == Language.PYTHON;
  }

  @Deprecated // We should perhaps have a language-specific version of the Context class... or?
  public String constructImportExpression(QualifiedName nm) {
    if (python()) {
      return "from " + nm.packagePath() + " import " + nm.className();
    } else {
      return nm.combined();
    }
  }

  private Context() {
    loadTools();
  }

  public static Context SHARED_INSTANCE = new Context();

  /**
   * Construct a new shared instance
   */
  public static void prepare() {
    SHARED_INSTANCE = new Context();
  }
}
