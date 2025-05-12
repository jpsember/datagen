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

  public boolean quietMode() {
    return mQuietMode;
  }

  public boolean format() {
    return mFormat;
  }

  public boolean dbsim() {
    return mDbsim;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "start_dir";
  protected static final String _1 = "dat_path";
  protected static final String _2 = "language";
  protected static final String _3 = "source_path";
  protected static final String _4 = "clean";
  protected static final String _5 = "delete_old";
  protected static final String _6 = "treat_warnings_as_errors";
  protected static final String _7 = "comments";
  protected static final String _8 = "python_source_path";
  protected static final String _9 = "verbose_names";
  protected static final String _10 = "quiet_mode";
  protected static final String _11 = "format";
  protected static final String _12 = "dbsim";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mStartDir.toString());
    m.putUnsafe(_1, mDatPath.toString());
    m.putUnsafe(_2, mLanguage.toString().toLowerCase());
    m.putUnsafe(_3, mSourcePath.toString());
    m.putUnsafe(_4, mClean);
    m.putUnsafe(_5, mDeleteOld);
    m.putUnsafe(_6, mTreatWarningsAsErrors);
    m.putUnsafe(_7, mComments);
    m.putUnsafe(_8, mPythonSourcePath.toString());
    m.putUnsafe(_9, mVerboseNames);
    m.putUnsafe(_10, mQuietMode);
    m.putUnsafe(_11, mFormat);
    m.putUnsafe(_12, mDbsim);
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
      String x = m.opt(_0, (String) null);
      if (x != null) {
        mStartDir = new File(x);
      }
    }
    {
      mDatPath = _D1;
      String x = m.opt(_1, (String) null);
      if (x != null) {
        mDatPath = new File(x);
      }
    }
    {
      String x = m.opt(_2, "");
      mLanguage = x.isEmpty() ? Language.DEFAULT_INSTANCE : Language.valueOf(x.toUpperCase());
    }
    {
      mSourcePath = Files.DEFAULT;
      String x = m.opt(_3, (String) null);
      if (x != null) {
        mSourcePath = new File(x);
      }
    }
    mClean = m.opt(_4, false);
    mDeleteOld = m.opt(_5, false);
    mTreatWarningsAsErrors = m.opt(_6, false);
    mComments = m.opt(_7, false);
    {
      mPythonSourcePath = Files.DEFAULT;
      String x = m.opt(_8, (String) null);
      if (x != null) {
        mPythonSourcePath = new File(x);
      }
    }
    mVerboseNames = m.opt(_9, false);
    mQuietMode = m.opt(_10, false);
    mFormat = m.opt(_11, false);
    mDbsim = m.opt(_12, false);
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
    if (!(mQuietMode == other.mQuietMode))
      return false;
    if (!(mFormat == other.mFormat))
      return false;
    if (!(mDbsim == other.mDbsim))
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
      r = r * 37 + (mQuietMode ? 1 : 0);
      r = r * 37 + (mFormat ? 1 : 0);
      r = r * 37 + (mDbsim ? 1 : 0);
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
  protected boolean mQuietMode;
  protected boolean mFormat;
  protected boolean mDbsim;
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
      mQuietMode = m.mQuietMode;
      mFormat = m.mFormat;
      mDbsim = m.mDbsim;
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
      r.mQuietMode = mQuietMode;
      r.mFormat = mFormat;
      r.mDbsim = mDbsim;
      return r;
    }

    public Builder startDir(File x) {
      mStartDir = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder datPath(File x) {
      mDatPath = (x == null) ? _D1 : x;
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

    public Builder quietMode(boolean x) {
      mQuietMode = x;
      return this;
    }

    public Builder format(boolean x) {
      mFormat = x;
      return this;
    }

    public Builder dbsim(boolean x) {
      mDbsim = x;
      return this;
    }

  }

  private static final File _D1 = new File("dat_files");

  public static final DatagenConfig DEFAULT_INSTANCE = new DatagenConfig();

  private DatagenConfig() {
    mStartDir = Files.DEFAULT;
    mDatPath = _D1;
    mLanguage = Language.DEFAULT_INSTANCE;
    mSourcePath = Files.DEFAULT;
    mPythonSourcePath = Files.DEFAULT;
  }

}
