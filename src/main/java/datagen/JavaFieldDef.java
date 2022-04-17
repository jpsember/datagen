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
    if (!verboseNames())
      return "_" + index();
    return name().toUpperCase();
  }

  @Override
  public String provideNameStringConstantQualified() {
    return provideNameStringConstantUnqualified();
  }

  @Override
  public String provideInstanceName() {
    if (!verboseNames())
      return "m" + index();
    return "m" + DataUtil.convertUnderscoresToCamelCase(name());
  }

}
