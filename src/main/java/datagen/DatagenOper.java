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
import static datagen.Utils.*;

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

public class DatagenOper extends AppOper {

  protected String getHelpDescription() {
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
    DatagenConfig config = datagenConfig();
    Context.prepare(files(), config);
     
    List<DatWithSource> entriesToFreshen = constructFileEntries(); 

    if (entriesToFreshen.isEmpty())
      log("...all files up-to-date (rerun with 'clean' option to force rebuild)");

    for (DatWithSource entry : entriesToFreshen) {
      log("...processing file:", entry.datRelPath());

      // Reset context for a new file
      Context.prepare(entry);

      try {

        // Parse .dat file
        //
        DataDefinitionParser p = new DataDefinitionParser();
        p.setVerbose(verbose());
        p.parse();

        // Generate source file in appropriate language
        //
        SourceGen g = SourceGen.construct();
        g.setVerbose(verbose());
        g.generate();

      } catch (Throwable t) {
        if (app().showExceptions() || SHOW_STACK_TRACES)
          throw t;
        setError("Processing", entry.datRelPath(), INDENT, t.getMessage());
      } finally {
        Context.discard();
      }
    }

    if (config.deleteOld())
      deleteOldSourceFiles(config.sourcePath());
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

      if (config.clean() || !sourceFile.exists()
          || sourceFile.lastModified() < dirWalk.abs(rel).lastModified()) {
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

  private File determineGenDirectory(File sourceFile) {
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

}
