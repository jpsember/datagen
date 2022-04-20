package datagen;

import js.data.DataUtil;
import static datagen.Utils.*;

public class JavaFieldDef extends FieldDef {

  @Override
  public String provideSetterName() {
    return DataUtil.lowerFirst(DataUtil.convertUnderscoresToCamelCase(name()));
  }

  @Override
  public String provideGetterName() {
    return provideSetterName();
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
    if (true) {
      // Our tooling uses reflection to find the instance field, so avoid mangling it...
      return "m" + DataUtil.convertUnderscoresToCamelCase(name());
    }
    return verboseVariant("m" + index(), "m" + DataUtil.convertUnderscoresToCamelCase(name()));
  }

  @Override
  public String provideConstantName() {
    return verboseVariant("_D" + index(), "DEF_" + name().toUpperCase());
  }
}
