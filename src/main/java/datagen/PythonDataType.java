package datagen;

import static datagen.SourceBuilder.*;

public class PythonDataType extends DataType {

  /**
   * Generate source code for deserializing a value from a dict
   */
  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("inst._", f.sourceName(), " = obj.get(", f.nameStringConstant(), ", ", f.defaultValueOrNull(), ")");
  }

  /**
   * Generate source code to continue the calculation of a value for hashCode().
   */
  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a("r = r * 37 + hash(self._", f.sourceName(), ")");
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

  @Override
  public void sourceIfNotNull(SourceBuilder s, FieldDef f) {
    s.doIf(f.optional(), "if self._", f.sourceName(), " is not None:", OPEN);
  }

  @Override
  public SourceBuilder sourceEndIf(SourceBuilder s) {
    return s.endIf(CLOSE);
  }

}
