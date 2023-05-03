package datagen.datatype;

import static datagen.ParseTools.*;
import static js.base.Tools.*;
import static datagen.SourceBuilder.*;

import java.util.List;

import datagen.DataType;
import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.ParseTools;
import datagen.SourceBuilder;
import js.parsing.Scanner;

public class GoListDataType extends JavaDataType {

  public GoListDataType(DataType wrappedType) {
    mWrappedType = wrappedType;
    setQualifiedClassName(
        ParseTools.parseQualifiedName("[]" + wrappedType.qualifiedClassName().className(), null));
  }

  @Override
  protected String provideTypeName() {
    return "[]" + ParseTools.importExprWithClassName(wrappedType().qualifiedClassName());
  }

  @Override
  public String provideSourceDefaultValue() {
    return "[]" + mWrappedType.typeName() + "{}";
  }

  //  /**
  //   * Constructs a mutable copy of a list. Note that while it creates a copy of
  //   * the list, it doesn't create copies of its elements; the references to those
  //   * elements are stored in the new list unchanged.
  //   * 
  //   * This does not apply unless 'old style' is in effect
  //   */
  //  @Override
  //  public String sourceExpressionToMutable(String valueExpression) {
  //    if (!Context.generatedTypeDef.classMode())
  //      return ParseTools.mutableCopyOfList(valueExpression);
  //    // In debug mode, this is already in immutable form; no need to modify it
  //    return valueExpression;
  //  }
  //
  //  @Override
  //  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
  //      String valueExpression) {
  //    if (!Context.generatedTypeDef.classMode()) {
  //      s.a(targetExpression, " = ", ParseTools.immutableCopyOfList(valueExpression));
  //      return;
  //    }
  //
  //    if (Context.debugMode()) {
  //      s.a(targetExpression, " = ", ParseTools.immutableCopyOfList(valueExpression));
  //      return;
  //    }
  //    super.sourceExpressionToImmutable(s, fieldDef, targetExpression, valueExpression);
  //  }

  public DataType wrappedType() {
    return mWrappedType;
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a("=== not finished sourceSerializeToObject===", CR);
    //    s.a(OPEN, //
    //        ParseTools.PKG_JSLIST, " j = new ", ParseTools.PKG_JSLIST, "();", CR, //
    //        "for (", wrappedType().typeName(), " x : ", f.instanceName(), ")", IN, //
    //        "j.add(", wrappedType().sourceGenerateSerializeToObjectExpression("x"), ");", OUT, //
    //        "m.put(", f.nameStringConstantQualified(), ", j);", //
    //        CLOSE, CR);
    sourceEndIf(s);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    wrappedType().sourceDeserializeFromList(s, f);
  }

  @Override
  public String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource, FieldDef fieldDef) {

    List<String> parsedExpressions = arrayList();

    {
      scanner.read(SQOP);
      for (int index = 0;; index++) {
        if (scanner.readIf(SQCL) != null)
          break;
        if (index > 0) {
          scanner.read(COMMA);
          // Allow an extraneous trailing comma
          if (scanner.readIf(SQCL) != null)
            break;
        }
        String expr = wrappedType().parseDefaultValue(scanner, classSpecificSource, null);
        parsedExpressions.add(expr);
      }
    }

    // Construct the class-specific source (which will appear elsewhere in the generated source file)
    // defining the variable containing the default value for the list
    //
    SourceBuilder sb = classSpecificSource;
    sb.a("var ", fieldDef.constantName(), " = ", typeName(), "{");
    int index = INIT_INDEX;
    for (String expr : parsedExpressions) {
      index++;
      if (index > 0)
        sb.a(", ");
      sb.a(expr);
    }
    sb.a("}", CR);

    return fieldDef.constantName();
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {

    String argName = f.instanceName();

    s.a("if ", argName, " == nil", OPEN, //
        argName, " = ", f.defaultValueOrNull(), CLOSE, //
        targetExpr, " = ", argName //
    );
    s.a(" // not implemented: in debug mode, set to an immutable slice?", CR);
  }

  private final DataType mWrappedType;
}
