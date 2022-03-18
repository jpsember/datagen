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

import static datagen.ParseTools.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;
import java.util.Set;

import datagen.gen.DatWithSource;
import datagen.gen.DatagenConfig;
import datagen.gen.Language;
import js.app.AppOper;
import js.data.DataUtil;
import js.file.DirWalk;
import js.file.Files;
import js.parsing.Scanner;
import js.parsing.Token;

public class DatagenOper extends AppOper {

  protected String getHelpDescription() {
    return "Generate Java data classes from .dat files";
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

    Context.prepare();
    Context context = Context.SHARED_INSTANCE;

    DatagenConfig.Builder config = (DatagenConfig.Builder) config().toBuilder();

    if (Files.nonEmpty(config.startDir()))
      config.startDir(config.startDir().getAbsoluteFile());

    File datPath = getFile(config.startDir(), config.datPath());

    if (Files.nonEmpty(config.protoPath())) {
      File protoPath = Files.assertDirectoryExists(getFile(config.startDir(), config.protoPath()),
          "proto_path");
      config.protoPath(protoPath);
      files().mkdirs(datPath);
      config.datPath(datPath);

      context.config = config;
      if (context.python())
        pr("PYTHON");
      performConvert();
      return;
    } else {
      config.datPath(Files.assertDirectoryExists(datPath, "dat_dir"));
    }

    if (Files.empty(config.sourcePath())) {
      File f;
      switch (config.language()) {
      default:
        throw notSupported(config.language());
      case JAVA:
        f = new File("src/main/java");
        break;
      case PYTHON:
        f = Files.currentDirectory();
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

    context.config = config;

    {
      DirWalk d = new DirWalk(config.datPath()).withRecurse(true).withExtensions(EXT_DATA_DEFINITION);
      if (d.files().isEmpty()) {
        pr("(...no .dat files were found in:", config.datPath() + ")");
      }

      for (File rel : d.filesRelative()) {
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
          throw notSupported(config.language());
        case JAVA:
          sourceClassName = DataUtil.convertUnderscoresToCamelCase(protoName);
          break;
        case PYTHON:
          sourceClassName = protoName;
          break;
        }
        String relativeClassFile = relPathExpr + sourceClassName + "."
            + SourceGen.sourceFileExtension(config.language());
        File sourceFile = new File(config.sourcePath(), relativeClassFile);

        if (config.clean()) {
          // If we haven't yet done so, delete the 'gen' directory that will contain this source file
          discardGenDirectory(sourceFile);
        }

        DatWithSource fileEntry = DatWithSource.newBuilder().datRelPath(rel.getPath())
            .sourceRelPath(relativeClassFile).build();

        if (config.clean() || !sourceFile.exists() || sourceFile.lastModified() < d.abs(rel).lastModified()) {
          if (verbose()) {
            if (sourceFile.exists())
              log("file is out of date:", relativeClassFile);
            else
              log("could not locate:", INDENT, relativeClassFile);
          }
          mGeneratedSourceFilesToFreshen.add(fileEntry);
        } else {
          // We don't need to rebuild this file; but register it, so
          // we clean its directory of any old files
          registerGeneratedSourceFile(sourceFile);
        }
      }
    }

    if (mGeneratedSourceFilesToFreshen.isEmpty())
      log("...all files up-to-date (rerun with 'clean' option to force rebuild)");

    for (DatWithSource entry : mGeneratedSourceFilesToFreshen) {
      log("...processing file:", entry.datRelPath());

      // Reset context for a new file
      context.datWithSource = entry;
      context.config = config.toBuilder();
      context.files = files();
      context.dataTypeManager = new DataTypeManager();
      context.generatedTypeDef = null;

      DataDefinitionParser p = new DataDefinitionParser();
      p.setVerbose(verbose());
      try {
        p.parse();
        SourceGen gn = SourceGen.construct(context);
        gn.setVerbose(verbose());
        gn.generate();
        File sourceFile = new File(config.sourcePath(), entry.sourceRelPath());
        registerGeneratedSourceFile(sourceFile);
      } catch (Throwable t) {
        if (!app().catchingErrors() || SHOW_STACK_TRACES)
          throw t;
        setError("Processing", entry.datRelPath(), INDENT, t.getMessage());
      }
    }

    if (config.deleteOld())
      deleteOldSourceFiles();
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

  private void deleteOldSourceFiles() {
    Context context = Context.SHARED_INSTANCE;
    DirWalk dirWalk = new DirWalk(context.config.sourcePath()).withRecurse(true)
        .withExtensions(SourceGen.sourceFileExtension(context.language()));
    todo("!is this working with the new relative paths for DatWithSource?");
    for (File sourceFile : dirWalk.files()) {
      // If file is not in a directory we wrote generated files to, ignore
      if (!mGeneratedDirectorySet.contains(Files.parent(sourceFile)))
        continue;
      if (mGeneratedSourceFileSet.contains(sourceFile))
        continue;
      log("deleting old generated source file:", dirWalk.rel(sourceFile));
      files().deleteFile(sourceFile);
    }
  }

  /**
   * Add generated file to list, so we don't delete it (in case the 'delete_old'
   * option was specified)
   */
  private void registerGeneratedSourceFile(File sourceFile) {
    mGeneratedSourceFileSet.add(sourceFile);
    mGeneratedDirectorySet.add(Files.parent(sourceFile));
  }

  /**
   * Convert all .proto files to .dat files
   */
  private void performConvert() {
    DatagenConfig config = Context.SHARED_INSTANCE.config;
    log("proto directory:", config.protoPath());
    log("  dat directory:", config.datPath());

    DirWalk w = new DirWalk(config.protoPath()).withExtensions("proto");
    int filesExamined = 0;
    for (File f : w.filesRelative()) {
      if (false && alert("skipping most files") && !f.getName().startsWith("udder_focus_config")) {
        continue;
      }
      filesExamined++;
      File source = w.abs(f);
      File target = Files.setExtension(new File(config.datPath(), f.getPath()), EXT_DATA_DEFINITION);

      files().mkdirs(target.getParentFile());

      convert(source, target);
    }
    if (filesExamined == 0)
      pr("*** No .proto files found in", config.protoPath());
  }

  private void convert(File source, File target) {
    List<Token> tokens = readTokens(source);
    List<String> strings = rewriteTokens(tokens);
    encodeOutput(strings, target);
  }

  private void encodeOutput(List<String> strings, File target) {
    String result = String.join("", strings);
    files().writeString(target, result);
  }

  /**
   * Read tokens from file (including whitespace and comments)
   */
  private List<Token> readTokens(File source) {
    String fileContent = Files.readString(source);
    // Don't skip any whitespace, since we want to preserve comments
    Scanner scanner = new Scanner(dfa(), fileContent, -1);
    List<Token> tokens = arrayList();
    while (scanner.hasNext())
      tokens.add(scanner.read());
    return tokens;
  }

  /**
   * Rewrite tokens based on rules
   */
  private List<String> rewriteTokens(List<Token> input) {
    List<String> output = arrayList();

    int icursor = 0;
    while (icursor < input.size()) {
      Token t = input.get(icursor);
      String x = t.text();
      String y = null;

      switch (x) {
      case "message":
        x = "fields";
        break;
      case "class":
        x = "extern";
        break;
      case "repeated":
        x = "*";
        break;
      case "default":
        x = "=";
        break;
      case "bytes":
        x = "*";
        y = "byte";
        break;
      case "ints":
        x = "*";
        y = "int";
        break;
      case "shorts":
        x = "*";
        y = "short";
        break;
      case "option":
        y = x;
        x = "// no longer supported: ";
        break;
      case "optional":
        x = "?";
        break;
      }
      output.add(x);
      if (y != null)
        output.add(y);
      icursor++;
    }
    return output;
  }

  private void discardGenDirectory(File sourceFile) {
    String path = sourceFile.toString();
    int cursor = path.lastIndexOf("/gen/");
    if (cursor < 0)
      setError("Cannot find generated directory for source file:", sourceFile);
    File genDirectory = new File(path.substring(0, cursor) + "/gen");
    if (mGenDirectoriesSet.add(genDirectory)) {
      if (genDirectory.exists()) {
        log("Deleting existing generated source directory:", genDirectory);
        files().deleteDirectory(genDirectory);
      }
    }
  }

  private Set<File> mGenDirectoriesSet = hashSet();
  private List<DatWithSource> mGeneratedSourceFilesToFreshen = arrayList();

  // Set of Java files corresponding to all .dat files found
  private Set<File> mGeneratedSourceFileSet = hashSet();
  // Set of directories containing generated Java source files
  private Set<File> mGeneratedDirectorySet = hashSet();;

}
