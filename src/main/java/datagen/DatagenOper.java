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

import datagen.gen.DatagenConfig;
import datagen.gen.LangInfo;
import datagen.gen.Language;
import js.app.AppOper;
import js.base.SystemCall;
import js.file.DirWalk;
import js.file.Files;

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
    Context.prepareApp(files(), config);

    List<File> entriesToFreshen = constructFileEntries();

    File previousDirectory = null;

    for (var entry : entriesToFreshen) {
      log("...processing file:", entry);

      try {

        {
          var dir = Files.parent(entry);
          p54("proc file:", entry, INDENT, "dir:", dir);
          if (!dir.equals(previousDirectory)) {
            Context.prepareDir(dir);
            previousDirectory = dir;
          }
        }

        // Parse .dat file
        //
        DataDefinitionParser p = new DataDefinitionParser();
        p.setVerbose(verbose());
        p.parse(entry);

      } catch (Throwable t) {
        if (app().showExceptions() || SHOW_STACK_TRACES)
          throw t;
        setError("Processing", entry, INDENT, t.getMessage());
      } finally {
        Context.discardClassOrEnum();
      }
    }

    Context.sql.complete();
    Context.generateAuxilliarySourceFiles();

    if (!files().dryRun() && config.format()) {
      if (!DEBUG_RUST_FILES)
        formatSourceFiles();
    }
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

      log("...source directory:", config.sourcePath());
      log("...   dat directory:", config.datPath());
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

  private List<File> constructFileEntries() {

    DatagenConfig config = datagenConfig();
    File datRoot = Context.config.datPath();
    p54("constructFileEntries, datRoot:", datRoot);
    DirWalk dirWalk = new DirWalk(datRoot).withRecurse(true).withExtensions(EXT_DATA_DEFINITION);
    if (dirWalk.files().isEmpty())
      pr("*** no .dat files were found in:", config.datPath());
    var fileEntries = dirWalk.filesRelative();
    p54("fileEntries:", INDENT, fileEntries);
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


  private DatagenConfig mConfig;
}
