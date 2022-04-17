package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

public class PythonDataType extends DataType {

  /**
   * Generate source code for deserializing a value from a dict
   */
  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("inst._", f.sourceName(), " = obj.get(", f.nameStringConstantQualified(), ", ", f.defaultValueOrNull(), ")");
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

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    todo("!we should have some symbolic constants for things like t, inst");
    todo("!we should optimize later by having a utility class to reduce volume of boilerplate");
    if (f.optional()) {
      s.a("t = obj.get(", f.nameStringConstantQualified(), ", ", f.nullIfOptional("[]"), ")", CR, //
          "if t is not None:", OPEN, //
          "inst._", f.sourceName(), " = t.copy()", CLOSE);
    } else {
      s.a("inst._", f.sourceName(), " = obj.get(", f.nameStringConstantQualified(), ", ", f.nullIfOptional("[]"),
          ").copy()", CR);
    }
  }
}
