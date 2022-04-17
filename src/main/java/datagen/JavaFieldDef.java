package datagen;

public class JavaFieldDef extends FieldDef {

  @Override
  protected String provideNameStringConstant() {
    return name().toUpperCase();
  }

  @Override
  protected String provideNameStringConstantQualified() {
    return name().toUpperCase();
  }

}
