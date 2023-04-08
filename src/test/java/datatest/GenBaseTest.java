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
package datatest;

import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import datagen.Datagen;
import datagen.ParseTools;
import datagen.Utils;
import datagen.gen.Language;
import js.file.Files;
import js.app.App;
import js.base.BasePrinter;
import js.data.DataUtil;
import js.testutil.MyTestCase;

public abstract class GenBaseTest extends MyTestCase {

  protected final void debug() {
    addArg("debug");
  }
  
  protected final void classMode() {
    addArg("class_mode");
  }

  protected final void compile() {
    closeCurrentDat();
    checkState(!mDatRecords.isEmpty());
    for (DatRecord ty : mDatRecords)
      writeDat(ty);
    addArg("language", mLanguage.toString().toLowerCase());
    if (mVerboseNames)
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

  protected final BasePrinter p() {
    if (dr().mPrBuffer == null) {
      generateDirs();
      dr().mPrBuffer = new BasePrinter();
    }
    return dr().mPrBuffer;
  }

  protected void setDatSubdir(String relPath) {
    dr().mDatSubdirRelPath = relPath;
  }

  private DatRecord dr() {
    if (mDatRecord == null) {
      mDatRecord = new DatRecord();
      mDatRecords.add(mDatRecord);
    }
    return mDatRecord;
  }

  private DatRecord mDatRecord;

  private void generateDirs() {
    if (dr().mDatSubdirRelPath == null)
      setDatSubdir(Utils.GEN_SUBDIR_NAME);
    if (dr().mDatSubdirRelPath.isEmpty())
      dr().mDatSubdir = datFilesDir();
    else
      dr().mDatSubdir = Files.S.mkdirs(new File(datFilesDir(), dr().mDatSubdirRelPath));
  }

  private List<String> args() {
    return mArgs;
  }

  private void supplyTypeName(String name) {
    if (dr().mTypeName == null)
      dr().mTypeName = name;
  }

  private void writeDat(DatRecord ty) {
    String content = ty.mPrBuffer.toString();
    checkState(!content.isEmpty());
    File datFile = new File(ty.mDatSubdir,
        convertCamelToUnderscore(ty.mTypeName) + ParseTools.DOT_EXT_DATA_DEFINITION);
    Files.S.writeString(datFile, content);
  }

  protected final void startType(String name) {
    closeCurrentDat();
    dr().mTypeName = name;
  }

  private void closeCurrentDat() {
    if (mDatRecord != null) {
      supplyTypeName(name());
    }
    mDatRecord = null;
  }

  private static class DatRecord {
    BasePrinter mPrBuffer;
    File mDatSubdir;
    String mDatSubdirRelPath;
    String mTypeName;
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
      mSourceDir = Files.S.mkdirs(new File(generatedDir(), Utils.sourceFileExtension(mLanguage)));
    }
    return mSourceDir;
  }

  protected final void language(Language x) {
    mLanguage = x;
  }

  protected final void verboseNames() {
    mVerboseNames = true;
  }

  protected final void generateDummyDatFile(String name) {
    File datFile = new File(mDatRecord.mDatSubdir,
        convertCamelToUnderscore(name) + ParseTools.DOT_EXT_DATA_DEFINITION);
    checkState(!datFile.exists());
    Files.S.writeString(datFile, "fields{int unused;}");
  }

  private Language mLanguage = Language.JAVA;
  private boolean mVerboseNames;

  private File mSourceDir;
  private File mDatFilesDir;
  // ------------------------------------------------------------------

  private List<String> mArgs = arrayList();

  // List of completed dat records
  //
  private List<DatRecord> mDatRecords = arrayList();

}
