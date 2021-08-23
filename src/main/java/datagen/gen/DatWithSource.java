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
public class DatWithSource implements AbstractData {

  // Field 'getters'

  public String datRelPath() {
    return mDatRelPath;
  }

  public String sourceRelPath() {
    return mSourceRelPath;
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

  public static final String DAT_REL_PATH = "dat_rel_path";
  public static final String SOURCE_REL_PATH = "source_rel_path";

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
    m.put(DAT_REL_PATH, mDatRelPath);
    m.put(SOURCE_REL_PATH, mSourceRelPath);
    return m;
  }

  /**
   * The instance is already immutable, so return unchanged
   */
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

    /**
     * Create an immutable version of this builder.  Where appropriate, defensive, immutable copies
     * are made of the builder fields.
     */
    @Override
    public DatWithSource build() {
      DatWithSource r = new DatWithSource();
      r.mDatRelPath = mDatRelPath;
      r.mSourceRelPath = mSourceRelPath;
      return r;
    }

    // Field 'setters'.  Where appropriate, if an argument is immutable, a mutable copy is stored instead

    public Builder datRelPath(String x) {
      mDatRelPath = (x == null) ? "" : x;
      return this;
    }

    public Builder sourceRelPath(String x) {
      mSourceRelPath = (x == null) ? "" : x;
      return this;
    }

  }

  /**
   * The default (immutable) instance of this data object
   */
  public static final DatWithSource DEFAULT_INSTANCE = new DatWithSource();

  /**
   * The private constructor.  To create new instances, use newBuilder()
   */
  private DatWithSource() {
    mDatRelPath = "";
    mSourceRelPath = "";
  }

}
