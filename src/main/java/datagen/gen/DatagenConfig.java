package datagen.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class DatagenConfig implements AbstractData {

  public File startDir() {
    return mStartDir;
  }

  public File datPath() {
    return mDatPath;
  }

  public Language language() {
    return mLanguage;
  }

  public File sourcePath() {
    return mSourcePath;
  }

  public boolean clean() {
    return mClean;
  }

  public boolean deleteOld() {
    return mDeleteOld;
  }

  public boolean treatWarningsAsErrors() {
    return mTreatWarningsAsErrors;
  }

  public boolean comments() {
    return mComments;
  }

  public File pythonSourcePath() {
    return mPythonSourcePath;
  }

  public boolean verboseNames() {
    return mVerboseNames;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String START_DIR = "start_dir";
  protected static final String DAT_PATH = "dat_path";
  protected static final String LANGUAGE = "language";
  protected static final String SOURCE_PATH = "source_path";
  protected static final String CLEAN = "clean";
  protected static final String DELETE_OLD = "delete_old";
  protected static final String TREAT_WARNINGS_AS_ERRORS = "treat_warnings_as_errors";
  protected static final String COMMENTS = "comments";
  protected static final String PYTHON_SOURCE_PATH = "python_source_path";
  protected static final String VERBOSE_NAMES = "verbose_names";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.put(START_DIR, mStartDir.toString());
    m.put(DAT_PATH, mDatPath.toString());
    m.put(LANGUAGE, mLanguage.toString().toLowerCase());
    m.put(SOURCE_PATH, mSourcePath.toString());
    m.put(CLEAN, mClean);
    m.put(DELETE_OLD, mDeleteOld);
    m.put(TREAT_WARNINGS_AS_ERRORS, mTreatWarningsAsErrors);
    m.put(COMMENTS, mComments);
    m.put(PYTHON_SOURCE_PATH, mPythonSourcePath.toString());
    m.put(VERBOSE_NAMES, mVerboseNames);
    return m;
  }

  @Override
  public DatagenConfig build() {
    return this;
  }

  @Override
  public DatagenConfig parse(Object obj) {
    return new DatagenConfig((JSMap) obj);
  }

  private DatagenConfig(JSMap m) {
    {
      mStartDir = Files.DEFAULT;
      String x = m.opt(START_DIR, (String) null);
      if (x != null) {
        mStartDir = new File(x);
      }
    }
    {
      mDatPath = DEF_DAT_PATH;
      String x = m.opt(DAT_PATH, (String) null);
      if (x != null) {
        mDatPath = new File(x);
      }
    }
    {
      String x = m.opt(LANGUAGE, "");
      mLanguage = x.isEmpty() ? Language.DEFAULT_INSTANCE : Language.valueOf(x.toUpperCase());
    }
    {
      mSourcePath = Files.DEFAULT;
      String x = m.opt(SOURCE_PATH, (String) null);
      if (x != null) {
        mSourcePath = new File(x);
      }
    }
    mClean = m.opt(CLEAN, false);
    mDeleteOld = m.opt(DELETE_OLD, false);
    mTreatWarningsAsErrors = m.opt(TREAT_WARNINGS_AS_ERRORS, false);
    mComments = m.opt(COMMENTS, false);
    {
      mPythonSourcePath = Files.DEFAULT;
      String x = m.opt(PYTHON_SOURCE_PATH, (String) null);
      if (x != null) {
        mPythonSourcePath = new File(x);
      }
    }
    mVerboseNames = m.opt(VERBOSE_NAMES, false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof DatagenConfig))
      return false;
    DatagenConfig other = (DatagenConfig) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mStartDir.equals(other.mStartDir)))
      return false;
    if (!(mDatPath.equals(other.mDatPath)))
      return false;
    if (!(mLanguage.equals(other.mLanguage)))
      return false;
    if (!(mSourcePath.equals(other.mSourcePath)))
      return false;
    if (!(mClean == other.mClean))
      return false;
    if (!(mDeleteOld == other.mDeleteOld))
      return false;
    if (!(mTreatWarningsAsErrors == other.mTreatWarningsAsErrors))
      return false;
    if (!(mComments == other.mComments))
      return false;
    if (!(mPythonSourcePath.equals(other.mPythonSourcePath)))
      return false;
    if (!(mVerboseNames == other.mVerboseNames))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mStartDir.hashCode();
      r = r * 37 + mDatPath.hashCode();
      r = r * 37 + mLanguage.ordinal();
      r = r * 37 + mSourcePath.hashCode();
      r = r * 37 + (mClean ? 1 : 0);
      r = r * 37 + (mDeleteOld ? 1 : 0);
      r = r * 37 + (mTreatWarningsAsErrors ? 1 : 0);
      r = r * 37 + (mComments ? 1 : 0);
      r = r * 37 + mPythonSourcePath.hashCode();
      r = r * 37 + (mVerboseNames ? 1 : 0);
      m__hashcode = r;
    }
    return r;
  }

  protected File mStartDir;
  protected File mDatPath;
  protected Language mLanguage;
  protected File mSourcePath;
  protected boolean mClean;
  protected boolean mDeleteOld;
  protected boolean mTreatWarningsAsErrors;
  protected boolean mComments;
  protected File mPythonSourcePath;
  protected boolean mVerboseNames;
  protected int m__hashcode;

  public static final class Builder extends DatagenConfig {

    private Builder(DatagenConfig m) {
      mStartDir = m.mStartDir;
      mDatPath = m.mDatPath;
      mLanguage = m.mLanguage;
      mSourcePath = m.mSourcePath;
      mClean = m.mClean;
      mDeleteOld = m.mDeleteOld;
      mTreatWarningsAsErrors = m.mTreatWarningsAsErrors;
      mComments = m.mComments;
      mPythonSourcePath = m.mPythonSourcePath;
      mVerboseNames = m.mVerboseNames;
    }

    @Override
    public Builder toBuilder() {
      return this;
    }

    @Override
    public int hashCode() {
      m__hashcode = 0;
      return super.hashCode();
    }

    @Override
    public DatagenConfig build() {
      DatagenConfig r = new DatagenConfig();
      r.mStartDir = mStartDir;
      r.mDatPath = mDatPath;
      r.mLanguage = mLanguage;
      r.mSourcePath = mSourcePath;
      r.mClean = mClean;
      r.mDeleteOld = mDeleteOld;
      r.mTreatWarningsAsErrors = mTreatWarningsAsErrors;
      r.mComments = mComments;
      r.mPythonSourcePath = mPythonSourcePath;
      r.mVerboseNames = mVerboseNames;
      return r;
    }

    public Builder startDir(File x) {
      mStartDir = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder datPath(File x) {
      mDatPath = (x == null) ? DEF_DAT_PATH : x;
      return this;
    }

    public Builder language(Language x) {
      mLanguage = (x == null) ? Language.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder sourcePath(File x) {
      mSourcePath = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder clean(boolean x) {
      mClean = x;
      return this;
    }

    public Builder deleteOld(boolean x) {
      mDeleteOld = x;
      return this;
    }

    public Builder treatWarningsAsErrors(boolean x) {
      mTreatWarningsAsErrors = x;
      return this;
    }

    public Builder comments(boolean x) {
      mComments = x;
      return this;
    }

    public Builder pythonSourcePath(File x) {
      mPythonSourcePath = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder verboseNames(boolean x) {
      mVerboseNames = x;
      return this;
    }

  }

  private static final File DEF_DAT_PATH = new File("dat_files");

  public static final DatagenConfig DEFAULT_INSTANCE = new DatagenConfig();

  private DatagenConfig() {
    mStartDir = Files.DEFAULT;
    mDatPath = DEF_DAT_PATH;
    mLanguage = Language.DEFAULT_INSTANCE;
    mSourcePath = Files.DEFAULT;
    mPythonSourcePath = Files.DEFAULT;
  }

}
