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

  public String getConstructFromX() {
    return "x.build()";
  }

  @Override
  public String parseDefaultValue(SourceBuilder sb, FieldDef fieldDef, JSMap json) {
    //      JSList list = json.getList("");

    // We parsed the default value into a JSMap.  Read the coordinates from that,
    // so we can construct it in Go code from the literal values.
    IPoint value = IPoint.get(json, "");

    sb.a("var ", fieldDef.constantName(), " = IPointWith(", value.x, ",", value.y, ")", CR);

    //      String constName = "DEF" + fieldDef.nameStringConstantUnqualified();
    //      sb.a(constName, "  = ", typeName(), ".with_x_y(", list.getInt(0), ", ", list.getInt(1), ")", CR);
    // return constName;

    return fieldDef.constantName();
  }
}
