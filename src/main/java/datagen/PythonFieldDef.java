package datagen;

import js.data.DataUtil;

public class PythonFieldDef extends FieldDef {


  @Override
  protected String provideNameStringConstantUnqualified() {
    return "_key_" + sourceName();
  }

  @Override
  protected String provideNameStringConstantQualified() {
    return Context.generatedTypeDef.name() + "." + nameStringConstantUnqualified();
  }


  @Override
  protected String provideInstanceName() {
    return "_" + DataUtil.convertUnderscoresToCamelCase(name());
  }
  
  @Override
  protected String provideSourceName() {
    return DataUtil.lowerFirst(name());
  }

  @Override
  protected String provideSourceNameLowerFirst() {
    return DataUtil.lowerFirst(name());
  }

}
