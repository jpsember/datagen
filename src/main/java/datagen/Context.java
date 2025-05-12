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

import java.io.File;
import java.util.Set;

import datagen.gen.DatWithSource;
import datagen.gen.DatagenConfig;
import datagen.gen.Language;
import js.file.Files;

import static js.base.Tools.*;

/**
 * Class containing components used while processing a single .dat file
 */
public final class Context {

  public static DatagenConfig config;
  public static Files files;
  public static GeneratedTypeDef generatedTypeDef;
  public static DataTypeManager dataTypeManager;
  public static DatWithSource datWithSource;
  public static Set<File> generatedFilesSet;
  public static ParseTools pt;
  public static SqlGen sql;

  public static void prepare(Files files, DatagenConfig config) {
    discard();
    Context.pt = new ParseTools(config.language());
    Context.pt.prepare();
    Context.files = files;
    Context.config = config.build();
    Context.generatedFilesSet = hashSet();
    Context.sql = new SqlGen(config);
    Context.sql.prepare();
  }

  public static void prepare(DatWithSource entry) {
    discard();
    Context.datWithSource = entry;
    Context.dataTypeManager = new DataTypeManager();
  }

  @Deprecated
  public static boolean nonClassMode() {
    return false;
  }

  public static boolean debugMode() {
    return   !generatedTypeDef.isUnsafe();
  }

  /**
   * Discard some old elements
   */
  public static void discard() {
    generatedTypeDef = null;
    dataTypeManager = null;
    datWithSource = null;
  }

  public static File rustModFile(File generatedClassFile) {
    if (config.language() != Language.RUST)
      return null;
    return new File(Files.parent(generatedClassFile), "mod.rs");
  }

  public static boolean rust() {
    return pt.rust();
  }
  
  private Context() {
  }

}
