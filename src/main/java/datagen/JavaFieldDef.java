package datagen;

import js.data.DataUtil;

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
    return name().toUpperCase();
  }

  @Override
  public String provideNameStringConstantQualified() {
    return name().toUpperCase();
  }

  @Override
  public String provideInstanceName() {
    return "m" + DataUtil.convertUnderscoresToCamelCase(name());
  }

}
