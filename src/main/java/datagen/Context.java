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
import js.file.Files;

import static js.base.Tools.*;
import static datagen.Utils.*;

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

  public static File sourceRelPath() {
    return mSourceRelPath;
  }

  // ----------------------------------------------------------------------------------------------
  // Lifetimes
  // ----------------------------------------------------------------------------------------------

  public static void prepareApp(Files files, DatagenConfig config) {
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

    p54("prepareApp; dat_path:", INDENT, Context.config.datPath());

    sModules = null;
    if (Context.rust())
      sModules = new RustModuleMgr();
  }

  public static void prepareDir(File dir) {
    discardDir();
    mDataTypeManager = new DataTypeManager();
    p54("prepareDir; dir:", dir);
    Files.assertRelative(dir, "expected relative dir");
    mDatDirectoryRel = dir;
    p54("datDirectoryRel:", INDENT, Files.infoMap(mDatDirectoryRel));
  }

  public static void discardDir() {
    mDataTypeManager = null;
    mDatDirectoryRel = null;
  }

  public static void prepareForClassOrEnumDefinition(File sourceRelPath) {
    discardClassOrEnum();
    p54("Context.prepare, sourceRelPath:", INDENT, sourceRelPath);
    mSourceRelPath = sourceRelPath;
  }

  /**
   * Discard some old elements
   */
  public static void discardClassOrEnum() {
    generatedTypeDef = null;
    mSourceRelPath = null;
  }

  public static void flushRustModules() {
    if (sModules != null)
      sModules.flushRustModules();
  }

  public static void updateRustModule(File sourceRelPath) {
    todo("we could refactor this by keeping track of list of source files generated");
    if (sModules != null)
      sModules.updateRustModules(sourceRelPath);
  }


  // ----------------------------------------------------------------------------------------------

  public static boolean rust() {
    return pt.rust();
  }

  private Context() {
  }

  private static File mSourceRelPath;
  private static RustModuleMgr sModules;

  public static void cleanIfNecessary(File dir) {
    if (!config.clean())
      return;
    var genDirectory = determineGenDirectory(dir);
    if (sCleanedDirectorySet.add(genDirectory)) {
      if (genDirectory.exists()) {
        Files.S.deleteDirectory(genDirectory);
      }
    }
  }

  private static File determineGenDirectory(File sourceFile) {
    String path = sourceFile.toString();
    int cursor = path.lastIndexOf("/gen/");
    checkArgument(cursor >= 0, "Cannot find generated directory for source file:", sourceFile);
    return new File(path.substring(0, cursor) + "/gen");
  }

  private static Set<File> sCleanedDirectorySet = hashSet();

}

