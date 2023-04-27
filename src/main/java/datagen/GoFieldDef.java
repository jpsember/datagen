package datagen;

import js.data.DataUtil;
import static datagen.Utils.*;

public class GoFieldDef extends FieldDef {

  @Override
  public String provideSetterName() {
    return provideGetterName() + "Set";
  }

  @Override
  public String provideGetterName() {
    return DataUtil.convertUnderscoresToCamelCase(name());
  }

  @Override
  public String provideNameStringConstantUnqualified() {
    return verboseVariant("_" + index(), name().toUpperCase());
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
