package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

public abstract class GoDataType extends DataType {

  /**
   * Generate source code for deserializing a value from a dict
   */
  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    notFinished();
    s.a("inst.", f.instanceName(), " = obj.get(", f.nameStringConstantQualified(), ", ",
        f.defaultValueOrNull(), ")");
  }

  @Override
  public void sourceGenerateEquals(SourceBuilder s, String a, String b) {
    notSupported();
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    notSupported();
  }

  public   void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    String arg = f.instanceName();
    if (isPrimitive()) {
      s.a(targetExpr, " = ", arg, CR);
    } else {
      s.a("if x == nil {", IN, //
          "x = ", f.defaultValueOrNull(), OUT, //
          targetExpr, " = x", CR);
    }
  }

  @Override
  public void sourceIfNotNull(SourceBuilder s, String expr) {
    notFinished();
    s.doIf(true, "if ", expr, " is not None:", OPEN);
  }

  @Override
  public void sourceIfNotNull(SourceBuilder s, FieldDef f) {
    notFinished();
    s.doIf(f.optional(), "if self.", f.instanceName(), " is not None:", OPEN);
  }

  @Override
  public SourceBuilder sourceEndIf(SourceBuilder s) {
    return s.endIf(CLOSE);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    notFinished();
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
    notFinished();
    sourceIfNotNull(s, f);
    s.a("m[", f.nameStringConstantQualified(), "] = ",
        sourceGenerateSerializeToObjectExpression("self." + f.instanceName()));
    sourceEndIf(s).cr();
  }
}
