package datagen;

import datagen.gen.Language;

public class JavaDataType extends DataType {

  @Deprecated
  public Language language() {
    return Utils.language();
  }

  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    if (f.optional() || isPrimitive()) {
      s.a(targetExpr, " = ", //
          sourceExpressionToMutable("x"));
    } else {
      s.a(targetExpr, " = ", //
          sourceExpressionToMutable("(x == null) ? " + f.defaultValueOrNull() + " : x"));
    }
    s.a(";");
  }
}
