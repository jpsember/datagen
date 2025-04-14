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
    var goName = DataUtil.convertUnderscoresToCamelCase(name());
    var varName = d.qualifiedName().className() + "_" + goName;
    s.a("const ", varName, " = \"", name(), "\"", CR);
    return varName;
  }

  @Override
  public String provideNameStringConstantQualified() {
    return provideNameStringConstantUnqualified();
  }

  @Override
  public String provideInstanceName() {
    return DataUtil.lowerFirst(DataUtil.convertUnderscoresToCamelCase(name()));
  }

  @Override
  public String provideConstantName() {
    // We don't have names private to classes like in Java, so we have a more elaborate prefix
    return "c_" + Context.generatedTypeDef.qualifiedName().combined().replace('.', '_') + "_" + index();
  }
}
