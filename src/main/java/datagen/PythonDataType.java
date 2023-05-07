package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

public class PythonDataType extends DataType {

  public final boolean isPrimitive() {
    return qualifiedName().packagePath().isEmpty();
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
  public void sourceIfNotNull(SourceBuilder s, String expr) {
    s.doIf(true, "if ", expr, " is not None:", OPEN);
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
    if (f.optional()) {
      s.a("x = obj.get(", f.nameStringConstantQualified(), ", ", f.nullIfOptional("[]"), ")", CR, //
          "if x is not None:", OPEN, //
          "inst.", f.instanceName(), " = x.copy()", CLOSE);
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
