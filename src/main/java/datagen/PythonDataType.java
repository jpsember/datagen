package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

public class PythonDataType extends DataType {

  public final boolean isPrimitive() {
    return qualifiedClassName().packagePath().isEmpty();
  }

  /**
   * Generate source code for deserializing a value from a dict
   */
  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("inst.", f.instanceName(), " = obj.get(", f.nameStringConstantQualified(), ", ",
        f.defaultValueOrNull(), ")");
  }

  @Override
  public void sourceGenerateEquals(SourceBuilder s, String a, String b) {
    todo("apparently if classes implement an __eq__ method, we can just use '==', and this holds for arrays");
    if (isPrimitive())
      s.a(a, " == ", b);
    else {
      todo("deprecate this");
      s.a(a, ".equals(", b, ")");
    }
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    todo("should we add Python type hints?");
    s.a("r = r * 37 + hash(self.", f.instanceName(), ")");
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
    s.doIf(f.optional(), "if self.", f.instanceName(), " is not None:", OPEN);
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
          "inst.", f.instanceName(), " = t.copy()", CLOSE);
    } else {
      s.a("inst.", f.instanceName(), " = obj.get(", f.nameStringConstantQualified(), ", ",
          f.nullIfOptional("[]"), ").copy()", CR);
    }
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a("m[", f.nameStringConstantQualified(), "] = ",
        sourceGenerateSerializeToObjectExpression("self." + f.instanceName()));
    sourceEndIf(s).cr();
  }
}
