package datagen.datatype;

import datagen.FieldDef;
import datagen.GoDataType;
import datagen.SourceBuilder;

import static datagen.ParseTools.*;
import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import java.util.List;

import datagen.ParseTools;
import js.data.DataUtil;
import js.parsing.Scanner;
import js.parsing.Token;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class GoContractDataType extends GoDataType implements ContractDataType {

  @Override
  protected String provideTypeName() {
    // We will consider the typeName() to be "FooOrBuilder", and the builtTypeName() to be "Foo".
    //
    String expr = super.provideTypeName();
    mBuiltTypeName = expr;
    return expr + "OrBuilder";
  }

  private String builtTypeName() {
    typeName();
    return mBuiltTypeName;
  }

  private String mBuiltTypeName;

  @Override
  public String provideSourceDefaultValue() {
    return "Default" + ParseTools.importExprWithClassName(qualifiedClassName());
  }

  public String getConstructFromX() {
    todo("!this won't work for non-primitive data types");
    return "x.copy()";
  }

  public String getSerializeDataType() {
    return "??Object??";
  }

  // Make this final for now to avoid unintended overriding
  @Override
  public final void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    // If there is a value for this key, use the type's default instance to parse that value
    // and store that parsed value.
    // Otherwise, if there is no value, leave the current value alone (which may be None, e.g. if value is optional)
    //
    String className = builtTypeName();
    s.a(OPEN, "var z = s.OptMap(\"", f.name(), "\")", CR, //
        "if z != nil ", OPEN, //
        "n.", f.instanceName(), " = Default", className, ".Parse(z).(*", className, ")", //
        CLOSE, //
        CLOSE);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("inst.", f.instanceName(), " = ", ParseTools.PKGPY_DATAUTIL, ".parse_list_of_objects(",
        builtTypeName(), ".default_instance, obj.get(", f.nameStringConstantQualified(), "), ",
        f.optional() ? "True" : "False", ")");
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    if (true)
      return "???sourceGenerateSerializeToObjectExpression";
    return getSerializeToJSONValue(valueExpression);
  }

  public String getSerializeToJSONValue(String value) {
    if (true)
      return "???getSerializeToJSONValue";
    return value + ".to_json()";
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {

    String className = builtTypeName();

    String arg = f.instanceName();
    s.a("if ", arg, " == nil ", OPEN, //
        "v.m.", f.instanceName(), " = Default", className, //
        CR, "} else {", CR, "v.m.", f.instanceName(), " = ", arg, ".Build()", //,
        CLOSE);
  }

  @Override
  public String parseDefaultValue(Scanner scanner, SourceBuilder sb, FieldDef fieldDef) {

    // Attempt to infer from the tokens how to parse a default value

    Token t = scanner.peek();

    // If it looks like [ ...., ...]
    //
    // then scan a sequence of comma-delimited strings, and pass as arguments to a constructor
    // 
    if (t.id(SQOP)) {
      scanner.read();
      List<String> exprs = arrayList();
      boolean commaExp = false;
      while (scanner.readIf(SQCL) == null) {
        if (commaExp) {
          scanner.read(COMMA);
          commaExp = false;
          continue;
        }
        exprs.add(scanner.read().text());
        commaExp = true;
      }

      String constName;
      constName = "DEF" + fieldDef.nameStringConstantUnqualified();
      sb.a(constName, "  = ", typeName(), "(");
      int i = INIT_INDEX;
      for (String expr : exprs) {
        i++;
        if (i > 0)
          sb.a(", ");
        sb.a(expr);
      }
      sb.a(")", CR);

      return constName;
    }
    throw notSupported("can't parse default value for token:", t);
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

}
