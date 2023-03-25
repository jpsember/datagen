package datagen.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class PartialType implements AbstractData {

  public String name() {
    return mName;
  }

  public boolean enumFlag() {
    return mEnumFlag;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String NAME = "name";
  protected static final String ENUM_FLAG = "enum_flag";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(NAME, mName);
    m.putUnsafe(ENUM_FLAG, mEnumFlag);
    return m;
  }

  @Override
  public PartialType build() {
    return this;
  }

  @Override
  public PartialType parse(Object obj) {
    return new PartialType((JSMap) obj);
  }

  private PartialType(JSMap m) {
    mName = m.opt(NAME, "");
    mEnumFlag = m.opt(ENUM_FLAG, false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof PartialType))
      return false;
    PartialType other = (PartialType) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mName.equals(other.mName)))
      return false;
    if (!(mEnumFlag == other.mEnumFlag))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mName.hashCode();
      r = r * 37 + (mEnumFlag ? 1 : 0);
      m__hashcode = r;
    }
    return r;
  }

  protected String mName;
  protected boolean mEnumFlag;
  protected int m__hashcode;

  public static final class Builder extends PartialType {

    private Builder(PartialType m) {
      mName = m.mName;
      mEnumFlag = m.mEnumFlag;
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
    public PartialType build() {
      PartialType r = new PartialType();
      r.mName = mName;
      r.mEnumFlag = mEnumFlag;
      return r;
    }

    public Builder name(String x) {
      mName = (x == null) ? "" : x;
      return this;
    }

    public Builder enumFlag(boolean x) {
      mEnumFlag = x;
      return this;
    }

  }

  public static final PartialType DEFAULT_INSTANCE = new PartialType();

  private PartialType() {
    mName = "";
  }

}
