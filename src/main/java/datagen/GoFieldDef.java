package datagen;

import static js.base.Tools.*;

import js.data.DataUtil;
import static datagen.Utils.*;

public class GoFieldDef extends FieldDef {

  @Override
  public String provideSetterName() {
    return "Set" + provideGetterName();
  }

  @Override
  public String provideGetterName() {
    return DataUtil.convertUnderscoresToCamelCase(name());
  }

  @Override
  public String provideNameStringConstantUnqualified() {
    return quote(DataUtil.convertCamelCaseToUnderscores(name()));
  }

  @Override
  public String provideNameStringConstantQualified() {
    return provideNameStringConstantUnqualified();
  }

  @Override
  public String provideInstanceName() {
    return DataUtil.lowerFirst(DataUtil.convertUnderscoresToCamelCase(name()));
  }

  @Override
  public String provideConstantName() {
    return verboseVariant("_d" + index(), "def_" + name().toUpperCase());
  }
}
