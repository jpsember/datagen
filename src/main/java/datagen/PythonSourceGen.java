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

import java.io.File;
import java.util.List;

import datagen.datatype.EnumDataType;
import js.file.Files;
import js.json.JSMap;
import js.parsing.MacroParser;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

public class PythonSourceGen extends SourceGen {

  public void generate() {
    GeneratedTypeDef def = context().generatedTypeDef;
    s().reset();

    JSMap m = map();
    m.put("package_decl", generatePackageDecl(def));
    // In this first pass, leave the imports macro unchanged so we can replace it after this first pass
    m.put("imports", "[!imports]");
    m.put("class", def.name());

    String content;
    if (def.isEnum()) {
      content = sEnumTemplate;
      generateEnumValues(def);
      m.put("enum_values", content());
      m.put("default_value", def.enumDataType().labels().get(0));
    } else {
      content = sClassTemplate;

      mInset = 2;
      m.put("string_constants", generateStringConstants(def));
      m.put("init_instance_fields", generateInitInstanceFields(def));
      m.put("parse", generateParse(def));
      m.put("class_getter_implementation", generateGetters(def));
      m.put("copy_to_builder", generateImmutableToBuilder(def));
      m.put("to_dict", generateToDict(def));
      m.put("hashcode", generateHashCode(def));
      m.put("equals", generateEquals(def));
      m.put("setters", generateSetters(def));
      m.put("copyfield_from_builder", generateCopyFromBuilderToImmutable(def));

      // Get any source that DataTypes may have needed to add
      m.put("class_specific", def.getClassSpecificSource());

    }

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
    content = ParseTools.processOptionalComments(content, config().comments());

    //
    // Pass 5: remove extraneous linefeeds
    //
    content = ParseTools.adjustLinefeeds(content, config().language());

    File target = sourceFile();
    context().files.mkdirs(Files.parent(target));
    boolean wrote = context().files.writeIfChanged(target, content);
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
    return "";
  }

  private String generateStringConstants(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset);
    for (FieldDef f : def.fields()) {
      s.br();
      s.a(f.nameStringConstant(false), " = ", quote(f.name()));
    }
    s.out();
    return content();
  }

  private String generateInitInstanceFields(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset + 2);
    for (FieldDef f : def.fields()) {
      s.a(CR, "self._", f.javaName(), " = ", f.defaultValueOrNull());
    }
    s.out();
    return content();
  }

  private String generateCopyFromBuilderToImmutable(GeneratedTypeDef def) {
    s().in(mInset + 2);
    for (FieldDef f : def.fields()) {
      s().cr();
      f.dataType().sourceExpressionToImmutable(s(), f, "v._" + f.javaName(), "self._" + f.javaName());
    }
    s().out();
    return content();
  }

  private String generateSetters(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset);
    for (FieldDef f : def.fields()) {
      s.a("\\\\", CR);
      DataType d = f.dataType();
      s.a("def set_", setExpr(def, f), "(self, x):", OPEN);
      String targetExpr = "self._" + f.javaName();
      d.sourceSetter(s, f, targetExpr);
      s.a(CR, "return self", CLOSE);
    }
    s.out();
    return content();
  }

  private String generateToDict(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset + 2);

    s.a("m = {}", CR);
    for (FieldDef fieldDef : def.fields())
      fieldDef.dataType().sourceSerializeToObject(s, fieldDef);
    s.a("return m");
    s.a(CLOSE);
    return content();
  }

  private String generateImports() {
    SourceBuilder s = s();
    List<String> importStatements = arrayList();
    importStatements.addAll(context().getImports());
    importStatements.sort(null);
    for (String k : importStatements) {
      s.a(k).cr();
    }
    return content();
  }

  private String generateParse(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.br();
    s.in(mInset + 2);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceDeserializeFromObject(s, f);
      s.cr();
    }
    s.a(OUT);
    return content();
  }

  private String generateGetters(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset);
    for (FieldDef f : def.fields()) {
      s.a("\\\\").cr();
      s.a("def ", f.javaName(), "(self):", OPEN, //
          "return self._", f.javaName(), CLOSE);
    }
    s.out();
    return content();
  }

  private String generateImmutableToBuilder(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset + 2);
    s.a("x = ", def.name(), "Builder()", CR);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceIfNotNull(s, f);
      s.a("x._", f.javaName(), " = ", f.dataType().sourceExpressionToMutable("self._" + f.javaName()), CR);
      f.dataType().sourceEndIf(s).cr();
    }
    s.a("return x", CR);
    s.out();
    return content();
  }

  private String generateEquals(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset + 4);
    s.a("return hash(self) == hash(other)");
    if (!def.fields().isEmpty()) {
      boolean first = true;
      for (FieldDef f : def.fields()) {
        s.a("\\");
        if (first) {
          s.a(IN);
          first = false;
        } else
          s.a(CR);
        s.a("and self._", f.javaName(), " == other._", f.javaName());
      }
      if (!first)
        s.out();
    }
    s.out();
    return content();
  }

  private String generateHashCode(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.in(mInset + 4);
    s.a("r = 1", CR);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceIfNotNull(s, f);
      f.dataType().sourceHashCalculationCode(s, f);
      f.dataType().sourceEndIf(s).cr();
    }
    s.a("self._hash_value = r").out();
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
    for (String label : dt.labels()) {
      s().a(label, " = ", quote(label.toLowerCase())).cr();
    }
    s().out();
  }

  private static String sClassTemplate = Files.readString(SourceGen.class, "class_template_py.txt");
  private static String sEnumTemplate = Files.readString(SourceGen.class, "enum_template_py.txt");

  private int mInset;
}
