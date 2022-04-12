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
import js.data.DataUtil;
import js.file.Files;
import js.json.JSMap;
import js.parsing.MacroParser;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

public class PythonSourceGen extends SourceGen {

  @Override
  protected String getTemplate() {
    if (Context.generatedTypeDef.isEnum())
      return sEnumTemplate;
    else
      return sClassTemplate;
  }

  public void oldgenerate() {
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
      generateEnumValues(def.enumDataType());
      m.put("default_value", def.enumDataType().labels().get(0));
      m.put("enum_values", content());
    } else {
      content = sClassTemplate;
      setInset(2);
      m.put("class_getter_implementation", generateGetters(def));
      m.put("copy_to_builder", generateImmutableToBuilder(def));
      m.put("copyfield_from_builder", generateCopyFromBuilderToImmutable(def));
      m.put("equals", generateEquals(def));
      m.put("hashcode", generateHashCode(def));
      m.put("init_instance_fields", generateInitInstanceFields(def));
      m.put("parse", generateParse(def));
      m.put("setters", generateSetters(def));
      m.put("string_constants", generateStringConstants(def));
      m.put("to_json", generateToJson(def));
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
    Files  files = Context.files;
    boolean wrote = files.writeIfChanged(target, content);
    if (wrote)
      log(".....updated:", sourceFileRelative());
    else {
      target.setLastModified(System.currentTimeMillis());
      log("...freshened:", sourceFileRelative());
    }

//    // This is annoying, but to make relative imports work in Python we need to ensure
//    // there's an (empty) file '__init__.py' in the same directory as any Python file.
//    //
//    if (!files.dryRun()) {
//      File sentinelFile = new File(parent, "__init__.py");
//      if (!sentinelFile.exists())
//        files.write(DataUtil.EMPTY_BYTE_ARRAY, sentinelFile);
//    }
  }

  // ------------------------------------------------------------------
  // Source code generation for various macros
  // ------------------------------------------------------------------

  @Override
  protected String generatePackageDecl(GeneratedTypeDef def) {
    return "";
  }

  @Override
  protected String generateStringConstants(GeneratedTypeDef def) {
    SourceBuilder s = s();
    inset(0);
    for (FieldDef f : def.fields()) {
      s.br();
      s.a(f.nameStringConstant(false), " = ", quote(f.name()));
    }
    s.out();
    return content();
  }

  @Override
  protected String generateInitInstanceFields(GeneratedTypeDef def) {
    SourceBuilder s = s();
    inset(2);
    for (FieldDef f : def.fields()) {
      s.a(CR, "self._", f.javaName(), " = ", f.defaultValueOrNull());
    }
    s.out();
    return content();
  }

@Override protected String generateCopyFromBuilderToImmutable(GeneratedTypeDef def) {
    inset(2);
    for (FieldDef f : def.fields()) {
      s().cr();
      f.dataType().sourceExpressionToImmutable(s(), f, "v._" + f.javaName(), "self._" + f.javaName());
    }
    s().out();
    return content();
  }

  @Override
  protected String generateSetters(GeneratedTypeDef def) {
    SourceBuilder s = s();
    inset(0);
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

  @Override
  protected String generateToJson(GeneratedTypeDef def) {
    SourceBuilder s = s();
    inset(2);

    s.a("m = {}", CR);
    for (FieldDef fieldDef : def.fields())
      fieldDef.dataType().sourceSerializeToObject(s, fieldDef);
    s.a("return m");
    s.a(CLOSE);
    return content();
  }

  @Override
  protected String generateImports() {
    SourceBuilder s = s();
    List<String> importStatements = arrayList();
    importStatements.addAll(getImports());
    importStatements.sort(null);
    for (String k : importStatements) {
      s.a(k).cr();
    }
    return content();
  }

  @Override
  protected String generateParse(GeneratedTypeDef def) {
    SourceBuilder s = s();
    s.br();
    inset(2);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceDeserializeFromObject(s, f);
      s.cr();
    }
    s.a(OUT);
    return content();
  }

  @Override
  protected String generateGetters(GeneratedTypeDef def) {
    SourceBuilder s = s();
    inset(0);
    for (FieldDef f : def.fields()) {
      s.a("\\\\").cr();
      s.a("def ", f.javaName(), "(self):", OPEN, //
          "return self._", f.javaName(), CLOSE);
    }
    s.out();
    return content();
  }

  @Override
  protected String generateImmutableToBuilder(GeneratedTypeDef def) {
    SourceBuilder s = s();
    inset(2);
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

  @Override
  protected String generateEquals(GeneratedTypeDef def) {
    SourceBuilder s = s();
    inset(4);
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

  @Override
  protected String generateHashCode(GeneratedTypeDef def) {
    SourceBuilder s = s();
    inset(4);
    s.a("r = 1", CR);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceIfNotNull(s, f);
      f.dataType().sourceHashCalculationCode(s, f);
      f.dataType().sourceEndIf(s).cr();
    }
    s.a("self._hash_value = r").out();
    return content();
  }

//  @Override
//  protected String generateEqualsForMemberField(SourceBuilder s, FieldDef f) {
//    return "<NOT SUPPORTED>";
//  }

  @Override
  protected String generateInstanceFields(GeneratedTypeDef def) {
    return "<NOT SUPPORTED>";
  }

  @Override
  protected String generateToString(GeneratedTypeDef def) {
    return "<NOT SUPPORTED>";
  }
  @Override
  protected void postGenerate() {
    Files files = Context.files;
    // This is annoying, but to make relative imports work in Python we need to ensure
    // there's an (empty) file '__init__.py' in the same directory as any Python file.
    //
    if (!files.dryRun()) {
      File parent =   Files.parent(sourceFile());
      File sentinelFile = new File(parent, "__init__.py");
      if (!sentinelFile.exists())
        files.write(DataUtil.EMPTY_BYTE_ARRAY, sentinelFile);
    }
 
  }
  //------------------------------------------------------------------

  /**
   * Get the name of a setter
   */
  private String setExpr(GeneratedTypeDef m, FieldDef f) {
    return f.javaNameLowerFirst();
  }

  @Override
  protected void generateEnumValues(EnumDataType dt) {
    s().in();
    for (String label : dt.labels()) {
      s().a(label, " = ", quote(label.toLowerCase())).cr();
    }
    s().out();
  }

  private static String sClassTemplate = Files.readString(SourceGen.class, "class_template_py.txt");
  private static String sEnumTemplate = Files.readString(SourceGen.class, "enum_template_py.txt");

}
