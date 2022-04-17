package datagen;

import js.data.DataUtil;

public class JavaFieldDef extends FieldDef {

  private static final boolean SUCCINCT_NAMES = true;

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
    if (SUCCINCT_NAMES)
      return "_" + index();
    return name().toUpperCase();
  }

  @Override
  public String provideNameStringConstantQualified() {
    return provideNameStringConstantUnqualified();
  }

  @Override
  public String provideInstanceName() {
    if (SUCCINCT_NAMES)
      return "m" + index();
    return "m" + DataUtil.convertUnderscoresToCamelCase(name());
  }

}
