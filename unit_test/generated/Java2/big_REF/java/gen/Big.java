package gen;

import abc.xyz.Beaver;
import foo.gen.Garp;
import foo.gen.GarpEnum;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import js.base.Tools;
import js.data.AbstractData;
import js.data.DataUtil;
import js.file.Files;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.Matrix;
import js.json.JSList;
import js.json.JSMap;

public class Big implements AbstractData {

  public int alpha() {
    return mAlpha;
  }

  public int beta() {
    return mBeta;
  }

  public Integer gamma() {
    return mGamma;
  }

  public int[] epsilon() {
    return mEpsilon;
  }

  public byte b() {
    return mB;
  }

  public byte c() {
    return mC;
  }

  public Byte d() {
    return mD;
  }

  public byte[] e() {
    return mE;
  }

  public String sa() {
    return mSa;
  }

  public String sb() {
    return mSb;
  }

  public String sc() {
    return mSc;
  }

  public List<String> sd() {
    return mSd;
  }

  public List<String> se() {
    return mSe;
  }

  public List<String> sf() {
    return mSf;
  }

  public boolean ba() {
    return mBa;
  }

  public boolean bb() {
    return mBb;
  }

  public Boolean bc() {
    return mBc;
  }

  public Beaver bv() {
    return mBv;
  }

  public Beaver bv2() {
    return mBv2;
  }

  public List<Beaver> mult() {
    return mMult;
  }

  public File falpha() {
    return mFalpha;
  }

  public File fbeta() {
    return mFbeta;
  }

  public List<File> fgamma() {
    return mFgamma;
  }

  public List<File> fdelta() {
    return mFdelta;
  }

  public File fepsilon() {
    return mFepsilon;
  }

  public IPoint location() {
    return mLocation;
  }

  public IRect locx() {
    return mLocx;
  }

  public FPoint floc() {
    return mFloc;
  }

  public FRect frect() {
    return mFrect;
  }

  public Matrix mat() {
    return mMat;
  }

  public JSMap jalpha() {
    return mJalpha;
  }

  public JSMap jbeta() {
    return mJbeta;
  }

  public List<JSMap> jgamma() {
    return mJgamma;
  }

  public JSMap jepsilon() {
    return mJepsilon;
  }

  public JSList lalpha() {
    return mLalpha;
  }

  public JSList lbeta() {
    return mLbeta;
  }

  public List<JSList> lgamma() {
    return mLgamma;
  }

  public JSList lepsilon() {
    return mLepsilon;
  }

  public Garp garp() {
    return mGarp;
  }

  public GarpEnum gnum() {
    return mGnum;
  }

  public IPoint za() {
    return mZa;
  }

  public IPoint zb() {
    return mZb;
  }

  public List<IPoint> zc() {
    return mZc;
  }

  public List<IPoint> zd() {
    return mZd;
  }

  public IPoint ze() {
    return mZe;
  }

  public Map<String, File> mpalpha() {
    return mMpalpha;
  }

  public Map<String, File> mpbeta() {
    return mMpbeta;
  }

  public Map<String, Beaver> mpgamma() {
    return mMpgamma;
  }

  public Map<String, String> mpdelta() {
    return mMpdelta;
  }

  public Map<String, Long> nameMap() {
    return mNameMap;
  }

  public Set<Long> agesSet() {
    return mAgesSet;
  }

  public Set<File> setalpha() {
    return mSetalpha;
  }

  public Set<File> setbeta() {
    return mSetbeta;
  }

  public Set<Beaver> setgamma() {
    return mSetgamma;
  }

  public Set<String> setdelta() {
    return mSetdelta;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String ALPHA = "alpha";
  protected static final String BETA = "beta";
  protected static final String GAMMA = "gamma";
  protected static final String EPSILON = "epsilon";
  protected static final String B = "b";
  protected static final String C = "c";
  protected static final String D = "d";
  protected static final String E = "e";
  protected static final String SA = "sa";
  protected static final String SB = "sb";
  protected static final String SC = "sc";
  protected static final String SD = "sd";
  protected static final String SE = "se";
  protected static final String SF = "sf";
  protected static final String BA = "ba";
  protected static final String BB = "bb";
  protected static final String BC = "bc";
  protected static final String BV = "bv";
  protected static final String BV2 = "bv2";
  protected static final String MULT = "mult";
  protected static final String FALPHA = "falpha";
  protected static final String FBETA = "fbeta";
  protected static final String FGAMMA = "fgamma";
  protected static final String FDELTA = "fdelta";
  protected static final String FEPSILON = "fepsilon";
  protected static final String LOCATION = "location";
  protected static final String LOCX = "locx";
  protected static final String FLOC = "floc";
  protected static final String FRECT = "frect";
  protected static final String MAT = "mat";
  protected static final String JALPHA = "jalpha";
  protected static final String JBETA = "jbeta";
  protected static final String JGAMMA = "jgamma";
  protected static final String JEPSILON = "jepsilon";
  protected static final String LALPHA = "lalpha";
  protected static final String LBETA = "lbeta";
  protected static final String LGAMMA = "lgamma";
  protected static final String LEPSILON = "lepsilon";
  protected static final String GARP = "garp";
  protected static final String GNUM = "gnum";
  protected static final String ZA = "za";
  protected static final String ZB = "zb";
  protected static final String ZC = "zc";
  protected static final String ZD = "zd";
  protected static final String ZE = "ze";
  protected static final String MPALPHA = "mpalpha";
  protected static final String MPBETA = "mpbeta";
  protected static final String MPGAMMA = "mpgamma";
  protected static final String MPDELTA = "mpdelta";
  protected static final String NAME_MAP = "name_map";
  protected static final String AGES_SET = "ages_set";
  protected static final String SETALPHA = "setalpha";
  protected static final String SETBETA = "setbeta";
  protected static final String SETGAMMA = "setgamma";
  protected static final String SETDELTA = "setdelta";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(ALPHA, mAlpha);
    m.putUnsafe(BETA, mBeta);
    if (mGamma != null) {
      m.putUnsafe(GAMMA, mGamma);
    }
    if (mEpsilon != null) {
      m.putUnsafe(EPSILON, DataUtil.encodeBase64Maybe(mEpsilon));
    }
    m.putUnsafe(B, mB);
    m.putUnsafe(C, mC);
    if (mD != null) {
      m.putUnsafe(D, mD);
    }
    if (mE != null) {
      m.putUnsafe(E, DataUtil.encodeBase64Maybe(mE));
    }
    m.putUnsafe(SA, mSa);
    m.putUnsafe(SB, mSb);
    if (mSc != null) {
      m.putUnsafe(SC, mSc);
    }
    {
      JSList j = new JSList();
      for (String x : mSd)
        j.add(x);
      m.put(SD, j);
    }
    if (mSe != null) {
      {
        JSList j = new JSList();
        for (String x : mSe)
          j.add(x);
        m.put(SE, j);
      }
    }
    {
      JSList j = new JSList();
      for (String x : mSf)
        j.add(x);
      m.put(SF, j);
    }
    m.putUnsafe(BA, mBa);
    m.putUnsafe(BB, mBb);
    if (mBc != null) {
      m.putUnsafe(BC, mBc);
    }
    m.putUnsafe(BV, mBv.toJson());
    if (mBv2 != null) {
      m.putUnsafe(BV2, mBv2.toJson());
    }
    {
      JSList j = new JSList();
      for (Beaver x : mMult)
        j.add(x.toJson());
      m.put(MULT, j);
    }
    m.putUnsafe(FALPHA, mFalpha.toString());
    if (mFbeta != null) {
      m.putUnsafe(FBETA, mFbeta.toString());
    }
    {
      JSList j = new JSList();
      for (File x : mFgamma)
        j.add(x.toString());
      m.put(FGAMMA, j);
    }
    if (mFdelta != null) {
      {
        JSList j = new JSList();
        for (File x : mFdelta)
          j.add(x.toString());
        m.put(FDELTA, j);
      }
    }
    m.putUnsafe(FEPSILON, mFepsilon.toString());
    m.putUnsafe(LOCATION, mLocation.toJson());
    m.putUnsafe(LOCX, mLocx.toJson());
    m.putUnsafe(FLOC, mFloc.toJson());
    m.putUnsafe(FRECT, mFrect.toJson());
    m.putUnsafe(MAT, mMat.toJson());
    m.putUnsafe(JALPHA, mJalpha);
    if (mJbeta != null) {
      m.putUnsafe(JBETA, mJbeta);
    }
    {
      JSList j = new JSList();
      for (JSMap x : mJgamma)
        j.add(x);
      m.put(JGAMMA, j);
    }
    m.putUnsafe(JEPSILON, mJepsilon);
    m.putUnsafe(LALPHA, mLalpha);
    if (mLbeta != null) {
      m.putUnsafe(LBETA, mLbeta);
    }
    {
      JSList j = new JSList();
      for (JSList x : mLgamma)
        j.add(x);
      m.put(LGAMMA, j);
    }
    m.putUnsafe(LEPSILON, mLepsilon);
    m.putUnsafe(GARP, mGarp.toJson());
    m.putUnsafe(GNUM, mGnum.toString().toLowerCase());
    m.putUnsafe(ZA, mZa.toJson());
    if (mZb != null) {
      m.putUnsafe(ZB, mZb.toJson());
    }
    {
      JSList j = new JSList();
      for (IPoint x : mZc)
        j.add(x.toJson());
      m.put(ZC, j);
    }
    if (mZd != null) {
      {
        JSList j = new JSList();
        for (IPoint x : mZd)
          j.add(x.toJson());
        m.put(ZD, j);
      }
    }
    m.putUnsafe(ZE, mZe.toJson());
    {
      JSMap j = new JSMap();
      for (Map.Entry<String, File> e : mMpalpha.entrySet())
        j.put(e.getKey(), e.getValue().toString());
      m.put(MPALPHA, j);
    }
    if (mMpbeta != null) {
      {
        JSMap j = new JSMap();
        for (Map.Entry<String, File> e : mMpbeta.entrySet())
          j.put(e.getKey(), e.getValue().toString());
        m.put(MPBETA, j);
      }
    }
    {
      JSMap j = new JSMap();
      for (Map.Entry<String, Beaver> e : mMpgamma.entrySet())
        j.put(e.getKey(), e.getValue().toJson());
      m.put(MPGAMMA, j);
    }
    {
      JSMap j = new JSMap();
      for (Map.Entry<String, String> e : mMpdelta.entrySet())
        j.put(e.getKey(), e.getValue());
      m.put(MPDELTA, j);
    }
    {
      JSMap j = new JSMap();
      for (Map.Entry<String, Long> e : mNameMap.entrySet())
        j.put(e.getKey(), e.getValue());
      m.put(NAME_MAP, j);
    }
    {
      JSList j = new JSList();
      for (Long e : mAgesSet)
        j.add(e);
      m.put(AGES_SET, j);
    }
    {
      JSList j = new JSList();
      for (File e : mSetalpha)
        j.add(e.toString());
      m.put(SETALPHA, j);
    }
    if (mSetbeta != null) {
      {
        JSList j = new JSList();
        for (File e : mSetbeta)
          j.add(e.toString());
        m.put(SETBETA, j);
      }
    }
    {
      JSList j = new JSList();
      for (Beaver e : mSetgamma)
        j.add(e.toJson());
      m.put(SETGAMMA, j);
    }
    {
      JSList j = new JSList();
      for (String e : mSetdelta)
        j.add(e);
      m.put(SETDELTA, j);
    }
    return m;
  }

  @Override
  public Big build() {
    return this;
  }

  @Override
  public Big parse(Object obj) {
    return new Big((JSMap) obj);
  }

  private Big(JSMap m) {
    mAlpha = m.opt(ALPHA, 0);
    mBeta = m.opt(BETA, 2147483647);
    mGamma = m.optInt(GAMMA);
    {
      Object x = m.optUnsafe(EPSILON);
      if (x != null) {
        mEpsilon = DataUtil.parseIntsFromArrayOrBase64(x);
      }
    }
    mB = m.opt(B, (byte) 0);
    mC = m.opt(C, 127);
    mD = m.optByte(D);
    {
      Object x = m.optUnsafe(E);
      if (x != null) {
        mE = DataUtil.parseBytesFromArrayOrBase64(x);
      }
    }
    mSa = m.opt(SA, "");
    mSb = m.opt(SB, "hello");
    mSc = m.opt(SC, (String) null);
    mSd = DataUtil.parseListOfObjects(m.optJSList(SD), false);
    mSe = DataUtil.parseListOfObjects(m.optJSList(SE), true);
    mSf = DataUtil.parseListOfObjects(m.optJSList(SF), false);
    mBa = m.opt(BA, false);
    mBb = m.opt(BB, true);
    mBc = m.opt(BC, (Boolean) null);
    {
      mBv = Beaver.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(BV);
      if (x != null) {
        mBv = Beaver.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      Object x = (Object) m.optUnsafe(BV2);
      if (x != null) {
        mBv2 = Beaver.DEFAULT_INSTANCE.parse(x);
      }
    }
    mMult = DataUtil.parseListOfObjects(Beaver.DEFAULT_INSTANCE, m.optJSList(MULT), false);
    {
      mFalpha = Files.DEFAULT;
      String x = m.opt(FALPHA, (String) null);
      if (x != null) {
        mFalpha = new File(x);
      }
    }
    {
      String x = m.opt(FBETA, (String) null);
      if (x != null) {
        mFbeta = new File(x);
      }
    }
    {
      List<File> result = new ArrayList<>();
      JSList j = m.optJSList(FGAMMA);
      if (j != null) {
        result = new ArrayList<>(j.size());
        for (Object z : j.wrappedList()) {
          File y = Files.DEFAULT;
          if (z != null) {
            String x = (String) z;
            y = new File(x);
          }
          result.add(y);
        }
      }
      mFgamma = DataUtil.immutableCopyOf(result);
    }
    {
      List<File> result = null;
      JSList j = m.optJSList(FDELTA);
      if (j != null) {
        result = new ArrayList<>(j.size());
        for (Object z : j.wrappedList()) {
          File y = Files.DEFAULT;
          if (z != null) {
            String x = (String) z;
            y = new File(x);
          }
          result.add(y);
        }
      }
      mFdelta = DataUtil.immutableCopyOf(result);
    }
    {
      mFepsilon = DEF_FEPSILON;
      String x = m.opt(FEPSILON, (String) null);
      if (x != null) {
        mFepsilon = new File(x);
      }
    }
    {
      mLocation = IPoint.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(LOCATION);
      if (x != null) {
        mLocation = IPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mLocx = IRect.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(LOCX);
      if (x != null) {
        mLocx = IRect.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mFloc = FPoint.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(FLOC);
      if (x != null) {
        mFloc = FPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mFrect = FRect.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(FRECT);
      if (x != null) {
        mFrect = FRect.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mMat = Matrix.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(MAT);
      if (x != null) {
        mMat = Matrix.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mJalpha = JSMap.DEFAULT_INSTANCE;
      JSMap x = m.optJSMap(JALPHA);
      if (x != null) {
        mJalpha = x.lock();
      }
    }
    {
      JSMap x = m.optJSMap(JBETA);
      if (x != null) {
        mJbeta = x.lock();
      }
    }
    mJgamma = DataUtil.parseListOfObjects(m.optJSList(JGAMMA), false);
    {
      mJepsilon = DEF_JEPSILON;
      JSMap x = m.optJSMap(JEPSILON);
      if (x != null) {
        mJepsilon = x.lock();
      }
    }
    {
      mLalpha = JSList.DEFAULT_INSTANCE;
      JSList x = m.optJSList(LALPHA);
      if (x != null) {
        mLalpha = x.lock();
      }
    }
    {
      JSList x = m.optJSList(LBETA);
      if (x != null) {
        mLbeta = x.lock();
      }
    }
    mLgamma = DataUtil.parseListOfObjects(m.optJSList(LGAMMA), false);
    {
      mLepsilon = DEF_LEPSILON;
      JSList x = m.optJSList(LEPSILON);
      if (x != null) {
        mLepsilon = x.lock();
      }
    }
    {
      mGarp = Garp.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(GARP);
      if (x != null) {
        mGarp = Garp.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      String x = m.opt(GNUM, "");
      mGnum = x.isEmpty() ? GarpEnum.DEFAULT_INSTANCE : GarpEnum.valueOf(x.toUpperCase());
    }
    {
      mZa = IPoint.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(ZA);
      if (x != null) {
        mZa = IPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      Object x = (Object) m.optUnsafe(ZB);
      if (x != null) {
        mZb = IPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    mZc = DataUtil.parseListOfObjects(IPoint.DEFAULT_INSTANCE, m.optJSList(ZC), false);
    mZd = DataUtil.parseListOfObjects(IPoint.DEFAULT_INSTANCE, m.optJSList(ZD), true);
    {
      mZe = DEF_ZE;
      Object x = (Object) m.optUnsafe(ZE);
      if (x != null) {
        mZe = IPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mMpalpha = DataUtil.emptyMap();
      {
        JSMap m2 = m.optJSMap("mpalpha");
        if (m2 != null && !m2.isEmpty()) {
          Map<String, File> mp = new ConcurrentHashMap<>();
          for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())
            mp.put(e.getKey(), new File((String) e.getValue()));
          mMpalpha = mp;
        }
      }
    }
    {
      {
        JSMap m2 = m.optJSMap("mpbeta");
        if (m2 != null && !m2.isEmpty()) {
          Map<String, File> mp = new ConcurrentHashMap<>();
          for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())
            mp.put(e.getKey(), new File((String) e.getValue()));
          mMpbeta = mp;
        }
      }
    }
    {
      mMpgamma = DataUtil.emptyMap();
      {
        JSMap m2 = m.optJSMap("mpgamma");
        if (m2 != null && !m2.isEmpty()) {
          Map<String, Beaver> mp = new ConcurrentHashMap<>();
          for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())
            mp.put(e.getKey(), Beaver.DEFAULT_INSTANCE.parse((JSMap) e.getValue()));
          mMpgamma = mp;
        }
      }
    }
    {
      mMpdelta = DataUtil.emptyMap();
      {
        JSMap m2 = m.optJSMap("mpdelta");
        if (m2 != null && !m2.isEmpty()) {
          Map<String, String> mp = new ConcurrentHashMap<>();
          for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())
            mp.put(e.getKey(), (String) e.getValue());
          mMpdelta = mp;
        }
      }
    }
    {
      mNameMap = DataUtil.emptyMap();
      {
        JSMap m2 = m.optJSMap("name_map");
        if (m2 != null && !m2.isEmpty()) {
          Map<String, Long> mp = new ConcurrentHashMap<>();
          for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())
            mp.put(e.getKey(), (Long) e.getValue());
          mNameMap = mp;
        }
      }
    }
    {
      mAgesSet = DataUtil.emptySet();
      {
        JSList m2 = m.optJSList("ages_set");
        if (m2 != null && !m2.isEmpty()) {
          Set<Long> mp = new HashSet<>();
          for (Object e : m2.wrappedList())
            mp.add((Long) e);
          mAgesSet = mp;
        }
      }
    }
    {
      mSetalpha = DataUtil.emptySet();
      {
        JSList m2 = m.optJSList("setalpha");
        if (m2 != null && !m2.isEmpty()) {
          Set<File> mp = new HashSet<>();
          for (Object e : m2.wrappedList())
            mp.add(new File((String) e));
          mSetalpha = mp;
        }
      }
    }
    {
      {
        JSList m2 = m.optJSList("setbeta");
        if (m2 != null && !m2.isEmpty()) {
          Set<File> mp = new HashSet<>();
          for (Object e : m2.wrappedList())
            mp.add(new File((String) e));
          mSetbeta = mp;
        }
      }
    }
    {
      mSetgamma = DataUtil.emptySet();
      {
        JSList m2 = m.optJSList("setgamma");
        if (m2 != null && !m2.isEmpty()) {
          Set<Beaver> mp = new HashSet<>();
          for (Object e : m2.wrappedList())
            mp.add(Beaver.DEFAULT_INSTANCE.parse((JSMap) e));
          mSetgamma = mp;
        }
      }
    }
    {
      mSetdelta = DataUtil.emptySet();
      {
        JSList m2 = m.optJSList("setdelta");
        if (m2 != null && !m2.isEmpty()) {
          Set<String> mp = new HashSet<>();
          for (Object e : m2.wrappedList())
            mp.add((String) e);
          mSetdelta = mp;
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
    if (object == null || !(object instanceof Big))
      return false;
    Big other = (Big) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mAlpha == other.mAlpha))
      return false;
    if (!(mBeta == other.mBeta))
      return false;
    if ((mGamma == null) ^ (other.mGamma == null))
      return false;
    if (mGamma != null) {
      if (!(mGamma.equals(other.mGamma)))
        return false;
    }
    if ((mEpsilon == null) ^ (other.mEpsilon == null))
      return false;
    if (mEpsilon != null) {
      if (!(Arrays.equals(mEpsilon, other.mEpsilon)))
        return false;
    }
    if (!(mB == other.mB))
      return false;
    if (!(mC == other.mC))
      return false;
    if ((mD == null) ^ (other.mD == null))
      return false;
    if (mD != null) {
      if (!(mD.equals(other.mD)))
        return false;
    }
    if ((mE == null) ^ (other.mE == null))
      return false;
    if (mE != null) {
      if (!(Arrays.equals(mE, other.mE)))
        return false;
    }
    if (!(mSa.equals(other.mSa)))
      return false;
    if (!(mSb.equals(other.mSb)))
      return false;
    if ((mSc == null) ^ (other.mSc == null))
      return false;
    if (mSc != null) {
      if (!(mSc.equals(other.mSc)))
        return false;
    }
    if (!(mSd.equals(other.mSd)))
      return false;
    if ((mSe == null) ^ (other.mSe == null))
      return false;
    if (mSe != null) {
      if (!(mSe.equals(other.mSe)))
        return false;
    }
    if (!(mSf.equals(other.mSf)))
      return false;
    if (!(mBa == other.mBa))
      return false;
    if (!(mBb == other.mBb))
      return false;
    if ((mBc == null) ^ (other.mBc == null))
      return false;
    if (mBc != null) {
      if (!(mBc.equals(other.mBc)))
        return false;
    }
    if (!(mBv.equals(other.mBv)))
      return false;
    if ((mBv2 == null) ^ (other.mBv2 == null))
      return false;
    if (mBv2 != null) {
      if (!(mBv2.equals(other.mBv2)))
        return false;
    }
    if (!(mMult.equals(other.mMult)))
      return false;
    if (!(mFalpha.equals(other.mFalpha)))
      return false;
    if ((mFbeta == null) ^ (other.mFbeta == null))
      return false;
    if (mFbeta != null) {
      if (!(mFbeta.equals(other.mFbeta)))
        return false;
    }
    if (!(mFgamma.equals(other.mFgamma)))
      return false;
    if ((mFdelta == null) ^ (other.mFdelta == null))
      return false;
    if (mFdelta != null) {
      if (!(mFdelta.equals(other.mFdelta)))
        return false;
    }
    if (!(mFepsilon.equals(other.mFepsilon)))
      return false;
    if (!(mLocation.equals(other.mLocation)))
      return false;
    if (!(mLocx.equals(other.mLocx)))
      return false;
    if (!(mFloc.equals(other.mFloc)))
      return false;
    if (!(mFrect.equals(other.mFrect)))
      return false;
    if (!(mMat.equals(other.mMat)))
      return false;
    if (!(mJalpha.equals(other.mJalpha)))
      return false;
    if ((mJbeta == null) ^ (other.mJbeta == null))
      return false;
    if (mJbeta != null) {
      if (!(mJbeta.equals(other.mJbeta)))
        return false;
    }
    if (!(mJgamma.equals(other.mJgamma)))
      return false;
    if (!(mJepsilon.equals(other.mJepsilon)))
      return false;
    if (!(mLalpha.equals(other.mLalpha)))
      return false;
    if ((mLbeta == null) ^ (other.mLbeta == null))
      return false;
    if (mLbeta != null) {
      if (!(mLbeta.equals(other.mLbeta)))
        return false;
    }
    if (!(mLgamma.equals(other.mLgamma)))
      return false;
    if (!(mLepsilon.equals(other.mLepsilon)))
      return false;
    if (!(mGarp.equals(other.mGarp)))
      return false;
    if (!(mGnum.equals(other.mGnum)))
      return false;
    if (!(mZa.equals(other.mZa)))
      return false;
    if ((mZb == null) ^ (other.mZb == null))
      return false;
    if (mZb != null) {
      if (!(mZb.equals(other.mZb)))
        return false;
    }
    if (!(mZc.equals(other.mZc)))
      return false;
    if ((mZd == null) ^ (other.mZd == null))
      return false;
    if (mZd != null) {
      if (!(mZd.equals(other.mZd)))
        return false;
    }
    if (!(mZe.equals(other.mZe)))
      return false;
    if (!(mMpalpha.equals(other.mMpalpha)))
      return false;
    if ((mMpbeta == null) ^ (other.mMpbeta == null))
      return false;
    if (mMpbeta != null) {
      if (!(mMpbeta.equals(other.mMpbeta)))
        return false;
    }
    if (!(mMpgamma.equals(other.mMpgamma)))
      return false;
    if (!(mMpdelta.equals(other.mMpdelta)))
      return false;
    if (!(mNameMap.equals(other.mNameMap)))
      return false;
    if (!(mAgesSet.equals(other.mAgesSet)))
      return false;
    if (!(mSetalpha.equals(other.mSetalpha)))
      return false;
    if ((mSetbeta == null) ^ (other.mSetbeta == null))
      return false;
    if (mSetbeta != null) {
      if (!(mSetbeta.equals(other.mSetbeta)))
        return false;
    }
    if (!(mSetgamma.equals(other.mSetgamma)))
      return false;
    if (!(mSetdelta.equals(other.mSetdelta)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mAlpha;
      r = r * 37 + mBeta;
      if (mGamma != null) {
        r = r * 37 + mGamma;
      }
      if (mEpsilon != null) {
        r = r * 37 + Arrays.hashCode(mEpsilon);
      }
      r = r * 37 + mB;
      r = r * 37 + mC;
      if (mD != null) {
        r = r * 37 + mD.byteValue();
      }
      if (mE != null) {
        r = r * 37 + Arrays.hashCode(mE);
      }
      r = r * 37 + mSa.hashCode();
      r = r * 37 + mSb.hashCode();
      if (mSc != null) {
        r = r * 37 + mSc.hashCode();
      }
      for (String x : mSd)
        if (x != null)
          r = r * 37 + x.hashCode();
      if (mSe != null) {
        for (String x : mSe)
          if (x != null)
            r = r * 37 + x.hashCode();
      }
      for (String x : mSf)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + (mBa ? 1 : 0);
      r = r * 37 + (mBb ? 1 : 0);
      if (mBc != null) {
        r = r * 37 + (mBc ? 1 : 0);
      }
      r = r * 37 + mBv.hashCode();
      if (mBv2 != null) {
        r = r * 37 + mBv2.hashCode();
      }
      for (Beaver x : mMult)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + mFalpha.hashCode();
      if (mFbeta != null) {
        r = r * 37 + mFbeta.hashCode();
      }
      for (File x : mFgamma)
        if (x != null)
          r = r * 37 + x.hashCode();
      if (mFdelta != null) {
        for (File x : mFdelta)
          if (x != null)
            r = r * 37 + x.hashCode();
      }
      r = r * 37 + mFepsilon.hashCode();
      r = r * 37 + mLocation.hashCode();
      r = r * 37 + mLocx.hashCode();
      r = r * 37 + mFloc.hashCode();
      r = r * 37 + mFrect.hashCode();
      r = r * 37 + mMat.hashCode();
      r = r * 37 + mJalpha.hashCode();
      if (mJbeta != null) {
        r = r * 37 + mJbeta.hashCode();
      }
      for (JSMap x : mJgamma)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + mJepsilon.hashCode();
      r = r * 37 + mLalpha.hashCode();
      if (mLbeta != null) {
        r = r * 37 + mLbeta.hashCode();
      }
      for (JSList x : mLgamma)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + mLepsilon.hashCode();
      r = r * 37 + mGarp.hashCode();
      r = r * 37 + mGnum.ordinal();
      r = r * 37 + mZa.hashCode();
      if (mZb != null) {
        r = r * 37 + mZb.hashCode();
      }
      for (IPoint x : mZc)
        if (x != null)
          r = r * 37 + x.hashCode();
      if (mZd != null) {
        for (IPoint x : mZd)
          if (x != null)
            r = r * 37 + x.hashCode();
      }
      r = r * 37 + mZe.hashCode();
      r = r * 37 + mMpalpha.hashCode();
      if (mMpbeta != null) {
        r = r * 37 + mMpbeta.hashCode();
      }
      r = r * 37 + mMpgamma.hashCode();
      r = r * 37 + mMpdelta.hashCode();
      r = r * 37 + mNameMap.hashCode();
      r = r * 37 + mAgesSet.hashCode();
      r = r * 37 + mSetalpha.hashCode();
      if (mSetbeta != null) {
        r = r * 37 + mSetbeta.hashCode();
      }
      r = r * 37 + mSetgamma.hashCode();
      r = r * 37 + mSetdelta.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected int mAlpha;
  protected int mBeta;
  protected Integer mGamma;
  protected int[] mEpsilon;
  protected byte mB;
  protected byte mC;
  protected Byte mD;
  protected byte[] mE;
  protected String mSa;
  protected String mSb;
  protected String mSc;
  protected List<String> mSd;
  protected List<String> mSe;
  protected List<String> mSf;
  protected boolean mBa;
  protected boolean mBb;
  protected Boolean mBc;
  protected Beaver mBv;
  protected Beaver mBv2;
  protected List<Beaver> mMult;
  protected File mFalpha;
  protected File mFbeta;
  protected List<File> mFgamma;
  protected List<File> mFdelta;
  protected File mFepsilon;
  protected IPoint mLocation;
  protected IRect mLocx;
  protected FPoint mFloc;
  protected FRect mFrect;
  protected Matrix mMat;
  protected JSMap mJalpha;
  protected JSMap mJbeta;
  protected List<JSMap> mJgamma;
  protected JSMap mJepsilon;
  protected JSList mLalpha;
  protected JSList mLbeta;
  protected List<JSList> mLgamma;
  protected JSList mLepsilon;
  protected Garp mGarp;
  protected GarpEnum mGnum;
  protected IPoint mZa;
  protected IPoint mZb;
  protected List<IPoint> mZc;
  protected List<IPoint> mZd;
  protected IPoint mZe;
  protected Map<String, File> mMpalpha;
  protected Map<String, File> mMpbeta;
  protected Map<String, Beaver> mMpgamma;
  protected Map<String, String> mMpdelta;
  protected Map<String, Long> mNameMap;
  protected Set<Long> mAgesSet;
  protected Set<File> mSetalpha;
  protected Set<File> mSetbeta;
  protected Set<Beaver> mSetgamma;
  protected Set<String> mSetdelta;
  protected int m__hashcode;

  public static final class Builder extends Big {

    private Builder(Big m) {
      mAlpha = m.mAlpha;
      mBeta = m.mBeta;
      mGamma = m.mGamma;
      mEpsilon = m.mEpsilon;
      mB = m.mB;
      mC = m.mC;
      mD = m.mD;
      mE = m.mE;
      mSa = m.mSa;
      mSb = m.mSb;
      mSc = m.mSc;
      mSd = DataUtil.mutableCopyOf(m.mSd);
      mSe = DataUtil.mutableCopyOf(m.mSe);
      mSf = DataUtil.mutableCopyOf(m.mSf);
      mBa = m.mBa;
      mBb = m.mBb;
      mBc = m.mBc;
      mBv = m.mBv;
      mBv2 = m.mBv2;
      mMult = DataUtil.mutableCopyOf(m.mMult);
      mFalpha = m.mFalpha;
      mFbeta = m.mFbeta;
      mFgamma = DataUtil.mutableCopyOf(m.mFgamma);
      mFdelta = DataUtil.mutableCopyOf(m.mFdelta);
      mFepsilon = m.mFepsilon;
      mLocation = m.mLocation;
      mLocx = m.mLocx;
      mFloc = m.mFloc;
      mFrect = m.mFrect;
      mMat = m.mMat;
      mJalpha = m.mJalpha;
      mJbeta = m.mJbeta;
      mJgamma = DataUtil.mutableCopyOf(m.mJgamma);
      mJepsilon = m.mJepsilon;
      mLalpha = m.mLalpha;
      mLbeta = m.mLbeta;
      mLgamma = DataUtil.mutableCopyOf(m.mLgamma);
      mLepsilon = m.mLepsilon;
      mGarp = m.mGarp;
      mGnum = m.mGnum;
      mZa = m.mZa;
      mZb = m.mZb;
      mZc = DataUtil.mutableCopyOf(m.mZc);
      mZd = DataUtil.mutableCopyOf(m.mZd);
      mZe = m.mZe;
      mMpalpha = DataUtil.mutableCopyOf(m.mMpalpha);
      mMpbeta = DataUtil.mutableCopyOf(m.mMpbeta);
      mMpgamma = DataUtil.mutableCopyOf(m.mMpgamma);
      mMpdelta = DataUtil.mutableCopyOf(m.mMpdelta);
      mNameMap = DataUtil.mutableCopyOf(m.mNameMap);
      mAgesSet = DataUtil.mutableCopyOf(m.mAgesSet);
      mSetalpha = DataUtil.mutableCopyOf(m.mSetalpha);
      mSetbeta = DataUtil.mutableCopyOf(m.mSetbeta);
      mSetgamma = DataUtil.mutableCopyOf(m.mSetgamma);
      mSetdelta = DataUtil.mutableCopyOf(m.mSetdelta);
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
    public Big build() {
      Big r = new Big();
      r.mAlpha = mAlpha;
      r.mBeta = mBeta;
      r.mGamma = mGamma;
      r.mEpsilon = mEpsilon;
      r.mB = mB;
      r.mC = mC;
      r.mD = mD;
      r.mE = mE;
      r.mSa = mSa;
      r.mSb = mSb;
      r.mSc = mSc;
      r.mSd = DataUtil.immutableCopyOf(mSd);
      r.mSe = DataUtil.immutableCopyOf(mSe);
      r.mSf = DataUtil.immutableCopyOf(mSf);
      r.mBa = mBa;
      r.mBb = mBb;
      r.mBc = mBc;
      r.mBv = mBv;
      r.mBv2 = mBv2;
      r.mMult = DataUtil.immutableCopyOf(mMult);
      r.mFalpha = mFalpha;
      r.mFbeta = mFbeta;
      r.mFgamma = DataUtil.immutableCopyOf(mFgamma);
      r.mFdelta = DataUtil.immutableCopyOf(mFdelta);
      r.mFepsilon = mFepsilon;
      r.mLocation = mLocation;
      r.mLocx = mLocx;
      r.mFloc = mFloc;
      r.mFrect = mFrect;
      r.mMat = mMat;
      r.mJalpha = mJalpha;
      r.mJbeta = mJbeta;
      r.mJgamma = DataUtil.immutableCopyOf(mJgamma);
      r.mJepsilon = mJepsilon;
      r.mLalpha = mLalpha;
      r.mLbeta = mLbeta;
      r.mLgamma = DataUtil.immutableCopyOf(mLgamma);
      r.mLepsilon = mLepsilon;
      r.mGarp = mGarp;
      r.mGnum = mGnum;
      r.mZa = mZa;
      r.mZb = mZb;
      r.mZc = DataUtil.immutableCopyOf(mZc);
      r.mZd = DataUtil.immutableCopyOf(mZd);
      r.mZe = mZe;
      r.mMpalpha = DataUtil.mutableCopyOf(mMpalpha);
      r.mMpbeta = DataUtil.mutableCopyOf(mMpbeta);
      r.mMpgamma = DataUtil.mutableCopyOf(mMpgamma);
      r.mMpdelta = DataUtil.mutableCopyOf(mMpdelta);
      r.mNameMap = DataUtil.mutableCopyOf(mNameMap);
      r.mAgesSet = DataUtil.mutableCopyOf(mAgesSet);
      r.mSetalpha = DataUtil.mutableCopyOf(mSetalpha);
      r.mSetbeta = DataUtil.mutableCopyOf(mSetbeta);
      r.mSetgamma = DataUtil.mutableCopyOf(mSetgamma);
      r.mSetdelta = DataUtil.mutableCopyOf(mSetdelta);
      return r;
    }

    public Builder alpha(int x) {
      mAlpha = x;
      return this;
    }

    public Builder beta(int x) {
      mBeta = x;
      return this;
    }

    public Builder gamma(Integer x) {
      mGamma = x;
      return this;
    }

    public Builder epsilon(int[] x) {
      mEpsilon = (x == null) ? null : x;
      return this;
    }

    public Builder b(byte x) {
      mB = x;
      return this;
    }

    public Builder c(byte x) {
      mC = x;
      return this;
    }

    public Builder d(Byte x) {
      mD = x;
      return this;
    }

    public Builder e(byte[] x) {
      mE = (x == null) ? null : x;
      return this;
    }

    public Builder sa(String x) {
      mSa = (x == null) ? "" : x;
      return this;
    }

    public Builder sb(String x) {
      mSb = (x == null) ? "hello" : x;
      return this;
    }

    public Builder sc(String x) {
      mSc = x;
      return this;
    }

    public Builder sd(List<String> x) {
      mSd = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder se(List<String> x) {
      mSe = DataUtil.mutableCopyOf(x);
      return this;
    }

    public Builder sf(List<String> x) {
      mSf = DataUtil.mutableCopyOf((x == null) ? DEF_SF : x);
      return this;
    }

    public Builder ba(boolean x) {
      mBa = x;
      return this;
    }

    public Builder bb(boolean x) {
      mBb = x;
      return this;
    }

    public Builder bc(Boolean x) {
      mBc = x;
      return this;
    }

    public Builder bv(Beaver x) {
      mBv = (x == null) ? Beaver.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder bv2(Beaver x) {
      mBv2 = (x == null) ? null : x.build();
      return this;
    }

    public Builder mult(List<Beaver> x) {
      mMult = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder falpha(File x) {
      mFalpha = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder fbeta(File x) {
      mFbeta = x;
      return this;
    }

    public Builder fgamma(List<File> x) {
      mFgamma = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder fdelta(List<File> x) {
      mFdelta = DataUtil.mutableCopyOf(x);
      return this;
    }

    public Builder fepsilon(File x) {
      mFepsilon = (x == null) ? DEF_FEPSILON : x;
      return this;
    }

    public Builder location(IPoint x) {
      mLocation = (x == null) ? IPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder locx(IRect x) {
      mLocx = (x == null) ? IRect.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder floc(FPoint x) {
      mFloc = (x == null) ? FPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder frect(FRect x) {
      mFrect = (x == null) ? FRect.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder mat(Matrix x) {
      mMat = (x == null) ? Matrix.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder jalpha(JSMap x) {
      mJalpha = (x == null) ? JSMap.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder jbeta(JSMap x) {
      mJbeta = x;
      return this;
    }

    public Builder jgamma(List<JSMap> x) {
      mJgamma = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder jepsilon(JSMap x) {
      mJepsilon = (x == null) ? DEF_JEPSILON : x;
      return this;
    }

    public Builder lalpha(JSList x) {
      mLalpha = (x == null) ? JSList.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder lbeta(JSList x) {
      mLbeta = x;
      return this;
    }

    public Builder lgamma(List<JSList> x) {
      mLgamma = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder lepsilon(JSList x) {
      mLepsilon = (x == null) ? DEF_LEPSILON : x;
      return this;
    }

    public Builder garp(Garp x) {
      mGarp = (x == null) ? Garp.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder gnum(GarpEnum x) {
      mGnum = (x == null) ? GarpEnum.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder za(IPoint x) {
      mZa = (x == null) ? IPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder zb(IPoint x) {
      mZb = (x == null) ? null : x.build();
      return this;
    }

    public Builder zc(List<IPoint> x) {
      mZc = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder zd(List<IPoint> x) {
      mZd = DataUtil.mutableCopyOf(x);
      return this;
    }

    public Builder ze(IPoint x) {
      mZe = (x == null) ? DEF_ZE : x.build();
      return this;
    }

    public Builder mpalpha(Map<String, File> x) {
      mMpalpha = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyMap() : x);
      return this;
    }

    public Builder mpbeta(Map<String, File> x) {
      mMpbeta = DataUtil.mutableCopyOf(x);
      return this;
    }

    public Builder mpgamma(Map<String, Beaver> x) {
      mMpgamma = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyMap() : x);
      return this;
    }

    public Builder mpdelta(Map<String, String> x) {
      mMpdelta = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyMap() : x);
      return this;
    }

    public Builder nameMap(Map<String, Long> x) {
      mNameMap = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyMap() : x);
      return this;
    }

    public Builder agesSet(Set<Long> x) {
      mAgesSet = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptySet() : x);
      return this;
    }

    public Builder setalpha(Set<File> x) {
      mSetalpha = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptySet() : x);
      return this;
    }

    public Builder setbeta(Set<File> x) {
      mSetbeta = DataUtil.mutableCopyOf(x);
      return this;
    }

    public Builder setgamma(Set<Beaver> x) {
      mSetgamma = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptySet() : x);
      return this;
    }

    public Builder setdelta(Set<String> x) {
      mSetdelta = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptySet() : x);
      return this;
    }

  }

  private static final List<String> DEF_SF = Tools.arrayList("abc","123");
  private static final File DEF_FEPSILON = new File("abc/xyz.txt");
  private static final JSMap DEF_JEPSILON = new JSMap("{\"a\":15,\"b\":\"hello\"}");
  private static final JSList DEF_LEPSILON = new JSList("[\"a\",15,\"b\",\"hello\"]");
  private static final IPoint DEF_ZE  = new IPoint(32, 64);

  public static final Big DEFAULT_INSTANCE = new Big();

  private Big() {
    mBeta = 2147483647;
    mC = 127;
    mSa = "";
    mSb = "hello";
    mSd = DataUtil.emptyList();
    mSf = DEF_SF;
    mBb = true;
    mBv = Beaver.DEFAULT_INSTANCE;
    mMult = DataUtil.emptyList();
    mFalpha = Files.DEFAULT;
    mFgamma = DataUtil.emptyList();
    mFepsilon = DEF_FEPSILON;
    mLocation = IPoint.DEFAULT_INSTANCE;
    mLocx = IRect.DEFAULT_INSTANCE;
    mFloc = FPoint.DEFAULT_INSTANCE;
    mFrect = FRect.DEFAULT_INSTANCE;
    mMat = Matrix.DEFAULT_INSTANCE;
    mJalpha = JSMap.DEFAULT_INSTANCE;
    mJgamma = DataUtil.emptyList();
    mJepsilon = DEF_JEPSILON;
    mLalpha = JSList.DEFAULT_INSTANCE;
    mLgamma = DataUtil.emptyList();
    mLepsilon = DEF_LEPSILON;
    mGarp = Garp.DEFAULT_INSTANCE;
    mGnum = GarpEnum.DEFAULT_INSTANCE;
    mZa = IPoint.DEFAULT_INSTANCE;
    mZc = DataUtil.emptyList();
    mZe = DEF_ZE;
    mMpalpha = DataUtil.emptyMap();
    mMpgamma = DataUtil.emptyMap();
    mMpdelta = DataUtil.emptyMap();
    mNameMap = DataUtil.emptyMap();
    mAgesSet = DataUtil.emptySet();
    mSetalpha = DataUtil.emptySet();
    mSetgamma = DataUtil.emptySet();
    mSetdelta = DataUtil.emptySet();
  }

}
