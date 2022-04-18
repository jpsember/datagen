package datagen;

import static datagen.Utils.*;

public class PythonFieldDef extends FieldDef {

  @Override
  public String provideSetterName() {
    return "set_" + provideGetterName();
  }

  @Override
  public String provideGetterName() {
    return name();
  }

  @Override
  public String provideNameStringConstantUnqualified() {
    return verboseVariant("_k_" + index(), "_key_" + name());
  }

  @Override
  public String provideNameStringConstantQualified() {
    return Context.generatedTypeDef.name() + "." + nameStringConstantUnqualified();
  }

  @Override
  public String provideInstanceName() {
    return verboseVariant("_" + index(), "_" + name());
  }

  @Override
  public String provideConstantName() {
    return verboseVariant("_D" + index(), "DEF_" + name().toUpperCase());
  }
}
