package datagen;

import static js.base.Tools.*;

import js.data.DataUtil;

public class RustFieldDef extends FieldDef {


  @Override
  public String provideSetterName() {
    return "set_" + provideGetterName();
  }

  @Override
  public String provideGetterName() {
    return DataUtil.convertCamelCaseToUnderscores(name());
  }

  @Override
  public String provideNameStringConstantUnqualified() {
    var d = Context.generatedTypeDef;
    SourceBuilder s = d.classSpecificSourceBuilder();
    var goName = DataUtil.convertCamelCaseToUnderscores(name());
    var varName = ("key_" + goName).toUpperCase();
    s.a("const ", varName, ": &str = \"", name(), "\";", CR);
    return varName;
  }

  @Override
  public String provideNameStringConstantQualified() {
    return provideNameStringConstantUnqualified();
  }

  @Override
  public String provideInstanceName() {
    return DataUtil.convertCamelCaseToUnderscores(name());
  }

  @Override
  public String provideConstantName() {
    todo("!this doesn't need to be so involved");
    return "c_" + Context.generatedTypeDef.qualifiedName().combined().replace('.', '_') + "_" + index();
  }

  @Override
  public String defaultValueSource() {
    return dataType().setterArgUsage( super.defaultValueSource() );
  }

}
