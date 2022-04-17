package datagen;

import datagen.gen.Language;

public class PythonDataType extends DataType {

  @Deprecated
  public Language language() {
    return Utils.language();
  }

  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    if (f.optional()) {
      s.a(targetExpr, " = ", //
          sourceExpressionToMutable("x"));
    } else {
      s.a(targetExpr, " = ", //
          f.defaultValueOrNull(), " if x is None else ", sourceExpressionToMutable("x"));
    }
  }
}
