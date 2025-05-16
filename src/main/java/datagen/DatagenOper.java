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

import static datagen.ParseTools.*;
import static js.base.Tools.*;
import static datagen.Utils.*;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import datagen.gen.DatWithSource;
import datagen.gen.DatagenConfig;
import datagen.gen.LangInfo;
import datagen.gen.Language;
import js.app.AppOper;
import js.base.SystemCall;
import js.data.DataUtil;
import js.file.DirWalk;
import js.file.Files;
import js.parsing.RegExp;

public class DatagenOper extends AppOper {

  protected String shortHelp() {
    return "Generate source files from .dat files";
  }

  @Override
  public DatagenConfig defaultArgs() {
    return DatagenConfig.DEFAULT_INSTANCE;
  }

  @Override
  public String userCommand() {
    return "datagen";
  }

  @Override
  public void perform() {
    cmdLineArgs().assertArgsDone();
    DatagenConfig config = datagenConfig();
    Context.prepare(files(), config);

    List<File> entriesToFreshen = constructFileEntries2();

    if (Context.rust())
      prepareRustModules();

//    if (entriesToFreshen.isEmpty())
//      log("...all files up-to-date (rerun with 'clean' option to force rebuild)");

    for (var entry : entriesToFreshen) {
      log("...processing file:", entry);

      todo("call Context.prepare() with suitable structure");
//      // Reset context for a new file
//      Context.prepare(entry);

      try {

        // Parse .dat file
        //
        DataDefinitionParser p = new DataDefinitionParser();
        p.setVerbose(verbose());
        p.parse(entry);

//        // Generate source file in appropriate language
//        //
//        SourceGen g = SourceGen.construct();
//        g.setVerbose(verbose());
//        g.generate();
//
//        if (Context.pt.rust())
//          updateRustModules(entry);
      } catch (Throwable t) {
        if (app().showExceptions() || SHOW_STACK_TRACES)
          throw t;
        setError("Processing", entry, INDENT, t.getMessage());
      } finally {
        Context.discard();
      }
    }

    Context.sql.complete();
    if (Context.rust())
      flushRustModules();

    if (!files().dryRun() && config.format()) {
      if (!DEBUG_RUST_FILES)
        formatSourceFiles();
    }

    if (config.deleteOld())
      deleteOldSourceFiles(config.sourcePath());
  }

  /**
   * Use the gofmt tool to format the source files as a go ide would
   */
  private void formatSourceFiles() {

    if (Context.generatedFilesSet.isEmpty())
      return;

    SystemCall sc = new SystemCall();

    // todo("allow user to configure location of format tool");

    switch (datagenConfig().language()) {
      case GO:
        sc.arg("/usr/local/go/bin/gofmt", "-w");
        for (File f : Context.generatedFilesSet) {
          sc.arg(f.toString());
        }
        break;
      case RUST:
        sc.arg(new File(Files.homeDirectory(), ".cargo/bin/rustfmt"));
        for (File f : Context.generatedFilesSet) {
          sc.arg(f.toString());
        }
        break;
      default:
        alert("no formatting is available for this language");
        return;
    }
    if (sc.exitCode() != 0) {
      alert("problem formatting; try without 'format' option to investigate?", INDENT, sc.systemErr());
    }
    sc.assertSuccess();
  }

  /**
   * Prepare DatagenConfig object by reading it from the configuration
   * arguments, and applying any default values or additional processing
   */
  private DatagenConfig datagenConfig() {

    if (mConfig == null) {
      DatagenConfig.Builder config = (DatagenConfig.Builder) config().toBuilder();

      if (Files.nonEmpty(config.startDir()))
        config.startDir(config.startDir().getAbsoluteFile());

      if (config.language() == Language.AUTO) {
        determineLanguage(config);
      }

      config.datPath(Files.assertDirectoryExists(getFile(config.startDir(), config.datPath()), "dat_path"));

      if (Files.empty(config.sourcePath())) {
        File f;
        switch (config.language()) {
          default:
            throw languageNotSupported();
          case JAVA:
            f = new File("src/main/java");
            break;
          case GO:
          case PYTHON:
            f = Files.currentDirectory();
            break;
          case RUST:
            f = new File("src");
            break;
        }
        config.sourcePath(f);
      }

      File sourcePathRel = config.sourcePath();
      if (config.language() == Language.PYTHON && !Files.empty(config.pythonSourcePath()))
        sourcePathRel = config.pythonSourcePath();

      // If the output source directory doesn't exist, make it
      config.sourcePath(files().mkdirs(getFile(config.startDir(), sourcePathRel)));

      log("source directory:", config.sourcePath());
      log(" dat directory:", config.datPath());
      mConfig = config.build();
    }
    return mConfig;
  }

  private void determineLanguage(DatagenConfig.Builder config) {
    var startDir = config.startDir();
    if (Files.empty(startDir))
      startDir = Files.currentDirectory();
    List<LangInfo> candidateList = arrayList();
    addLangInfo(candidateList, Language.JAVA, startDir, "pom.xml");
    addLangInfo(candidateList, Language.RUST, startDir, "Cargo.toml");
    addLangInfo(candidateList, Language.PYTHON, startDir, "__init__.py");
    addLangInfo(candidateList, Language.GO, startDir, "go.mod");
    candidateList.sort(new Comparator<>() {
      @Override
      public int compare(LangInfo o1, LangInfo o2) {
        var diff = -Integer.compare(o1.depth(), o2.depth());
        if (diff == 0)
          diff = Integer.compare(o1.language().ordinal(), o2.language().ordinal());
        return diff;
      }
    });

    log("determineLanguage, candidates:", INDENT, candidateList);
    if (candidateList.isEmpty())
      setError("Can't infer language");
    var winner = candidateList.get(0);
    if (candidateList.size() >= 2) {
      var runnerUp = candidateList.get(1);
      if (winner.depth() == runnerUp.depth())
        setError("Can't infer language; found:", INDENT, winner.sentinelFile(), CR, runnerUp.sentinelFile());
    }
    log("candidates:", INDENT, candidateList, CR, "result:", winner);
    config.language(winner.language());
  }

  private void addLangInfo(List<LangInfo> dest, Language language, File startDir, String seekName) {
    var f = Files.getFileWithinParents(startDir, seekName);
    if (Files.nonEmpty(f)) {
      var b = LangInfo.newBuilder().language(language).sentinelFile(f);
      var s = f.toString();
      int depth = 0;
      int i = 0;
      while (true) {
        var j = s.indexOf('/', i);
        if (j < 0)
          break;
        depth++;
        i = j + 1;
      }
      b.depth(depth);
      dest.add(b.build());
    }
  }

  private List<File> constructFileEntries2() {
    List<File> fileEntries = arrayList();

    DatagenConfig config = datagenConfig();
    DirWalk dirWalk = new DirWalk(config.datPath()).withRecurse(true).withExtensions(EXT_DATA_DEFINITION);
    if (dirWalk.files().isEmpty())
      pr("*** no .dat files were found in:", config.datPath());

    Set<File> discardedDirectoriesSet = hashSet();

    for (File rel : dirWalk.filesRelative()) {
      String relPathExpr;
      {
        File relPath = rel.getParentFile();
        if (relPath == null)
          relPathExpr = "";
        else {
          if (relPath.toString().contains("_SKIP_"))
            continue;
          relPathExpr = relPath + "/";
        }
      }

//      // Determine source file corresponding to this one.
//      String protoName = chomp(rel.getName(), DOT_EXT_DATA_DEFINITION);
//
//      String sourceClassName;
//      switch (config.language()) {
//        default:
//          throw languageNotSupported();
//        case JAVA:
//          sourceClassName = DataUtil.convertUnderscoresToCamelCase(protoName);
//          break;
//        case PYTHON:
//          sourceClassName = protoName;
//          break;
//        case GO:
//          sourceClassName = protoName;
//          break;
//        case RUST:
//          sourceClassName = protoName;
//          break;
//      }
//      String relativeClassFile = relPathExpr + sourceClassName + "." + sourceFileExtension();
//      File sourceFile = new File(config.sourcePath(), relativeClassFile);
//      File genDirectory = determineGenDirectory(sourceFile);

//      if (config.clean()) {
//        // If we haven't yet done so, delete the 'gen' directory that will contain this source file
//        discardGenDirectory(discardedDirectoriesSet, genDirectory);
//      }

      fileEntries.add(rel);
    }
//      DatWithSource fileEntry = DatWithSource.newBuilder().datRelPath(rel.getPath())
//          .sourceRelPath(relativeClassFile).build();
//
//      boolean rebuildRequired = config.clean();
//      if (!rebuildRequired) {
//        if (!sourceFile.exists() || sourceFile.lastModified() < dirWalk.abs(rel).lastModified())
//          rebuildRequired = true;
//      }
//
//      if (rebuildRequired) {
//        if (verbose()) {
//          if (sourceFile.exists())
//            log("file is out of date:", relativeClassFile);
//          else
//            log("could not locate generated file:", relativeClassFile);
//        }
//        fileEntries.add(fileEntry);
//      }
//    }
    return fileEntries;
  }


  private List<DatWithSource> constructFileEntries() {
    List<DatWithSource> fileEntries = arrayList();

    DatagenConfig config = datagenConfig();
    DirWalk dirWalk = new DirWalk(config.datPath()).withRecurse(true).withExtensions(EXT_DATA_DEFINITION);
    if (dirWalk.files().isEmpty())
      pr("*** no .dat files were found in:", config.datPath());

    Set<File> discardedDirectoriesSet = hashSet();

    for (File rel : dirWalk.filesRelative()) {
      String relPathExpr;
      {
        File relPath = rel.getParentFile();
        if (relPath == null)
          relPathExpr = "";
        else {
          if (relPath.toString().contains("_SKIP_"))
            continue;
          relPathExpr = relPath + "/";
        }
      }

      // Determine source file corresponding to this one.
      String protoName = chomp(rel.getName(), DOT_EXT_DATA_DEFINITION);

      String sourceClassName;
      switch (config.language()) {
        default:
          throw languageNotSupported();
        case JAVA:
          sourceClassName = DataUtil.convertUnderscoresToCamelCase(protoName);
          break;
        case PYTHON:
          sourceClassName = protoName;
          break;
        case GO:
          sourceClassName = protoName;
          break;
        case RUST:
          sourceClassName = protoName;
          break;
      }
      String relativeClassFile = relPathExpr + sourceClassName + "." + sourceFileExtension();
      File sourceFile = new File(config.sourcePath(), relativeClassFile);
      File genDirectory = determineGenDirectory(sourceFile);

      if (config.clean()) {
        // If we haven't yet done so, delete the 'gen' directory that will contain this source file
        discardGenDirectory(discardedDirectoriesSet, genDirectory);
      }

      DatWithSource fileEntry = DatWithSource.newBuilder().datRelPath(rel.getPath())
          .sourceRelPath(relativeClassFile).build();

      boolean rebuildRequired = config.clean();
      if (!rebuildRequired) {
        if (!sourceFile.exists() || sourceFile.lastModified() < dirWalk.abs(rel).lastModified())
          rebuildRequired = true;
      }

      if (rebuildRequired) {
        if (verbose()) {
          if (sourceFile.exists())
            log("file is out of date:", relativeClassFile);
          else
            log("could not locate generated file:", relativeClassFile);
        }
        fileEntries.add(fileEntry);
      }
    }
    return fileEntries;
  }

  /**
   * Make a file absolute, and within the supplied start directory (if not
   * null), if it's relative
   */
  private static File getFile(File startDirOrNull, File file) {
    File result = file;
    if (!file.isAbsolute()) {
      if (Files.nonEmpty(startDirOrNull))
        result = new File(startDirOrNull, file.getPath());
      else
        result = file.getAbsoluteFile();
    }
    return result;
  }

  private void deleteOldSourceFiles(File sourcePath) {
    Set<File> modifiedDirectorySet = hashSet();
    for (File f : Context.generatedFilesSet)
      modifiedDirectorySet.add(Files.parent(f));

    DirWalk dirWalk = new DirWalk(sourcePath).withRecurse(true).withExtensions(sourceFileExtension());

    for (File sourceFile : dirWalk.files()) {
      // If we generated this file, ignore
      if (Context.generatedFilesSet.contains(sourceFile))
        continue;
      // If file is not in a directory we wrote generated files to, ignore
      if (!modifiedDirectorySet.contains(Files.parent(sourceFile)))
        continue;
      log("deleting old generated source file:", dirWalk.rel(sourceFile));
      files().deleteFile(sourceFile);
    }
  }

  private File determineGenDirectoryOLD(File sourceFile) {
    String path = sourceFile.toString();
    int cursor = path.lastIndexOf("/gen/");
    if (cursor < 0)
      setError("Cannot find generated directory for source file:", sourceFile);
    return new File(path.substring(0, cursor) + "/gen");
  }

  private void discardGenDirectory(Set<File> discardedDirectoriesSet, File genDirectory) {
    if (discardedDirectoriesSet.add(genDirectory)) {
      if (genDirectory.exists()) {
        log("Deleting existing generated source directory:", genDirectory);
        files().deleteDirectory(genDirectory);
      }
    }
  }

  private DatagenConfig mConfig;

  // ------------------------------------------------------------------
  // Generating Rust module files
  // ------------------------------------------------------------------

  private void prepareRustModules() {
    mModFilesMap = hashMap();
  }

  private void updateRustModules(DatWithSource entry) {
    var db = DEBUG_RUST;
    if (db)
      log("updateRustModules, source:", entry.sourceRelPath());
    var srcPath = new File(datagenConfig().sourcePath(), entry.sourceRelPath());
    while (true) {
      if (db)
        log("...", srcPath);
      var parent = Files.parent(srcPath);
      if (parent.equals(datagenConfig().sourcePath()))
        break;
      var st = mModFilesMap.get(parent);
      if (st == null) {
        st = hashSet();
        mModFilesMap.put(parent, st);
      }
      var name = Files.removeExtension(srcPath.getName());
      st.add(name);
      srcPath = parent;
    }
  }

  private void flushRustModules() {
    for (var ent : mModFilesMap.entrySet()) {
      var file = ent.getKey();
      var set = ent.getValue();
      Set<String> sorted = treeSet();
      sorted.addAll(set);

      var modFile = new File(file, "mod.rs");
      if (datagenConfig().clean())
        files().deletePeacefully(modFile);

      // Add existing mod file entries, so we don't omit any ones
      // corresponding to files that aren't being freshened
      // (as they won't have entries in our mModFilesMap)
      {
        var c = Files.readString(modFile, "").strip();
        if (!c.isEmpty()) {
          var lines = split(c, '\n');
          for (var x : lines) {
            x = removePubModFromClass(x);
            sorted.add(x);
          }
        }
      }

      var sb = new StringBuilder();
      for (var x : sorted)
        sb.append(addPubModToClass(x)).append('\n');
      var content = sb.toString();
      if (DEBUG_RUST_IMPORTS && verbose())
        log(VERT_SP, "Updating", modFile, ":", INDENT, content, VERT_SP);
      files().writeString(modFile, content);
    }
  }

  private static String addPubModToClass(String className) {
    return "pub mod " + className + ";";
  }

  private static String removePubModFromClass(String pubModEntry) {
    var m = RegExp.matcher("pub mod ([a-zA-Z_][a-zA-Z_0-9]*);", pubModEntry.strip());
    checkArgument(m.matches(), "unexpected mod.rs entry:", pubModEntry);
    var stripped = m.group(1);
    return stripped;
  }

  private Map<File, Set<String>> mModFilesMap;

}
