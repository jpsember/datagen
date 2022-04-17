package datagen;

import js.data.DataUtil;

public class PythonFieldDef extends FieldDef {

  @Override
  public String provideSetterName() {
    return "set_" + provideGetterName();
  }

  @Override
  public String provideGetterName() {
    return DataUtil.lowerFirst(name());
  }

  @Override
  public String provideNameStringConstantUnqualified() {
    if (!verboseNames())
      return "_k_" + index();
    return "_key_" + DataUtil.lowerFirst(name());
  }

  @Override
  public String provideNameStringConstantQualified() {
    return Context.generatedTypeDef.name() + "." + nameStringConstantUnqualified();
  }

  @Override
  public String provideInstanceName() {
    if (!verboseNames())
      return "_" + index();
    return DataUtil.lowerFirst(name());
  }

}
