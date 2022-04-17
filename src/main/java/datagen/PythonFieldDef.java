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
    return "_key_" + DataUtil.lowerFirst(name());
  }

  @Override
  public String provideNameStringConstantQualified() {
    return Context.generatedTypeDef.name() + "." + nameStringConstantUnqualified();
  }

  @Override
  public String provideInstanceName() {
    return "_" + DataUtil.lowerFirst(name());
  }

}
