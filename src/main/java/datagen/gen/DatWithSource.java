package datagen.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class DatWithSource implements AbstractData {

  public String sourceRelPath() {
    return mSourceRelPath;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "source_rel_path";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mSourceRelPath);
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
    mSourceRelPath = m.opt(_0, "");
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
    if (!(mSourceRelPath.equals(other.mSourceRelPath)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mSourceRelPath.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mSourceRelPath;
  protected int m__hashcode;

  public static final class Builder extends DatWithSource {

    private Builder(DatWithSource m) {
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
      r.mSourceRelPath = mSourceRelPath;
      return r;
    }

    public Builder sourceRelPath(String x) {
      mSourceRelPath = (x == null) ? "" : x;
      return this;
    }

  }

  public static final DatWithSource DEFAULT_INSTANCE = new DatWithSource();

  private DatWithSource() {
    mSourceRelPath = "";
  }

}
