package datagen;

import js.data.DataUtil;

public class JavaFieldDef extends FieldDef {

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
    return name().toUpperCase();
  }

  @Override
  protected String provideNameStringConstantQualified() {
    return name().toUpperCase();
  }

  @Override
  protected String provideSourceName() {
    return DataUtil.convertUnderscoresToCamelCase(name());
  }

  @Override
  protected String provideSourceNameLowerFirst() {
    return DataUtil.lowerFirst(DataUtil.convertUnderscoresToCamelCase(name()));
  }

  @Override
  protected String provideInstanceName() {
    return "m" + DataUtil.convertUnderscoresToCamelCase(name());
  }

}
