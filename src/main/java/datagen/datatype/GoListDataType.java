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

    pr("goListDataType, wrapped type:",wrappedType);
    alert(
        "If wrapped type is a ContractDataType, I think we want an array of pointers to objects, not objects");

    String prefix = "";
    if (wrappedType instanceof ContractDataType) {
      //    halt("wrapped type:", wrappedType.qualifiedClassName());
      prefix = "*";
    }

    setQualifiedClassName(
        ParseTools.parseQualifiedName("[]" + prefix + wrappedType.qualifiedClassName().className(), null));
pr("qualifiedClassName:",qualifiedClassName());
  }

  @Override
  protected String provideTypeName() {
    if (true) {
    return qualifiedClassName().className();
    }
    
    pr("providing type name, wrapped type:",wrappedType().qualifiedClassName());
    return "[]" + ParseTools.importExprWithClassName(wrappedType().qualifiedClassName());
  }

  @Override
  public String provideSourceDefaultValue() {
    return "[]" + mWrappedType.typeName() + "{}";
  }

  public DataType wrappedType() {
    return mWrappedType;
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    s.a(OPEN, //
        "var list = NewJSList()", CR, //
        "for _, x := range v.", f.instanceName(), " ", OPEN, //
        "list.Add(", wrappedType().sourceGenerateSerializeToObjectExpression("x"), ")", //
        CLOSE, //
        "m.Put(", quote(f.instanceName()), ", list)", // 
        CLOSE);
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
