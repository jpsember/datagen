package datagen.datatype;

import static js.base.Tools.*;

import datagen.DataType;
import datagen.FieldDef;
import datagen.RustDataType;
import datagen.SourceBuilder;
import js.json.JSMap;

public class RustFloatDataType  extends RustDataType {

  public static final DataType FLOAT_TYPE = new RustFloatDataType(32).with("f32");
  public static final DataType DOUBLE_TYPE = new RustFloatDataType(64).with("f64");

  private int mBitSize;

  private RustFloatDataType(int bitSize) {
    mBitSize = bitSize;
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public final String provideSourceDefaultValue() {
    return "0.0";
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {
    return Double.toString(json.getDouble(""));
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.a("m.put_"+bitSizeSuffix() +"(", f.nameStringConstantQualified(), ", ", //
        sourceGenerateSerializeToObjectExpression("self." + f.instanceName()), ");", CR);
  }

  private String bitSizeSuffix() {
    return "f"+mBitSize;
  }
  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = m.opt(", f.nameStringConstantQualified(), ").or_",bitSizeSuffix(),"(", f.defaultValueSource(), ")?");
    if (mBitSize != 64)
      s.a(" as i", mBitSize);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    var x = valueExpression;
    if (mBitSize != 64)
      x += " as i64";
    return x;
  }

  @Override
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    var x = jsentityExpression + ".as_int()?";
    if (mBitSize != 64)
      x += " as i" + mBitSize;
    return x;
  }

  @Override
  public String deserializeStringToMapKey(String stringValueExpression) {
    return stringValueExpression;
  }

  @Override
  public String deserializeJsonToMapValue(String jsonValue) {
    return jsonValue + ".as_int()?";
  }

  @Override
  public String setterArgSignature(String expr) {
    return "i" + mBitSize;
  }

  @Override
  public String setterArgUsage(String expr) {
    return expr;
  }

  @Override
  public String buildRustJsonValueFrom(String expr) {
    return "new_int(*" + expr + ((mBitSize != 64) ? " as i64" : "") + ")";
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = m.opt(", f.nameStringConstantQualified(), ").or_empty_list()?.parse_i",
        mBitSize, "_list()?;");
  }

  @Override
  public void generateSerializeListOf(SourceBuilder s, FieldDef f) {
    s.a("encode_i", mBitSize, "_list(&self.", f.instanceName(), ")");
  }

}
