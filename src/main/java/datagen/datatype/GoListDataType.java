package datagen.datatype;

import static js.base.Tools.*;
import static datagen.SourceBuilder.*;

import datagen.DataType;
import datagen.FieldDef;
import datagen.GoDataType;
import datagen.SourceBuilder;
import js.json.JSList;
import js.json.JSMap;
import js.json.JSUtils;

public class GoListDataType extends GoDataType {

  public GoListDataType(DataType wrappedType) {
    mWrappedType = wrappedType;
    with("[]" + wrappedType.qualifiedName().className());
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public String provideSourceDefaultValue() {
    return "[]" + wrappedType().typeName() + "{}";
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
  public String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap json) {

    JSList parsedExpressions = json.getList("");

    SourceBuilder sb = classSpecificSource;
    sb.a("var ", fieldDef.constantName(), " = ", typeName(), "{");

    for (int index = 0; index < parsedExpressions.size(); index++) {
      Object expr = parsedExpressions.getUnsafe(index);
      if (index > 0)
        sb.a(", ");
      sb.a(JSUtils.valueToString(expr));
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
