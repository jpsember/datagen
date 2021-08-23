package datagen.gen;

import js.data.AbstractData;
import js.json.JSMap;

/**
 * Generated Java data class (do not edit!)
 *
 * Instances of this class should be considered immutable.  A mutable copy of an instance
 * can be constructed by calling the toBuilder() method.  When clients pass instances to other
 * code, if mutation of those instances is not desired, then the client should ensure that the
 * instance is not actually a Builder (e.g. by calling build() if necessary).
 */
public class QualifiedName implements AbstractData {

  // Field 'getters'

  public String packagePath() {
    return mPackagePath;
  }

  public String className() {
    return mClassName;
  }

  public String combined() {
    return mCombined;
  }

  /**
   * Construct a builder from this data class object.
   * Where appropriate, this object's values are defensively copied to mutable versions
   */
  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  // Constants used to refer to the fields, e.g., as they appear in json maps

  public static final String PACKAGE_PATH = "package_path";
  public static final String CLASS_NAME = "class_name";
  public static final String COMBINED = "combined";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  /**
   * Serialize this object to a json map
   */
  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.put(PACKAGE_PATH, mPackagePath);
    m.put(CLASS_NAME, mClassName);
    m.put(COMBINED, mCombined);
    return m;
  }

  /**
   * The instance is already immutable, so return unchanged
   */
  @Override
  public QualifiedName build() {
    return this;
  }

  @Override
  public QualifiedName parse(Object obj) {
    return new QualifiedName((JSMap) obj);
  }

  private QualifiedName(JSMap m) {
    mPackagePath = m.opt(PACKAGE_PATH, "");
    mClassName = m.opt(CLASS_NAME, "");
    mCombined = m.opt(COMBINED, "");
  }

  /**
   * Construct a new builder for objects of this data class
   */
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

    /**
     * Create an immutable version of this builder.  Where appropriate, defensive, immutable copies
     * are made of the builder fields.
     */
    @Override
    public QualifiedName build() {
      QualifiedName r = new QualifiedName();
      r.mPackagePath = mPackagePath;
      r.mClassName = mClassName;
      r.mCombined = mCombined;
      return r;
    }

    // Field 'setters'.  Where appropriate, if an argument is immutable, a mutable copy is stored instead

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

  /**
   * The default (immutable) instance of this data object
   */
  public static final QualifiedName DEFAULT_INSTANCE = new QualifiedName();

  /**
   * The private constructor.  To create new instances, use newBuilder()
   */
  private QualifiedName() {
    mPackagePath = "";
    mClassName = "";
    mCombined = "";
  }

}
