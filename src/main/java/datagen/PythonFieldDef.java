package datagen;

public class PythonFieldDef extends FieldDef {

  @Override
  protected String provideNameStringConstant() {
    return "_key_" + sourceName();
  }

  @Override
  protected String provideNameStringConstantQualified() {
    return Context.generatedTypeDef.name() + "." + nameStringConstantUnqualified();
  }

}
