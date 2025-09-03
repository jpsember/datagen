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
import java.util.List;
import java.util.Map;
import java.util.Set;

import datagen.gen.DatagenConfig;
import datagen.gen.Language;
import js.base.BasePrinter;
import js.data.DataUtil;
import js.file.Files;
import js.json.JSList;

import static js.base.Tools.*;

public final class Context {

  public static final boolean DEBUG_RUST_FILES = false && alert("ISSUE 47 is in effect");
  public static final boolean DEBUG_RUST_IMPORTS = false;
  public static final boolean DEBUG_RUST_MOD = false && alert("DEBUG_RUST_MOD is in effect");
  public static final boolean DEBUG_RUST = DEBUG_RUST_FILES || DEBUG_RUST_IMPORTS || DEBUG_RUST_MOD;
  public static final boolean RUST_COMMENTS = DEBUG_RUST;

  public static final String GEN_SUBDIR_NAME = "gen";

  public static final boolean ISSUE_JTOOLS = false && alert("ISSUE_JTOOLS is in effect");

  public static void pJTOOLS(Object... messages) {
    if (ISSUE_JTOOLS)
      pr(insertStringToFront("ISSUE_JTOOLS --->", messages));
  }

  public static void pmod(Object... messages) {
    if (DEBUG_RUST_MOD)
      pr(insertStringToFront("RUST_MOD --->", messages));
  }

  public static final String EXT_DATA_DEFINITION = "dat";
  public static final String DOT_EXT_DATA_DEFINITION = "." + EXT_DATA_DEFINITION;

  /**
   * Throw UnsupportedOperationException due to an unsupported target language
   */
  public static UnsupportedOperationException languageNotSupported(Object... messages) {
    throw notSupported(insertStringToFront("Language not supported:", messages));
  }

  public static void addCr(StringBuilder dest) {
    dest.append('\n');
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
      case GO:
        return "go";
      case RUST:
        return "rs";
    }
  }

  public static String verboseVariant(String succinctOption, String verboseOption) {
    return Context.config.verboseNames() ? verboseOption : succinctOption;
  }

  public static String notSupportedMessage(Object... messages) {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    StackTraceElement elem = stackTrace[2];

    StringBuilder sb = new StringBuilder();
    sb.append("!!!=== Not supported yet");
    if (messages.length > 0) {
      sb.append(": ");
      sb.append(BasePrinter.toString(messages));
    } else {
      sb.append("!");
    }
    sb.append(" (");
    sb.append(elem.getFileName());
    sb.append("(");
    sb.append(elem.getLineNumber());
    sb.append("): ");
    sb.append(elem.getMethodName());
    sb.append(") ===!!!");
    return sb.toString();
  }

  /**
   * Get stack trace element at a particular depth within the current thread,
   * converted to a string that allows clicking on within (an
   * intelligent-enough) IDE.
   */
  public static String getStackTraceElement(int stackDepth) {
    stackDepth += 2;
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    if (stackTrace.length <= stackDepth) {
      return "(no stack info avail)";
    } else {
      StackTraceElement element = stackTrace[stackDepth];

      var sb = new StringBuilder();
      //
      sb.append('(');
      sb.append(element.getFileName());
      sb.append(':');
      sb.append(element.getLineNumber());
      sb.append("; ");

      // The method name maybe just adds unnecessary clutter.
      sb.append(element.getMethodName());
      sb.append(')');
      return sb.toString();
    }
  }


  public static void addGeneratedFile(File f) {
    generatedSourceFiles.add(f);
  }

  public static DataTypeManager dataTypeManager() {
    return mDataTypeManager;
  }

  public static File datDirectoryRelative() {
    checkNotNull(sDatRelPath, "datDirectoryRelative");
    return sDatRelPath;
  }

  public static File sourceRelPath() {
    return sSourceRelPath;
  }

  // ----------------------------------------------------------------------------------------------
  // Lifetimes
  // ----------------------------------------------------------------------------------------------

  public static void prepareApp(boolean showExceptions, Files files, DatagenConfig config) {
    Context.showExceptions = showExceptions;
    var b = config.toBuilder();
    b.datPath(Files.absolute(b.datPath()));
    Context.config = b.build();

    pt = new ParseTools();
    Context.files = files;
    Context.config = config.build();
    generatedSourceFiles = hashSet();
    sql = new SqlGen(config);
    sql.prepare();
    sCleanedDirectorySet = hashSet();
    sGeneratedSources = arrayList();
  }

  public static void prepareDir(File dir) {
    discardDir();
    mDataTypeManager = new DataTypeManager();
    Files.assertRelative(dir, "expected relative dir");
    sDatRelPath = dir;
  }

  public static void discardDir() {
    mDataTypeManager = null;
    sDatRelPath = null;
  }

  public static void prepareForClassOrEnumDefinition(File sourceRelPath) {
    discardClassOrEnum();
    sSourceRelPath = sourceRelPath;
  }

  /**
   * Discard some old elements
   */
  public static void discardClassOrEnum() {
    generatedTypeDef = null;
    sSourceRelPath = null;
  }

  public static void generateAuxilliarySourceFiles() {
    pmod("generateAuxilliarySourceFiles");
    switch (language()) {
      default:
        return;
      case RUST:
        genAuxFilesRust();
        break;
      case PYTHON:
        genAuxFilesPython();
        break;
    }
  }


  private static void genAuxFilesPython() {
    var entries = sGeneratedSources;
    List<GeneratedTypeDef> listForCurrentDir = arrayList();
    File currentDir = Files.DEFAULT;

    for (var genType : entries) {
      var file = genType.sourceFile();
      var dir = Files.parent(file);
      pmod("file:", file, CR, "dir :", dir);

      if (!dir.equals(currentDir)) {
        flushDirPython(currentDir, listForCurrentDir);
        currentDir = dir;
        listForCurrentDir.clear();
      }
      listForCurrentDir.add(genType);
    }
    flushDirPython(currentDir, listForCurrentDir);
  }

  private static void flushDirPython(File directory, List<GeneratedTypeDef> entries) {
    if (entries.isEmpty()) return;
    File sentinelFile = new File(directory, "__init__.py");
    files.write(DataUtil.EMPTY_BYTE_ARRAY, sentinelFile);
  }

  private static void genAuxFilesRust() {
    sRustModFilesMap = hashMap();
    helpGenAuxFilesRust(sGeneratedSources);

    if (DEBUG_RUST_MOD) {
      // display the mod files maps
      for (var ent : sRustModFilesMap.entrySet()) {
        pmod("file:", ent.getKey());
        pmod("...entries:", JSList.with(ent.getValue()));
      }
    }

    for (var ent : sRustModFilesMap.entrySet()) {
      var dir = ent.getKey();
      var entries = ent.getValue();
      checkState(!entries.isEmpty(), "no entries for directory:", dir);

      List<String> sortedList = arrayList();
      sortedList.addAll(entries);
      sortedList.sort(null);

      var sb = new StringBuilder();

      for (var x : entries) {
        sb.append("pub mod ");
        sb.append(x);
        sb.append(";\n");
      }
      var content = sb.toString();
      var modFile = new File(dir, "mod.rs");
      pmod("updating", modFile, ":", INDENT, content, VERT_SP);
      files.writeString(modFile, content);
    }
  }

  private static void helpGenAuxFilesRust(List<GeneratedTypeDef> typesList) {
    // Examine the types for the ones with the longest paths, and process them;
    // then recursively call with the remaining ones
    if (typesList.isEmpty()) return;
    int[] numComp = new int[typesList.size()];
    var maxLen = 0;
    {
      var i = INIT_INDEX;
      for (var g : typesList) {
        i++;
        var f = g.sourceFile().toString();
        var numComponents = split(f, '/').size();
        numComp[i] = numComponents;
        maxLen = Math.max(maxLen, numComponents);
      }
    }
    if (DEBUG_RUST_MOD) {
      pmod("aux, types list:", INDENT, typesList);
      pmod("...num comp:", JSList.with(numComp));
    }
    List<GeneratedTypeDef> process = arrayList();
    List<GeneratedTypeDef> remaining = arrayList();

    {
      var i = INIT_INDEX;
      for (var g : typesList) {
        i++;
        var numComponents = numComp[i];
        if (numComponents < maxLen) {
          remaining.add(g);
        } else {
          process.add(g);
        }
      }
    }

    pmod(VERT_SP, "processing deepest components");
    for (var g : process) {
      var file = g.sourceFile();
      while (true) {
        var filenameToAdd = Files.basename(file);
        var dir = Files.parent(file);
        // If the parent directory is the generated directory, stop
        pmod("...process:", file, "filenameToAdd:", filenameToAdd, "(config source:", config.sourcePath() + ")");
        if (dir.equals(config.sourcePath())) {
          pmod("....equals source path, stopping");
          break;
        }
        var filenameSet = sRustModFilesMap.get(dir);
        if (filenameSet == null) {
          filenameSet = hashSet();
          sRustModFilesMap.put(dir, filenameSet);
        }
        filenameSet.add(filenameToAdd);
        pmod("...added:", quote(filenameToAdd), "list is now:", filenameSet);
        // Repeat with an empty filename so we generate mod.rs files for all the parent directories (until we've
        // reached the source path)
        file = dir;
      }
    }
    helpGenAuxFilesRust(remaining);
  }

// ----------------------------------------------------------------------------------------------

  public static Language language() {
    return config.language();
  }

  public static boolean python() {
    return language() == Language.PYTHON;
  }

  public static boolean go() {
    return language() == Language.GO;
  }

  public static boolean rust() {
    return language() == Language.RUST;
  }

  private Context() {
  }

  public static void cleanIfNecessary(File dir) {
    if (!config.clean())
      return;
    var genDirectory = determineGenDirectory(dir);
    if (sCleanedDirectorySet.add(genDirectory)) {
      if (genDirectory.exists()) {
        files.deleteDirectory(genDirectory);
      }
    }
  }

  private static File determineGenDirectory(File sourceFile) {
    String path = sourceFile.toString();
    int cursor = path.lastIndexOf("/gen/");
    checkArgument(cursor >= 0, "Cannot find generated directory for source file:", sourceFile);
    return new File(path.substring(0, cursor) + "/gen");
  }


  public static void registerGeneratedSource(File target, GeneratedTypeDef generatedTypeDef) {
    pmod("generated:", target.getName(), "type:", generatedTypeDef.qualifiedName().className());
    sGeneratedSources.add(generatedTypeDef);
  }


  public static DatagenConfig config;
  public static Files files;
  public static GeneratedTypeDef generatedTypeDef;
  public static DataTypeManager mDataTypeManager;
  public static Set<File> generatedSourceFiles;
  public static ParseTools pt;
  public static SqlGen sql;
  private static File sDatRelPath;
  private static Set<File> sCleanedDirectorySet = hashSet();
  private static List<GeneratedTypeDef> sGeneratedSources = arrayList();
  private static File sSourceRelPath;
  public static boolean showExceptions;

  private static Map<File, Set<String>> sRustModFilesMap;
}

