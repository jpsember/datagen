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
 * 
 **/
package datagen;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import datagen.datatype.EnumDataType;
import js.file.Files;
import js.json.JSMap;
import js.parsing.MacroParser;

public final class JavaSourceGen extends SourceGen {

  public void generate() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s().reset();

    JSMap m = map();
    m.put("package_decl", generatePackageDecl(def));
    // In this first pass, leave the imports macro unchanged
    m.put("imports", "[!imports]");
    m.put("class", def.name());

    String content;
    if (def.isEnum()) {
      content = sEnumTemplate;
      generateEnumValues(def);
      m.put("default_value", def.enumDataType().labels().get(0));
      m.put("enum_values", content());
    } else {
      content = sClassTemplate;

      mInset = 2;
      m.put("class_getter_implementation", generateGetters(def));
      m.put("copy_to_builder", generateImmutableToBuilder(def));
      m.put("copyfield_from_builder", generateCopyFromBuilderToImmutable(def));
      m.put("equals", generateEquals(def));
      m.put("hashcode", generateHashCode(def));
      m.put("init_instance_fields", generateInitInstanceFields(def));
      m.put("instance_fields", generateInstanceFields(def));
      m.put("parse", generateParse(def));
      m.put("setters", generateSetters(def));
      m.put("string_constants", generateStringConstants(def));
      m.put("to_json", generateToJson(def));
      m.put("to_string", generateToString(def));
    }

    // Get any source that DataTypes may have needed to add;
    // must be added here, after all other keys
    m.put("class_specific", def.getClassSpecificSource());

    // Perform pass 1 of macro substitution
    //
    {
      MacroParser parser = new MacroParser();
      parser.withTemplate(content).withMapper(m);
      content = parser.content();
    }

    // Pass 2: strip package names, add to set for import statements
    //
    content = extractImportStatements(content);

    // Pass 3: generate the import statements
    //
    m.clear();
    m.put("imports", generateImports());

    {
      MacroParser parser = new MacroParser();
      parser.withTemplate(content).withMapper(m);
      content = parser.content();
    }

    // Pass 4: Strip (or retain) optional comments
    //
    content = ParseTools.processOptionalComments(content, Context.config.comments());

    //
    // Pass 5: remove extraneous linefeeds
    //
    content = ParseTools.adjustLinefeeds(content, Context.config.language());
    File target = sourceFile();
    Context.files.mkdirs(Files.parent(target));
    boolean wrote = Context.files.writeIfChanged(target, content);
    if (wrote)
      log(".....updated:", sourceFileRelative());
    else {
      target.setLastModified(System.currentTimeMillis());
      log("...freshened:", sourceFileRelative());
    }
  }

  // ------------------------------------------------------------------
  // Source code generation for various macros
  // ------------------------------------------------------------------

  private String generatePackageDecl(GeneratedTypeDef def) {
    String pkgName = def.packageName();
    checkArgument(!pkgName.isEmpty(), "Package name is empty");
    return "package " + pkgName + ";";
  }

  private String generateInitInstanceFields(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset + 2);
    for (FieldDef f : def.fields()) {
      if (f.optional())
        continue;

      // We don't need an explicit initializer if the desired initial value equals the Java default value
      String initialValue = f.defaultValueOrNull();
      if (initialValue.equals(f.dataType().compilerInitialValue()))
        continue;
      s.a(CR, "m", f.javaName(), " = ", initialValue, ";");
    }
    s.out();
    return content();
  }

  private String generateCopyFromBuilderToImmutable(GeneratedTypeDef def) {
    s().in(mInset + 4);
    for (FieldDef f : def.fields()) {
      s().a(CR);
      f.dataType().sourceExpressionToImmutable(s(), f, "r.m" + f.javaName(), "m" + f.javaName());
      s().a(";");
    }
    s().out();
    return content();
  }

  private String generateSetters(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset + 2);
    for (FieldDef f : def.fields()) {
      s.br();
      DataType d = f.dataType();
      s.a("public ", "Builder ", setExpr(def, f), "(", d.typeName(), " x)", OPEN);
      String targetExpr = "m" + f.javaName();
      d.sourceSetter(s, f, targetExpr);
      s.a(CR, "return this;", CLOSE);
    }
    s.out();
    return content();
  }

  private String generateToString(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset);
    s.a("@Override", CR, //
        "public String toString()", OPEN, //
        "return toJson().prettyPrint();", CLOSE, //
        CR, OUT);
    return content();
  }

  private String generateToJson(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset);

    s.a("@Override", CR, //
        "public JSMap toJson()", OPEN);

    s.a(ParseTools.PKG_JSMAP, " m = new ", ParseTools.PKG_JSMAP, "();", CR);

    for (FieldDef fieldDef : def.fields())
      fieldDef.dataType().sourceSerializeToObject(s, fieldDef);

    s.a("return m;");
    s().a(CLOSE, OUT);
    return content();
  }

  private String generateImports() {
    SourceBuilder s = s();

    List<String> qualifiedClassNameStrings = arrayList();
    qualifiedClassNameStrings.addAll(getImports());
    qualifiedClassNameStrings.sort(null);

    for (String k : qualifiedClassNameStrings) {
      if (k.startsWith("java.lang."))
        continue;
      s.a("import ", k, ";").cr();
    }

    return content();
  }

  private String generateParse(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.br();
    s.in(mInset);
    s.a("@Override", CR, //
        "public ", def.name(), " parse(Object obj)", OPEN, //
        "return new ", def.name(), "((JSMap) obj);", CLOSE, //
        BR, //
        "private ", def.name(), "(JSMap m)", OPEN);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceDeserializeFromObject(s, f);
      s.cr();
    }
    s.a(CLOSE, OUT);
    return content();
  }

  private String generateGetters(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset);
    for (FieldDef f : def.fields()) {
      s.br();
      s.a("public ", f.dataType().typeName(), " ", f.javaNameLowerFirst(), "()", OPEN, //
          "return m", f.javaName(), ";", CLOSE);
    }
    s.out();
    return content();
  }

  private String generateImmutableToBuilder(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset + 4);
    for (FieldDef f : def.fields()) {
      s.a("m", f.javaName(), " = ", f.dataType().sourceExpressionToMutable("m.m" + f.javaName()), ";", CR);
    }
    s.out();
    return content();
  }

  private String generateStringConstants(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset);
    for (FieldDef f : def.fields()) {
      s.br();
      s.a("public static final String ", f.nameStringConstant(), " = \"", f.name(), "\";");
    }
    s.out();
    return content();
  }

  private String generateInstanceFields(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset);
    for (FieldDef f : def.fields())
      s.a("protected ", f.dataType().typeName(), " m", f.javaName(), ";", CR);
    s.a("protected int m__hashcode;");
    s.out();
    return content();
  }

  private String generateEquals(GeneratedTypeDef def) {
    String c = def.name();
    SourceBuilder s = s();
    s.in(mInset);
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
    s.out();
    s.a(CR);
    return content();
  }

  /**
   * Generate code to determine if two values of a DataType are equal, and if
   * not, short-circuit an equals(...) method by returning false
   */
  private void generateEqualsForMemberField(SourceBuilder s, FieldDef f) {
    String a = "m" + f.javaName();
    String b = "other.m" + f.javaName();

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

  private String generateHashCode(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset);
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
        "return r;", CLOSE, //
        OUT, CR);
    return content();
  }

  //------------------------------------------------------------------

  /**
   * Get the name of a setter
   */
  private String setExpr(GeneratedTypeDef m, FieldDef f) {
    return f.javaNameLowerFirst();
  }

  private void generateEnumValues(GeneratedTypeDef def) {
    EnumDataType dt = def.enumDataType();
    s().in();
    int i = INIT_INDEX;
    for (String label : dt.labels()) {
      i++;
      if (i > 0)
        s().a(", ");
      s().a(label);
    }
    s().a(";");
    s().out();
  }

  private static String sClassTemplate = Files.readString(SourceGen.class, "class_template.txt");
  private static String sEnumTemplate = Files.readString(SourceGen.class, "enum_template.txt");

  private int mInset;
}
