package datagen.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class QualifiedName implements AbstractData {

  public String packagePath() {
    return mPackagePath;
  }

  public String className() {
    return mClassName;
  }

  public String combined() {
    return mCombined;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "package_path";
  protected static final String _1 = "class_name";
  protected static final String _2 = "combined";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mPackagePath);
    m.putUnsafe(_1, mClassName);
    m.putUnsafe(_2, mCombined);
    return m;
  }

  @Override
  public QualifiedName build() {
    return this;
  }

  @Override
  public QualifiedName parse(Object obj) {
    return new QualifiedName((JSMap) obj);
  }

  private QualifiedName(JSMap m) {
    mPackagePath = m.opt(_0, "");
    mClassName = m.opt(_1, "");
    mCombined = m.opt(_2, "");
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof QualifiedName))
      return false;
    QualifiedName other = (QualifiedName) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mPackagePath.equals(other.mPackagePath)))
      return false;
    if (!(mClassName.equals(other.mClassName)))
      return false;
    if (!(mCombined.equals(other.mCombined)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mPackagePath.hashCode();
      r = r * 37 + mClassName.hashCode();
      r = r * 37 + mCombined.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mPackagePath;
  protected String mClassName;
  protected String mCombined;
  protected int m__hashcode;

  public static final class Builder extends QualifiedName {

    private Builder(QualifiedName m) {
      mPackagePath = m.mPackagePath;
      mClassName = m.mClassName;
      mCombined = m.mCombined;
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
    public QualifiedName build() {
      QualifiedName r = new QualifiedName();
      r.mPackagePath = mPackagePath;
      r.mClassName = mClassName;
      r.mCombined = mCombined;
      return r;
    }

    public Builder packagePath(String x) {
      mPackagePath = (x == null) ? "" : x;
      return this;
    }

    public Builder className(String x) {
      mClassName = (x == null) ? "" : x;
      return this;
    }

    public Builder combined(String x) {
      mCombined = (x == null) ? "" : x;
      return this;
    }

  }

  public static final QualifiedName DEFAULT_INSTANCE = new QualifiedName();

  private QualifiedName() {
    mPackagePath = "";
    mClassName = "";
    mCombined = "";
  }

}
