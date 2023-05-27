package datagen.datatype;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import datagen.Context;
import datagen.FieldDef;
import datagen.GoDataType;
import datagen.SourceBuilder;
import js.json.JSMap;

public class GoMapDataType extends GoDataType {

  public GoMapDataType(GoDataType keyType, GoDataType valueType) {
    mWrappedKeyType = keyType;
    mWrappedValueType = valueType;
    with("map[" + keyType.qualifiedName().className() + "]" + valueType.qualifiedName().className());
  }

  public GoDataType wrappedKeyType() {
    return mWrappedKeyType;
  }

  public GoDataType wrappedValueType() {
    return mWrappedValueType;
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public String provideSourceDefaultValue() {
    alert("some duplication here, with the constructor");
    return "make(map[" + mWrappedKeyType.qualifiedName().className() + "]"
        + mWrappedValueType.qualifiedName().className() + ")";
  }

  /**
   * Constructs a mutable copy of a map. Note that while it creates a copy of
   * the map, it doesn't create copies of its elements; the references to those
   * elements are stored in the new map unchanged.
   */
  @Override
  public String sourceExpressionToMutable(String valueExpression) {
    if (!Context.generatedTypeDef.classMode())
      return Context.pt.mutableCopyOfMap(valueExpression);
    return valueExpression;
  }

  @Override
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    if (!Context.generatedTypeDef.classMode()) {
      s.a(targetExpression, " = ", Context.pt.immutableCopyOfMap(valueExpression));
      return;
    }
    if (Context.debugMode()) {
      s.a(targetExpression, " = ", Context.pt.immutableCopyOfMap(valueExpression));
      return;
    }
    super.sourceExpressionToImmutable(s, fieldDef, targetExpression, valueExpression);
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    todo("this is a confusingly-named method; should be serializeToJson, or just toJson");
    s.a(OPEN, //
        "m2 := ", Context.pt.PKGGO_JSON, "NewJSMap()", CR, // 
        "for k, v := range v.", f.instanceName(), OPEN, //
        "m2.Put(", wrappedKeyType().sourceGenerateSerializeToObjectExpression("k"), ", ", //
        wrappedValueType().sourceGenerateSerializeToObjectExpression("v"), ")", CLOSE, //
        "m.Put(", f.nameStringConstantQualified(), ", m2)", //
        CLOSE, CR);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.open();
    s.a("m2 := s.OptMapOrEmpty(", f.nameStringConstantQualified(), ")", CR, //
        "for k, v := range m2.WrappedMap() ", OPEN, //
        "n.", f.instanceName(), "[", wrappedKeyType().deserializeStringToMapKey("k"), "] = ",
        wrappedValueType().deserializeJsonToMapValue("v"), CLOSE //
    );
    s.close();
  }

  @Override
  public String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDefOrNull, JSMap json) {
    FieldDef fieldDef = fieldDefOrNull;
    return fieldDef.constantName();
  }

  private final GoDataType mWrappedKeyType;
  private final GoDataType mWrappedValueType;
}
