package datagen.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class DatWithSource implements AbstractData {

  public String datRelPath() {
    return mDatRelPath;
  }

  public String sourceRelPath() {
    return mSourceRelPath;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String DAT_REL_PATH = "dat_rel_path";
  protected static final String SOURCE_REL_PATH = "source_rel_path";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(DAT_REL_PATH, mDatRelPath);
    m.putUnsafe(SOURCE_REL_PATH, mSourceRelPath);
    return m;
  }

  @Override
  public DatWithSource build() {
    return this;
  }

  @Override
  public DatWithSource parse(Object obj) {
    return new DatWithSource((JSMap) obj);
  }

  private DatWithSource(JSMap m) {
    mDatRelPath = m.opt(DAT_REL_PATH, "");
    mSourceRelPath = m.opt(SOURCE_REL_PATH, "");
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof DatWithSource))
      return false;
    DatWithSource other = (DatWithSource) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mDatRelPath.equals(other.mDatRelPath)))
      return false;
    if (!(mSourceRelPath.equals(other.mSourceRelPath)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mDatRelPath.hashCode();
      r = r * 37 + mSourceRelPath.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mDatRelPath;
  protected String mSourceRelPath;
  protected int m__hashcode;

  public static final class Builder extends DatWithSource {

    private Builder(DatWithSource m) {
      mDatRelPath = m.mDatRelPath;
      mSourceRelPath = m.mSourceRelPath;
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
    public DatWithSource build() {
      DatWithSource r = new DatWithSource();
      r.mDatRelPath = mDatRelPath;
      r.mSourceRelPath = mSourceRelPath;
      return r;
    }

    public Builder datRelPath(String x) {
      mDatRelPath = (x == null) ? "" : x;
      return this;
    }

    public Builder sourceRelPath(String x) {
      mSourceRelPath = (x == null) ? "" : x;
      return this;
    }

  }

  public static final DatWithSource DEFAULT_INSTANCE = new DatWithSource();

  private DatWithSource() {
    mDatRelPath = "";
    mSourceRelPath = "";
  }

}
