package datagen.gen;

import java.util.Arrays;
import js.data.AbstractData;
import js.data.DataUtil;
import js.data.DoubleArray;
import js.json.JSList;
import js.json.JSMap;

public class SampleDataType implements AbstractData {

  public byte b1() {
    return mB1;
  }

  public Byte b2() {
    return mB2;
  }

  public byte[] b3() {
    return mB3;
  }

  public byte[] b4() {
    return mB4;
  }

  public short s1() {
    return mS1;
  }

  public Short s2() {
    return mS2;
  }

  public short[] s3() {
    return mS3;
  }

  public short[] s4() {
    return mS4;
  }

  public int i1() {
    return mI1;
  }

  public Integer i2() {
    return mI2;
  }

  public int[] i3() {
    return mI3;
  }

  public int[] i4() {
    return mI4;
  }

  public long l1() {
    return mL1;
  }

  public Long l2() {
    return mL2;
  }

  public long[] l3() {
    return mL3;
  }

  public long[] l4() {
    return mL4;
  }

  public float f1() {
    return mF1;
  }

  public Float f2() {
    return mF2;
  }

  public float[] f3() {
    return mF3;
  }

  public float[] f4() {
    return mF4;
  }

  public double d1() {
    return mD1;
  }

  public Double d2() {
    return mD2;
  }

  public double[] d3() {
    return mD3;
  }

  public double[] d4() {
    return mD4;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String B1 = "b1";
  protected static final String B2 = "b2";
  protected static final String B3 = "b3";
  protected static final String B4 = "b4";
  protected static final String S1 = "s1";
  protected static final String S2 = "s2";
  protected static final String S3 = "s3";
  protected static final String S4 = "s4";
  protected static final String I1 = "i1";
  protected static final String I2 = "i2";
  protected static final String I3 = "i3";
  protected static final String I4 = "i4";
  protected static final String L1 = "l1";
  protected static final String L2 = "l2";
  protected static final String L3 = "l3";
  protected static final String L4 = "l4";
  protected static final String F1 = "f1";
  protected static final String F2 = "f2";
  protected static final String F3 = "f3";
  protected static final String F4 = "f4";
  protected static final String D1 = "d1";
  protected static final String D2 = "d2";
  protected static final String D3 = "d3";
  protected static final String D4 = "d4";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(B1, mB1);
    if (mB2 != null) {
      m.putUnsafe(B2, mB2);
    }
    m.putUnsafe(B3, DataUtil.encodeBase64Maybe(mB3));
    if (mB4 != null) {
      m.putUnsafe(B4, DataUtil.encodeBase64Maybe(mB4));
    }
    m.putUnsafe(S1, mS1);
    if (mS2 != null) {
      m.putUnsafe(S2, mS2);
    }
    m.putUnsafe(S3, DataUtil.encodeBase64Maybe(mS3));
    if (mS4 != null) {
      m.putUnsafe(S4, DataUtil.encodeBase64Maybe(mS4));
    }
    m.putUnsafe(I1, mI1);
    if (mI2 != null) {
      m.putUnsafe(I2, mI2);
    }
    m.putUnsafe(I3, DataUtil.encodeBase64Maybe(mI3));
    if (mI4 != null) {
      m.putUnsafe(I4, DataUtil.encodeBase64Maybe(mI4));
    }
    m.putUnsafe(L1, mL1);
    if (mL2 != null) {
      m.putUnsafe(L2, mL2);
    }
    m.putUnsafe(L3, DataUtil.encodeBase64Maybe(mL3));
    if (mL4 != null) {
      m.putUnsafe(L4, DataUtil.encodeBase64Maybe(mL4));
    }
    m.putUnsafe(F1, mF1);
    if (mF2 != null) {
      m.putUnsafe(F2, mF2);
    }
    m.putUnsafe(F3, DataUtil.encodeBase64Maybe(mF3));
    if (mF4 != null) {
      m.putUnsafe(F4, DataUtil.encodeBase64Maybe(mF4));
    }
    m.putUnsafe(D1, mD1);
    if (mD2 != null) {
      m.putUnsafe(D2, mD2);
    }
    m.putUnsafe(D3, DoubleArray.with(mD3).toJson());
    if (mD4 != null) {
      m.putUnsafe(D4, DoubleArray.with(mD4).toJson());
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
    mB1 = m.opt(B1, (byte) 0);
    mB2 = m.optByte(B2);
    {
      mB3 = DataUtil.EMPTY_BYTE_ARRAY;
      Object x = m.optUnsafe(B3);
      if (x != null) {
        mB3 = DataUtil.parseBytesFromArrayOrBase64(x);
      }
    }
    {
      Object x = m.optUnsafe(B4);
      if (x != null) {
        mB4 = DataUtil.parseBytesFromArrayOrBase64(x);
      }
    }
    mS1 = m.opt(S1, (short) 0);
    mS2 = m.optShort(S2);
    {
      mS3 = DataUtil.EMPTY_SHORT_ARRAY;
      Object x = m.optUnsafe(S3);
      if (x != null) {
        mS3 = DataUtil.parseShortsFromArrayOrBase64(x);
      }
    }
    {
      Object x = m.optUnsafe(S4);
      if (x != null) {
        mS4 = DataUtil.parseShortsFromArrayOrBase64(x);
      }
    }
    mI1 = m.opt(I1, 0);
    mI2 = m.optInt(I2);
    {
      mI3 = DataUtil.EMPTY_INT_ARRAY;
      Object x = m.optUnsafe(I3);
      if (x != null) {
        mI3 = DataUtil.parseIntsFromArrayOrBase64(x);
      }
    }
    {
      Object x = m.optUnsafe(I4);
      if (x != null) {
        mI4 = DataUtil.parseIntsFromArrayOrBase64(x);
      }
    }
    mL1 = m.opt(L1, 0L);
    mL2 = m.optLong(L2);
    {
      mL3 = DataUtil.EMPTY_LONG_ARRAY;
      Object x = m.optUnsafe(L3);
      if (x != null) {
        mL3 = DataUtil.parseLongsFromArrayOrBase64(x);
      }
    }
    {
      Object x = m.optUnsafe(L4);
      if (x != null) {
        mL4 = DataUtil.parseLongsFromArrayOrBase64(x);
      }
    }
    mF1 = m.opt(F1, 0f);
    mF2 = m.optFloat(F2);
    {
      mF3 = DataUtil.EMPTY_FLOAT_ARRAY;
      Object x = m.optUnsafe(F3);
      if (x != null) {
        mF3 = DataUtil.parseFloatsFromArrayOrBase64(x);
      }
    }
    {
      Object x = m.optUnsafe(F4);
      if (x != null) {
        mF4 = DataUtil.parseFloatsFromArrayOrBase64(x);
      }
    }
    mD1 = m.opt(D1, 0.0);
    mD2 = m.optDouble(D2);
    {
      mD3 = DataUtil.EMPTY_DOUBLE_ARRAY;
      JSList x = m.optJSList(D3);
      if (x != null) {
        mD3 = DoubleArray.DEFAULT_INSTANCE.parse(x).array();
      }
    }
    {
      JSList x = m.optJSList(D4);
      if (x != null) {
        mD4 = DoubleArray.DEFAULT_INSTANCE.parse(x).array();
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
    if ((mB2 == null) ^ (other.mB2 == null))
      return false;
    if (mB2 != null) {
      if (!(mB2.equals(other.mB2)))
        return false;
    }
    if (!(Arrays.equals(mB3, other.mB3)))
      return false;
    if ((mB4 == null) ^ (other.mB4 == null))
      return false;
    if (mB4 != null) {
      if (!(Arrays.equals(mB4, other.mB4)))
        return false;
    }
    if (!(mS1 == other.mS1))
      return false;
    if ((mS2 == null) ^ (other.mS2 == null))
      return false;
    if (mS2 != null) {
      if (!(mS2.equals(other.mS2)))
        return false;
    }
    if (!(Arrays.equals(mS3, other.mS3)))
      return false;
    if ((mS4 == null) ^ (other.mS4 == null))
      return false;
    if (mS4 != null) {
      if (!(Arrays.equals(mS4, other.mS4)))
        return false;
    }
    if (!(mI1 == other.mI1))
      return false;
    if ((mI2 == null) ^ (other.mI2 == null))
      return false;
    if (mI2 != null) {
      if (!(mI2.equals(other.mI2)))
        return false;
    }
    if (!(Arrays.equals(mI3, other.mI3)))
      return false;
    if ((mI4 == null) ^ (other.mI4 == null))
      return false;
    if (mI4 != null) {
      if (!(Arrays.equals(mI4, other.mI4)))
        return false;
    }
    if (!(mL1 == other.mL1))
      return false;
    if ((mL2 == null) ^ (other.mL2 == null))
      return false;
    if (mL2 != null) {
      if (!(mL2.equals(other.mL2)))
        return false;
    }
    if (!(Arrays.equals(mL3, other.mL3)))
      return false;
    if ((mL4 == null) ^ (other.mL4 == null))
      return false;
    if (mL4 != null) {
      if (!(Arrays.equals(mL4, other.mL4)))
        return false;
    }
    if (!(mF1 == other.mF1))
      return false;
    if ((mF2 == null) ^ (other.mF2 == null))
      return false;
    if (mF2 != null) {
      if (!(mF2.equals(other.mF2)))
        return false;
    }
    if (!(Arrays.equals(mF3, other.mF3)))
      return false;
    if ((mF4 == null) ^ (other.mF4 == null))
      return false;
    if (mF4 != null) {
      if (!(Arrays.equals(mF4, other.mF4)))
        return false;
    }
    if (!(mD1 == other.mD1))
      return false;
    if ((mD2 == null) ^ (other.mD2 == null))
      return false;
    if (mD2 != null) {
      if (!(mD2.equals(other.mD2)))
        return false;
    }
    if (!(Arrays.equals(mD3, other.mD3)))
      return false;
    if ((mD4 == null) ^ (other.mD4 == null))
      return false;
    if (mD4 != null) {
      if (!(Arrays.equals(mD4, other.mD4)))
        return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mB1;
      if (mB2 != null) {
        r = r * 37 + mB2.byteValue();
      }
      r = r * 37 + Arrays.hashCode(mB3);
      if (mB4 != null) {
        r = r * 37 + Arrays.hashCode(mB4);
      }
      r = r * 37 + mS1;
      if (mS2 != null) {
        r = r * 37 + mS2;
      }
      r = r * 37 + Arrays.hashCode(mS3);
      if (mS4 != null) {
        r = r * 37 + Arrays.hashCode(mS4);
      }
      r = r * 37 + mI1;
      if (mI2 != null) {
        r = r * 37 + mI2;
      }
      r = r * 37 + Arrays.hashCode(mI3);
      if (mI4 != null) {
        r = r * 37 + Arrays.hashCode(mI4);
      }
      r = r * 37 + (int)mL1;
      if (mL2 != null) {
        r = r * 37 + mL2.intValue();
      }
      r = r * 37 + Arrays.hashCode(mL3);
      if (mL4 != null) {
        r = r * 37 + Arrays.hashCode(mL4);
      }
      r = r * 37 + (int)mF1;
      if (mF2 != null) {
        r = r * 37 + mF2.hashCode();
      }
      r = r * 37 + Arrays.hashCode(mF3);
      if (mF4 != null) {
        r = r * 37 + Arrays.hashCode(mF4);
      }
      r = r * 37 + (int) mD1;
      if (mD2 != null) {
        r = r * 37 + mD2.intValue();
      }
      r = r * 37 + Arrays.hashCode(mD3);
      if (mD4 != null) {
        r = r * 37 + Arrays.hashCode(mD4);
      }
      m__hashcode = r;
    }
    return r;
  }

  protected byte mB1;
  protected Byte mB2;
  protected byte[] mB3;
  protected byte[] mB4;
  protected short mS1;
  protected Short mS2;
  protected short[] mS3;
  protected short[] mS4;
  protected int mI1;
  protected Integer mI2;
  protected int[] mI3;
  protected int[] mI4;
  protected long mL1;
  protected Long mL2;
  protected long[] mL3;
  protected long[] mL4;
  protected float mF1;
  protected Float mF2;
  protected float[] mF3;
  protected float[] mF4;
  protected double mD1;
  protected Double mD2;
  protected double[] mD3;
  protected double[] mD4;
  protected int m__hashcode;

  public static final class Builder extends SampleDataType {

    private Builder(SampleDataType m) {
      mB1 = m.mB1;
      mB2 = m.mB2;
      mB3 = m.mB3;
      mB4 = m.mB4;
      mS1 = m.mS1;
      mS2 = m.mS2;
      mS3 = m.mS3;
      mS4 = m.mS4;
      mI1 = m.mI1;
      mI2 = m.mI2;
      mI3 = m.mI3;
      mI4 = m.mI4;
      mL1 = m.mL1;
      mL2 = m.mL2;
      mL3 = m.mL3;
      mL4 = m.mL4;
      mF1 = m.mF1;
      mF2 = m.mF2;
      mF3 = m.mF3;
      mF4 = m.mF4;
      mD1 = m.mD1;
      mD2 = m.mD2;
      mD3 = m.mD3;
      mD4 = m.mD4;
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
      r.mB2 = mB2;
      r.mB3 = mB3;
      r.mB4 = mB4;
      r.mS1 = mS1;
      r.mS2 = mS2;
      r.mS3 = mS3;
      r.mS4 = mS4;
      r.mI1 = mI1;
      r.mI2 = mI2;
      r.mI3 = mI3;
      r.mI4 = mI4;
      r.mL1 = mL1;
      r.mL2 = mL2;
      r.mL3 = mL3;
      r.mL4 = mL4;
      r.mF1 = mF1;
      r.mF2 = mF2;
      r.mF3 = mF3;
      r.mF4 = mF4;
      r.mD1 = mD1;
      r.mD2 = mD2;
      r.mD3 = mD3;
      r.mD4 = mD4;
      return r;
    }

    public Builder b1(byte x) {
      mB1 = x;
      return this;
    }

    public Builder b2(Byte x) {
      mB2 = x;
      return this;
    }

    public Builder b3(byte[] x) {
      mB3 = (x == null) ? DataUtil.EMPTY_BYTE_ARRAY : x;
      return this;
    }

    public Builder b4(byte[] x) {
      mB4 = (x == null) ? null : x;
      return this;
    }

    public Builder s1(short x) {
      mS1 = x;
      return this;
    }

    public Builder s2(Short x) {
      mS2 = x;
      return this;
    }

    public Builder s3(short[] x) {
      mS3 = (x == null) ? DataUtil.EMPTY_SHORT_ARRAY : x;
      return this;
    }

    public Builder s4(short[] x) {
      mS4 = (x == null) ? null : x;
      return this;
    }

    public Builder i1(int x) {
      mI1 = x;
      return this;
    }

    public Builder i2(Integer x) {
      mI2 = x;
      return this;
    }

    public Builder i3(int[] x) {
      mI3 = (x == null) ? DataUtil.EMPTY_INT_ARRAY : x;
      return this;
    }

    public Builder i4(int[] x) {
      mI4 = (x == null) ? null : x;
      return this;
    }

    public Builder l1(long x) {
      mL1 = x;
      return this;
    }

    public Builder l2(Long x) {
      mL2 = x;
      return this;
    }

    public Builder l3(long[] x) {
      mL3 = (x == null) ? DataUtil.EMPTY_LONG_ARRAY : x;
      return this;
    }

    public Builder l4(long[] x) {
      mL4 = (x == null) ? null : x;
      return this;
    }

    public Builder f1(float x) {
      mF1 = x;
      return this;
    }

    public Builder f2(Float x) {
      mF2 = x;
      return this;
    }

    public Builder f3(float[] x) {
      mF3 = (x == null) ? DataUtil.EMPTY_FLOAT_ARRAY : x;
      return this;
    }

    public Builder f4(float[] x) {
      mF4 = x;
      return this;
    }

    public Builder d1(double x) {
      mD1 = x;
      return this;
    }

    public Builder d2(Double x) {
      mD2 = x;
      return this;
    }

    public Builder d3(double[] x) {
      mD3 = (x == null) ? DataUtil.EMPTY_DOUBLE_ARRAY : x;
      return this;
    }

    public Builder d4(double[] x) {
      mD4 = x;
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
  }

}
