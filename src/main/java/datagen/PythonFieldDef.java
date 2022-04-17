package datagen;

import js.data.DataUtil;

public class PythonFieldDef extends FieldDef {

  private static final boolean SUCCINCT_NAMES = true;

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
    if (SUCCINCT_NAMES)
      return "_k_" + index();
    return "_key_" + DataUtil.lowerFirst(name());
  }

  @Override
  public String provideNameStringConstantQualified() {
    return Context.generatedTypeDef.name() + "." + nameStringConstantUnqualified();
  }

  @Override
  public String provideInstanceName() {
    if (SUCCINCT_NAMES)
      return "_" + index();
    return DataUtil.lowerFirst(name());
  }

}
