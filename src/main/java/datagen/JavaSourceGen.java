/**
 * MIT License
 *
 * Copyright (c) 2021 Jeff Sember
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/
package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import java.util.List;
import java.util.Set;

import datagen.datatype.EnumDataType;
import js.file.Files;

public final class JavaSourceGen extends SourceGen {

  @Override
  protected String getTemplate() {
    if (Context.generatedTypeDef.isEnum())
      return sEnumTemplate;
    else
      return sClassTemplate;
  }

  @Override
  protected String generatePackageDecl() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    return "package " + def.qualifiedName().packagePath() + ";";
  }

  @Override
  protected String generateCopyFromBuilderToImmutable() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(6);
    for (FieldDef f : def.fields()) {
      s.a("r.", f.instanceName(), " = ", f.dataType().builderToStatic(f.instanceName()), ";");
      s.cr();
    }
    return chomp(content());
  }

  @Override
  protected final String generateInitInstanceFields() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(4);
    for (FieldDef f : def.fields()) {
      if (f.optional())
        continue;

      // We don't need an explicit initializer if the desired initial value equals the Java default value
      String initialValue = f.defaultValueOrNull();
      if (initialValue.equals(f.dataType().compilerInitialValue()))
        continue;
      s.a(f.instanceName(), " = ", initialValue, ";");
      s.cr();
    }
    return content();
  }

  @Override
  protected final String generateSetters() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(4);
    for (FieldDef f : def.fields()) {
      s.br();
      DataType d = f.dataType();
      if (f.deprecated())
        s.a("@Deprecated", CR);
      s.a("public ", "Builder ", f.setterName(), "(", d.typeName(), " x)", OPEN);
      String targetExpr = f.instanceName();
      d.sourceSetter(s, f, targetExpr);
      s.a(";", CR, "return this;", CLOSE);
    }
    return content();
  }

  @Override
  protected String generateToString() {
    s.setIndent(2);
    s.a("@Override", CR, //
        "public String toString()", OPEN, //
        "return toJson().prettyPrint();", CLOSE, //
        CR);
    return content();
  }

  @Override
  protected String generateToJson() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);

    s.a("@Override", CR, //
        "public JSMap toJson()", OPEN);

    s.a(Context.pt.PKG_JSMAP, " m = new ", Context.pt.PKG_JSMAP, "();", CR);

    for (FieldDef fieldDef : def.fields())
      fieldDef.dataType().sourceSerializeToObject(s, fieldDef);

    s.a("return m;");
    s.a(CLOSE);
    return content();
  }

  @Override
  protected String generateImports(List<String> qualifiedClassNames) {
    for (String cn : qualifiedClassNames) {
      QualifiedName qn = QualifiedName.parse(cn);

      // We don't need to import anything from java.lang
      if (qn.combined().startsWith("java.lang."))
        continue;

      // We also don't need to import anything from the local package
      // Assumes the class name includes a package
      if (qn.packagePath().equals(Context.generatedTypeDef.qualifiedName().packagePath()))
        continue;

      s.a("import ", cn, ";").cr();
    }
    return content();
  }

  @Override
  protected String generateParse() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    s.a("@Override", CR, //
        "public ", def.name(), " parse(Object obj)", OPEN, //
        "return new ", def.name(), "((JSMap) obj);", CLOSE, //
        BR, //
        "private ", def.name(), "(JSMap m)", OPEN);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceDeserializeFromObject(s, f);
      s.cr();
    }
    s.a(CLOSE);
    return content();
  }

  private static Set<String> sDebugSet = hashSet();

  @Override
  protected final String generateGetters() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      s.br();
      if (f.deprecated())
        s.a("@Deprecated", CR);
      if (false && alert("tracking type names") && sDebugSet.add(f.dataType().typeName())) {
        pr("typeName:", f.dataType().typeName(), "qual:", INDENT, f.dataType().qualifiedName());
      }
      s.a("public ", f.dataType().typeName(), " ", f.getterName(), "()", OPEN, //
          "return ", f.instanceName(), ";", CLOSE);
    }
    return content();
  }

  @Override
  protected String generateImmutableToBuilder() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(6);
    for (FieldDef f : def.fields()) {
      s.a(f.instanceName(), " = ",
          f.dataType().staticToBuilder("m." + f.instanceName()), ";", CR);
    }
    return chomp(content());
  }

  @Override
  protected String generateStringConstants() {
    s.setIndent(2);
    GeneratedTypeDef def = Context.generatedTypeDef;

    for (FieldDef f : def.fields()) {
      s.a("protected static final String ", f.nameStringConstantQualified(), " = \"", f.name(), "\";");
      s.br();
    }

    return content();
  }

  @Override
  protected String generateInstanceFields() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields())
      s.a("protected ", f.dataType().typeName(), " ", f.instanceName(), ";", CR);
    s.a("protected int m__hashcode;");
    return content();
  }

  @Override
  protected String generateEquals() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    String c = def.name();
    s.setIndent(2);

    s.a("@Override").cr();
    s.a("public boolean equals(Object object)", OPEN);
    {
      s.a("if (this == object)", IN, "return true;", OUT);

      s.a("if (object == null || !(object instanceof ", c, "))").in();
      s.a("return false;").out();
      s.a(c, " other = (", c, ") object;").cr();
      s.a("if (other.hashCode() != hashCode())").in().a("return false;").out();
      for (FieldDef f : def.fields()) {
        generateEqualsForMemberField(s, f);
        s.cr();
      }
      s.a("return true;");
    }
    s.a(CLOSE);
    return content();
  }

  /**
   * Generate code to determine if two values of a DataType are equal, and if
   * not, short-circuit an equals(...) method by returning false
   */
  private void generateEqualsForMemberField(SourceBuilder s, FieldDef f) {
    String a = "" + f.instanceName();
    String b = "other." + f.instanceName();

    if (f.optional()) {
      s.a("if ((", a, " == null) ^ (", b, " == null))", IN, //
          "return false;", OUT);
      s.a("if (", a, " != null)", OPEN);
    }

    s.a("if (!(");
    f.dataType().sourceGenerateEquals(s, a, b);
    s.a("))");

    s.a(IN, "return false;", OUT);

    if (f.optional())
      s.a(CLOSE);
  }

  @Override
  protected String generateHashCode() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);

    s.a("@Override", CR, //
        "public int hashCode()", OPEN, //
        "int r = m__hashcode;", CR, //
        "if (r == 0)", OPEN, //
        "r = 1;", CR);//
    for (FieldDef f : def.fields()) {
      f.dataType().sourceIfNotNull(s, f);
      f.dataType().sourceHashCalculationCode(s, f);
      f.dataType().sourceEndIf(s).cr();
    }
    s.a("m__hashcode = r;", CLOSE, //
        "return r;", CLOSE //
    );
    return content();
  }

  @Override
  protected void generateEnumValues(EnumDataType dt) {
    s.setIndent(2);
    int i = INIT_INDEX;
    for (String label : dt.labels()) {
      i++;
      if (i > 0)
        s.a(", ");
      s.a(label);
    }
    s.a(";");
  }

  private static String sClassTemplate = Files.readString(SourceGen.class, "class_template.txt");
  private static String sEnumTemplate = Files.readString(SourceGen.class, "enum_template.txt");

}
