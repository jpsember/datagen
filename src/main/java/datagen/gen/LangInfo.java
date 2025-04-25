package datagen.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class LangInfo implements AbstractData {

  public Language language() {
    return mLanguage;
  }

  public File sentinelFile() {
    return mSentinelFile;
  }

  public int depth() {
    return mDepth;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "language";
  protected static final String _1 = "sentinel_file";
  protected static final String _2 = "depth";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mLanguage.toString().toLowerCase());
    m.putUnsafe(_1, mSentinelFile.toString());
    m.putUnsafe(_2, mDepth);
    return m;
  }

  @Override
  public LangInfo build() {
    return this;
  }

  @Override
  public LangInfo parse(Object obj) {
    return new LangInfo((JSMap) obj);
  }

  private LangInfo(JSMap m) {
    {
      String x = m.opt(_0, "");
      mLanguage = x.isEmpty() ? Language.DEFAULT_INSTANCE : Language.valueOf(x.toUpperCase());
    }
    {
      mSentinelFile = Files.DEFAULT;
      String x = m.opt(_1, (String) null);
      if (x != null) {
        mSentinelFile = new File(x);
      }
    }
    mDepth = m.opt(_2, 0);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof LangInfo))
      return false;
    LangInfo other = (LangInfo) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mLanguage.equals(other.mLanguage)))
      return false;
    if (!(mSentinelFile.equals(other.mSentinelFile)))
      return false;
    if (!(mDepth == other.mDepth))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mLanguage.ordinal();
      r = r * 37 + mSentinelFile.hashCode();
      r = r * 37 + mDepth;
      m__hashcode = r;
    }
    return r;
  }

  protected Language mLanguage;
  protected File mSentinelFile;
  protected int mDepth;
  protected int m__hashcode;

  public static final class Builder extends LangInfo {

    private Builder(LangInfo m) {
      mLanguage = m.mLanguage;
      mSentinelFile = m.mSentinelFile;
      mDepth = m.mDepth;
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
    public LangInfo build() {
      LangInfo r = new LangInfo();
      r.mLanguage = mLanguage;
      r.mSentinelFile = mSentinelFile;
      r.mDepth = mDepth;
      return r;
    }

    public Builder language(Language x) {
      mLanguage = (x == null) ? Language.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder sentinelFile(File x) {
      mSentinelFile = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder depth(int x) {
      mDepth = x;
      return this;
    }

  }

  public static final LangInfo DEFAULT_INSTANCE = new LangInfo();

  private LangInfo() {
    mLanguage = Language.DEFAULT_INSTANCE;
    mSentinelFile = Files.DEFAULT;
  }

}
