package datagen.gen;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

public class SampleDataType implements AbstractData {

  public byte b1() {
    return mB1;
  }

  public byte[] b3() {
    return mB3;
  }

  public short s1() {
    return mS1;
  }

  public short[] s3() {
    return mS3;
  }

  public int i1() {
    return mI1;
  }

  public int[] i3() {
    return mI3;
  }

  public long l1() {
    return mL1;
  }

  public long[] l3() {
    return mL3;
  }

  public float f1() {
    return mF1;
  }

  public float[] f3() {
    return mF3;
  }

  public double d1() {
    return mD1;
  }

  public double[] d3() {
    return mD3;
  }

  public Set<File> s() {
    return mS;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "b1";
  protected static final String _1 = "b3";
  protected static final String _2 = "s1";
  protected static final String _3 = "s3";
  protected static final String _4 = "i1";
  protected static final String _5 = "i3";
  protected static final String _6 = "l1";
  protected static final String _7 = "l3";
  protected static final String _8 = "f1";
  protected static final String _9 = "f3";
  protected static final String _10 = "d1";
  protected static final String _11 = "d3";
  protected static final String _12 = "s";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mB1);
    m.putUnsafe(_1, DataUtil.encodeBase64Maybe(mB3));
    m.putUnsafe(_2, mS1);
    m.putUnsafe(_3, DataUtil.encodeBase64Maybe(mS3));
    m.putUnsafe(_4, mI1);
    m.putUnsafe(_5, DataUtil.encodeBase64Maybe(mI3));
    m.putUnsafe(_6, mL1);
    m.putUnsafe(_7, DataUtil.encodeBase64Maybe(mL3));
    m.putUnsafe(_8, mF1);
    m.putUnsafe(_9, DataUtil.encodeBase64Maybe(mF3));
    m.putUnsafe(_10, mD1);
    m.putUnsafe(_11, DataUtil.encodeBase64Maybe(mD3));
    {
      JSList j = new JSList();
      for (File e : mS)
        j.add(e.toString());
      m.put(_12, j);
    }
    return m;
  }

  @Override
  public SampleDataType build() {
    return this;
  }

  @Override
  public SampleDataType parse(Object obj) {
    return new SampleDataType((JSMap) obj);
  }

  private SampleDataType(JSMap m) {
    mB1 = m.opt(_0, (byte) 0);
    {
      mB3 = DataUtil.EMPTY_BYTE_ARRAY;
      Object x = m.optUnsafe(_1);
      if (x != null) {
        mB3 = DataUtil.parseBytesFromArrayOrBase64(x);
      }
    }
    mS1 = m.opt(_2, (short) 0);
    {
      mS3 = DataUtil.EMPTY_SHORT_ARRAY;
      Object x = m.optUnsafe(_3);
      if (x != null) {
        mS3 = DataUtil.parseShortsFromArrayOrBase64(x);
      }
    }
    mI1 = m.opt(_4, 0);
    {
      mI3 = DataUtil.EMPTY_INT_ARRAY;
      Object x = m.optUnsafe(_5);
      if (x != null) {
        mI3 = DataUtil.parseIntsFromArrayOrBase64(x);
      }
    }
    mL1 = m.opt(_6, 0L);
    {
      mL3 = DataUtil.EMPTY_LONG_ARRAY;
      Object x = m.optUnsafe(_7);
      if (x != null) {
        mL3 = DataUtil.parseLongsFromArrayOrBase64(x);
      }
    }
    mF1 = m.opt(_8, 0f);
    {
      mF3 = DataUtil.EMPTY_FLOAT_ARRAY;
      Object x = m.optUnsafe(_9);
      if (x != null) {
        mF3 = DataUtil.parseFloatsFromArrayOrBase64(x);
      }
    }
    mD1 = m.opt(_10, 0.0);
    {
      mD3 = DataUtil.EMPTY_DOUBLE_ARRAY;
      Object x = m.optUnsafe(_11);
      if (x != null) {
        mD3 = DataUtil.parseDoublesFromArrayOrBase64(x);
      }
    }
    {
      mS = DataUtil.emptySet();
      {
        JSList m2 = m.optJSList("s");
        if (m2 != null && !m2.isEmpty()) {
          Set<File> mp = new HashSet<>();
          for (Object e : m2.wrappedList())
            mp.add(new File((String) e));
          mS = mp;
        }
      }
    }
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof SampleDataType))
      return false;
    SampleDataType other = (SampleDataType) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mB1 == other.mB1))
      return false;
    if (!(Arrays.equals(mB3, other.mB3)))
      return false;
    if (!(mS1 == other.mS1))
      return false;
    if (!(Arrays.equals(mS3, other.mS3)))
      return false;
    if (!(mI1 == other.mI1))
      return false;
    if (!(Arrays.equals(mI3, other.mI3)))
      return false;
    if (!(mL1 == other.mL1))
      return false;
    if (!(Arrays.equals(mL3, other.mL3)))
      return false;
    if (!(mF1 == other.mF1))
      return false;
    if (!(Arrays.equals(mF3, other.mF3)))
      return false;
    if (!(mD1 == other.mD1))
      return false;
    if (!(Arrays.equals(mD3, other.mD3)))
      return false;
    if (!(mS.equals(other.mS)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mB1;
      r = r * 37 + Arrays.hashCode(mB3);
      r = r * 37 + mS1;
      r = r * 37 + Arrays.hashCode(mS3);
      r = r * 37 + mI1;
      r = r * 37 + Arrays.hashCode(mI3);
      r = r * 37 + (int)mL1;
      r = r * 37 + Arrays.hashCode(mL3);
      r = r * 37 + (int)mF1;
      r = r * 37 + Arrays.hashCode(mF3);
      r = r * 37 + (int) mD1;
      r = r * 37 + Arrays.hashCode(mD3);
      r = r * 37 + mS.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected byte mB1;
  protected byte[] mB3;
  protected short mS1;
  protected short[] mS3;
  protected int mI1;
  protected int[] mI3;
  protected long mL1;
  protected long[] mL3;
  protected float mF1;
  protected float[] mF3;
  protected double mD1;
  protected double[] mD3;
  protected Set<File> mS;
  protected int m__hashcode;

  public static final class Builder extends SampleDataType {

    private Builder(SampleDataType m) {
      mB1 = m.mB1;
      mB3 = m.mB3;
      mS1 = m.mS1;
      mS3 = m.mS3;
      mI1 = m.mI1;
      mI3 = m.mI3;
      mL1 = m.mL1;
      mL3 = m.mL3;
      mF1 = m.mF1;
      mF3 = m.mF3;
      mD1 = m.mD1;
      mD3 = m.mD3;
      mS = m.mS;
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
    public SampleDataType build() {
      SampleDataType r = new SampleDataType();
      r.mB1 = mB1;
      r.mB3 = mB3;
      r.mS1 = mS1;
      r.mS3 = mS3;
      r.mI1 = mI1;
      r.mI3 = mI3;
      r.mL1 = mL1;
      r.mL3 = mL3;
      r.mF1 = mF1;
      r.mF3 = mF3;
      r.mD1 = mD1;
      r.mD3 = mD3;
      r.mS = mS;
      return r;
    }

    public Builder b1(byte x) {
      mB1 = x;
      return this;
    }

    public Builder b3(byte[] x) {
      mB3 = (x == null) ? DataUtil.EMPTY_BYTE_ARRAY : x;
      return this;
    }

    public Builder s1(short x) {
      mS1 = x;
      return this;
    }

    public Builder s3(short[] x) {
      mS3 = (x == null) ? DataUtil.EMPTY_SHORT_ARRAY : x;
      return this;
    }

    public Builder i1(int x) {
      mI1 = x;
      return this;
    }

    public Builder i3(int[] x) {
      mI3 = (x == null) ? DataUtil.EMPTY_INT_ARRAY : x;
      return this;
    }

    public Builder l1(long x) {
      mL1 = x;
      return this;
    }

    public Builder l3(long[] x) {
      mL3 = (x == null) ? DataUtil.EMPTY_LONG_ARRAY : x;
      return this;
    }

    public Builder f1(float x) {
      mF1 = x;
      return this;
    }

    public Builder f3(float[] x) {
      mF3 = (x == null) ? DataUtil.EMPTY_FLOAT_ARRAY : x;
      return this;
    }

    public Builder d1(double x) {
      mD1 = x;
      return this;
    }

    public Builder d3(double[] x) {
      mD3 = (x == null) ? DataUtil.EMPTY_DOUBLE_ARRAY : x;
      return this;
    }

    public Builder s(Set<File> x) {
      mS = (x == null) ? DataUtil.emptySet() : x;
      return this;
    }

  }

  public static final SampleDataType DEFAULT_INSTANCE = new SampleDataType();

  private SampleDataType() {
    mB3 = DataUtil.EMPTY_BYTE_ARRAY;
    mS3 = DataUtil.EMPTY_SHORT_ARRAY;
    mI3 = DataUtil.EMPTY_INT_ARRAY;
    mL3 = DataUtil.EMPTY_LONG_ARRAY;
    mF3 = DataUtil.EMPTY_FLOAT_ARRAY;
    mD3 = DataUtil.EMPTY_DOUBLE_ARRAY;
    mS = DataUtil.emptySet();
  }

}
