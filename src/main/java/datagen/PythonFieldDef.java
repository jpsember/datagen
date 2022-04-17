package datagen;

import js.data.DataUtil;

public class PythonFieldDef extends FieldDef {
 
  @Override
  public String provideSetterName() {
    return sourceNameLowerFirst();
  }

  @Override
  public String provideGetterName() {
    return sourceNameLowerFirst();
  }

  @Override
  protected String provideNameStringConstantUnqualified() {
    return "_key_" + DataUtil.lowerFirst(name());
  }

  @Override
  protected String provideNameStringConstantQualified() {
    return Context.generatedTypeDef.name() + "." + nameStringConstantUnqualified();
  }

  @Override
  protected String provideInstanceName() {
    return "_" + DataUtil.lowerFirst(name());
  }

  @Override
  protected String provideSourceNameLowerFirst() {
    return DataUtil.lowerFirst(name());
  }

  @Override
  protected String provideSourceName() {
    return DataUtil.lowerFirst(name());
  }

}
