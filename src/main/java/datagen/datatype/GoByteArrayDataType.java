package datagen.datatype;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.SourceBuilder;

public class GoByteArrayDataType extends GoContractDataType {

  public static final DataType TYPE = new GoByteArrayDataType().with("[]byte");

  private GoByteArrayDataType() {
  }

  @Override
  public String provideSourceDefaultValue() {
    return "[]byte{}";
  }

  @Override
  public String getSerializeToJSONValue(String value) {
    return Context.pt.PKGGO_DATA + "EncodeBase64Maybe(" + value + ")";
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    throw notSupported();
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = s.OptBytes(\"", f.name(), "\", ", f.defaultValueSource(), ")", CR);
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    //    String defaultValue = f.defaultValueOrNull();

    s.a("if ", f.instanceName(), " != nil", OPEN, //  
        "v.", f.instanceName(), " = ", f.instanceName(), OUT, //
        "} else {", IN, //
        "v.", f.instanceName(), " = ", f.defaultValueSource(), CLOSE);
  }

}
