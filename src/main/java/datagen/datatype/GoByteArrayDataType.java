package datagen.datatype;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.GoDataType;
import datagen.SourceBuilder;

public class GoByteArrayDataType extends GoDataType {

  public static final DataType TYPE = new GoByteArrayDataType().with("[]byte");

  private GoByteArrayDataType() {
  }

  @Override
  public boolean isPrimitive() {
    alert("not sure");
    return true;
  }

  @Override
  public String provideSourceDefaultValue() {
    return "[]byte{}";
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
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.a("m.Put(", f.nameStringConstantQualified(), ", ", //
        Context.pt.PKGGO_JSON + "EncodeBase64Maybe(v." + f.instanceName() + "))", CR);
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    s.a("if ", f.instanceName(), " != nil", OPEN, //  
        "v.", f.instanceName(), " = ", f.instanceName(), OUT, //
        "} else {", IN, //
        "v.", f.instanceName(), " = ", f.defaultValueSource(), CLOSE);
  }


  public String sqlType() {
    return "BLOB";
  }
}
