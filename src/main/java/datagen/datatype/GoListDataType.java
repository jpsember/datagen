package datagen.datatype;

import static js.base.Tools.*;
import static datagen.SourceBuilder.*;

import datagen.DataType;
import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.ParseTools;
import datagen.SourceBuilder;
import js.json.JSList;
import js.json.JSMap;

public class GoListDataType extends JavaDataType {

  public GoListDataType(DataType wrappedType) {
    mWrappedType = wrappedType;
    alert(
        "If wrapped type is a ContractDataType, I think we want an array of pointers to objects, not objects");

    setQualifiedClassName(
        ParseTools.parseQualifiedName("[]" + wrappedType.qualifiedClassName().className(), null));
  }

  @Override
  protected String provideTypeName() {
    return qualifiedClassName().className();
  }

  @Override
  public String provideSourceDefaultValue() {
    return "[]" + wrappedType().typeName() + "{}";
  }

  public DataType wrappedType() {
    return mWrappedType;
  }

  //  /**
  //   * Get alternate name of wrapped type, if it's a ContractDataType. Converts
  //   * 'Foo' to 'FooOrBuilder'
  //   */
  //  private QualifiedName wrappedInterfaceName() {
  //    if (mQualifiedInterfaceName == null) {
  //      if (wrappedType() instanceof ContractDataType) {
  //        QualifiedName.Builder b = wrappedType().qualifiedClassName().toBuilder();
  //        b.className(b.className() + "OrBuilder");
  //        b.combined(b.packagePath() + b.className());
  //        mQualifiedInterfaceName = b.build();
  //      } else {
  //        mQualifiedInterfaceName = wrappedType().qualifiedClassName();
  //      }
  //    }
  //    return mQualifiedInterfaceName;
  //  }
  //
  //  private QualifiedName mQualifiedInterfaceName;

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
  public String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {

    JSList parsedExpressions = json.getList("");
    //    {
    //    SourceBuilder sb = classSpecificSource;
    //    sb.a("  private static final ", typeName(), " ", fieldDef.constantName(), " = ", ParseTools.PKG_TOOLS,
    //        ".arrayList(");
    //    for (int index = 0; index < parsedExpressions.size(); index++) {
    //      Object expr = parsedExpressions.getUnsafe(index);
    //      if (index > 0)
    //        sb.a(",");
    //      sb.a(expr);
    //    }
    //    sb.a(");").cr();
    //    }
    //    
    //    
    //    

    //    
    //    List<String> parsedExpressions = arrayList();
    //
    //    {
    //      scanner.read(SQOP);
    //      for (int index = 0;; index++) {
    //        if (scanner.readIf(SQCL) != null)
    //          break;
    //        if (index > 0) {
    //          scanner.read(COMMA);
    //          // Allow an extraneous trailing comma
    //          if (scanner.readIf(SQCL) != null)
    //            break;
    //        }
    //        String expr = wrappedType().parseDefaultValue(scanner, classSpecificSource, null);
    //        parsedExpressions.add(expr);
    //      }
    //    }

    // Construct the class-specific source (which will appear elsewhere in the generated source file)
    // defining the variable containing the default value for the list
    //
    SourceBuilder sb = classSpecificSource;
    sb.a("var ", fieldDef.constantName(), " = ", typeName(), "{");

    for (int index = 0; index < parsedExpressions.size(); index++) {
      Object expr = parsedExpressions.getUnsafe(index);
      if (index > 0)
        sb.a(",");
      sb.a(expr);
    }
    //    
    //    int index = INIT_INDEX;
    //    for (String expr : parsedExpressions) {
    //      index++;
    //      if (index > 0)
    //        sb.a(", ");
    //      sb.a(expr);
    //    }
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
