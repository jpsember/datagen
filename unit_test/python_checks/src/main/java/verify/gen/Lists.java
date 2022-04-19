package verify.gen;

import java.util.Arrays;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

public class Lists implements AbstractData {

  public int[] ints() {
    return m0;
  }

  public int[] intsopt() {
    return m1;
  }

  public List<String> strings() {
    return m2;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "ints";
  protected static final String _1 = "intsopt";
  protected static final String _2 = "strings";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.put(_0, DataUtil.encodeBase64(m0));
    if (m1 != null) {
      m.put(_1, DataUtil.encodeBase64(m1));
    }
    {
      JSList j = new JSList();
      for (String x : m2)
        j.add(x);
      m.put(_2, j);
    }
    return m;
  }

  @Override
  public Lists build() {
    return this;
  }

  @Override
  public Lists parse(Object obj) {
    return new Lists((JSMap) obj);
  }

  private Lists(JSMap m) {
    {
      m0 = DataUtil.EMPTY_INT_ARRAY;
      String x = (String) m.optUnsafe(_0);
      if (x != null) {
        m0 = DataUtil.parseBase64Ints(x);
      }
    }
    {
      String x = (String) m.optUnsafe(_1);
      if (x != null) {
        m1 = DataUtil.parseBase64Ints(x);
      }
    }
    m2 = DataUtil.parseListOfObjects(m.optJSList(_2), false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof Lists))
      return false;
    Lists other = (Lists) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(Arrays.equals(m0, other.m0)))
      return false;
    if ((m1 == null) ^ (other.m1 == null))
      return false;
    if (m1 != null) {
      if (!(Arrays.equals(m1, other.m1)))
        return false;
    }
    if (!(m2.equals(other.m2)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + Arrays.hashCode(m0);
      if (m1 != null) {
        r = r * 37 + Arrays.hashCode(m1);
      }
      for (String x : m2)
        if (x != null)
          r = r * 37 + x.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected int[] m0;
  protected int[] m1;
  protected List<String> m2;
  protected int m__hashcode;

  public static final class Builder extends Lists {

    private Builder(Lists m) {
      m0 = m.m0;
      m1 = m.m1;
      m2 = DataUtil.mutableCopyOf(m.m2);
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
    public Lists build() {
      Lists r = new Lists();
      r.m0 = m0;
      r.m1 = m1;
      r.m2 = DataUtil.immutableCopyOf(m2);
      return r;
    }

    public Builder ints(int[] x) {
      m0 = (x == null) ? DataUtil.EMPTY_INT_ARRAY : x;
      return this;
    }

    public Builder intsopt(int[] x) {
      m1 = (x == null) ? null : x;
      return this;
    }

    public Builder strings(List<String> x) {
      m2 = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

  }

  public static final Lists DEFAULT_INSTANCE = new Lists();

  private Lists() {
    m0 = DataUtil.EMPTY_INT_ARRAY;
    m2 = DataUtil.emptyList();
  }

}
