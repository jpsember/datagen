package datagen.datatype;

import static js.base.Tools.*;

import datagen.DataType;
import datagen.FieldDef;
import datagen.SourceBuilder;
import js.geometry.IPoint;
import js.json.JSMap;
import static datagen.ParseTools.*;

public class GoIPointDataType extends GoContractDataType {

  public static final DataType TYPE = new GoIPointDataType().with(goModuleExpr("IPoint"));

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    s.a(targetExpr, " = ", f.instanceName(), CR);
  }

  @Override
  public String parseDefaultValue(SourceBuilder sb, FieldDef fieldDef, JSMap json) {
    // We parsed the default value into a JSMap.  Read the coordinates from that,
    // so we can construct it in Go code from the literal values.
    IPoint value = IPoint.get(json, "");
    sb.a("var ", fieldDef.constantName(), " = IPointWith(", value.x, ",", value.y, ")", CR);
    return fieldDef.constantName();
  }
}
