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
package datatest;

import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import datagen.Datagen;
import datagen.ParseTools;
import datagen.gen.Language;
import js.file.Files;
import js.app.App;
import js.base.BasePrinter;
import js.data.DataUtil;
import js.testutil.MyTestCase;

import static datagen.Context.*;

public abstract class GenBaseTest extends MyTestCase {

  protected final void compile() {
    closeCurrentDat();
    checkState(!mDatRecords.isEmpty());
    for (DatRecord ty : mDatRecords)
      writeDat(ty);
    addArg("language", mLanguage.toString().toLowerCase());
    if (!mCryptic)
      addArg("verbose_names");
    addArg("dat_path", datFilesDir(), "source_path", sourceDir());
    if (verbose())
      addArg("--verbose");
    App app = new Datagen();
    addArg("--exceptions");
    app.startApplication(DataUtil.toStringArray(args()));
    if (app.returnCode() != 0)
      throw die(app.getError());
    assertGenerated();
  }

  protected final void addArg(Object... args) {
    for (Object a : args) {
      mArgs.add(a.toString());
    }
  }

  /**
   * Get BasePrinter for current dat file (creating one if necessary)
   */
  protected final BasePrinter p() {
    if (datRecord().mPrBuffer == null) {
      generateDirs();
      datRecord().mPrBuffer = new BasePrinter();
    }
    return datRecord().mPrBuffer;
  }

//  protected void newDatSubdir(String relPath) {
//    pdir("new dat subdir, path:", relPath);
//    mDatRecord = null;
//    datRecord().mDatSubdirRelPath = relPath;
//  }

  /**
   * Get current DatRecord, creating one if necessary
   */
  private DatRecord datRecord() {
    if (mDatRecord == null) {
      var dr = new DatRecord();
      dr.mDatSubdir = new File(datFilesDir(), GEN_SUBDIR_NAME);
      mDatRecords.add(dr);
      mDatRecord = dr;
    }
    return mDatRecord;
  }

  /**
   * Set filename for current dat file (defaults to <<testname>>)
   *
   * @param filename name without extension
   */
  protected void withDatFilename(String filename) {
    checkArgument(Files.basename(filename).equals(filename), "dat filename must be a base name (no extension or dirs)");
    datRecord().mDatFilename = filename;
  }

  /**
   * Start a new DatRecord, with a subdirectory that is relative to the previous one
   */
  protected void pushDatSubdir(String relPath) {
    File currentSubdir = (mDatRecord == null) ? datFilesDir() : mDatRecord.mDatSubdir;
    var dr = new DatRecord();
    dr.mDatSubdir = Files.getCanonicalFile(new File(currentSubdir, relPath));
    mDatRecords.add(dr);
    mDatRecord = dr;
  }


  private void generateDirs() {
    Files.S.mkdirs(Files.assertNonEmpty(datRecord().mDatSubdir));
  }

  private List<String> args() {
    return mArgs;
  }

  private void writeDat(DatRecord ty) {
    String content = ty.mPrBuffer.toString();
    if (content.isEmpty()) return;

    var filename = ty.mDatFilename;
    if (nullOrEmpty(filename))
      filename = name();
    File datFile = new File(ty.mDatSubdir,
        convertCamelToUnderscore(filename) + DOT_EXT_DATA_DEFINITION);
    Files.S.writeString(datFile, content);
  }

  private void closeCurrentDat() {
    mDatRecord = null;
  }

  private static class DatRecord {
    BasePrinter mPrBuffer;
    File mDatSubdir;
    String mDatFilename;
  }

  // ------------------------------------------------------------------
  // Cached values for test
  // ------------------------------------------------------------------

  private File datFilesDir() {
    if (mDatFilesDir == null) {
      mDatFilesDir = Files.S.mkdirs(new File(generatedDir(), "dat_files"));
    }
    return mDatFilesDir;
  }

  protected final File sourceDir() {
    if (mSourceDir == null) {
      mSourceDir = Files.S.mkdirs(new File(generatedDir(), sourceFileExtension(mLanguage)));
    }
    return mSourceDir;
  }

  protected final void language(Language x) {
    mLanguage = x;
  }

  protected final void cryptic() {
    mCryptic = true;
  }

  private Language mLanguage = Language.JAVA;
  private boolean mCryptic;
  private File mSourceDir;
  private File mDatFilesDir;
  private DatRecord mDatRecord;
  private List<String> mArgs = arrayList();

  // List of completed dat records
  //
  private List<DatRecord> mDatRecords = arrayList();

}
