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

import java.io.File;
import java.util.Set;

import datagen.gen.DatagenConfig;
import datagen.gen.Language;
import js.file.Files;

import static js.base.Tools.*;
import static datagen.Utils.*;

/**
 * Class containing components used while processing a single .dat file
 * (no longer true due to issue 54)
 */
public final class Context {

  public static DatagenConfig config;
  public static Files files;
  public static GeneratedTypeDef generatedTypeDef;
  public static DataTypeManager mDataTypeManager;
  public static Set<File> generatedFilesSet;
  public static ParseTools pt;
  public static SqlGen sql;
  private static File mDatDirectoryRel;

  public static DataTypeManager dataTypeManager() {
    return mDataTypeManager;
  }


  public static File datDirectoryRelative() {
    checkNotNull(mDatDirectoryRel, "datDirectoryRelative");
    return mDatDirectoryRel;
  }

  public static String sourceRelPath() {
    return checkNonEmpty(mSourceRelPath, "sourceRelPath");
  }


//  @Deprecated
//  public static DatWithSource datWithSource() {
//    todo("only called from two locations");
//    checkNotNull(mDatWithSource  , "Context.datWithSource");
//    return mDatWithSource;
//  }

  // ----------------------------------------------------------------------------------------------
  // Lifetimes
  // ----------------------------------------------------------------------------------------------

  public static void prepareApp(Files files, DatagenConfig config) {
    todo("!this needs some thinking given that a .dat file now might contain multiple classes, and external references");
    discardClassOrEnum();
    Context.pt = new ParseTools(config.language());
    Context.pt.prepare();
    Context.files = files;
    Context.config = config.build();
    Context.generatedFilesSet = hashSet();
    Context.sql = new SqlGen(config);
    Context.sql.prepare();

    var b = config.toBuilder();

    b.datPath(Files.absolute(b.datPath()));
    Context.config = b.build();

//    DatagenConfig config = datagenConfig();
//    File datRoot = config.datPath();
    p54("prepareApp; dat_path:", INDENT, Context.config.datPath());
  }


  public static void prepareDir(File dir) {
    discardDir();
    todo("!we need to initialize a relative directory here or some such");
    Context.mDataTypeManager = new DataTypeManager();

//    checkArgument(Files.nonEmpty(config.datPath()));

    p54("prepareDir; dir:", dir);
//    p54("config.datPath:", config.datPath());

    Files.assertRelative(dir, "expected relative dir");
    mDatDirectoryRel = dir; //Files.relativeToContainingDirectory(dir, config.datPath());
    p54("datDirectoryRel:", INDENT, Files.infoMap(mDatDirectoryRel));
  }

  public static void discardDir() {
    mDataTypeManager = null;
    mDatDirectoryRel = null;
  }

  public static void prepareForClassOrEnumDefinition(String sourceRelPath) {
    //DatWithSource entry
    discardClassOrEnum();
    p54("Context.prepare, sourceRelPath:", INDENT, sourceRelPath);
    mSourceRelPath = checkNonEmpty(sourceRelPath, "sourceRelPath");
  }

  /**
   * Discard some old elements
   */
  public static void discardClassOrEnum() {
    generatedTypeDef = null;
    mSourceRelPath = null;
  }


  // ----------------------------------------------------------------------------------------------
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


  private static String mSourceRelPath;

}
