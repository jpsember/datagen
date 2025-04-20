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
import static datagen.Utils.*;

import java.util.List;
import java.util.Set;

import datagen.datatype.EnumDataType;
import js.data.DataUtil;
import js.file.Files;
import js.json.JSMap;

public final class RustSourceGen extends SourceGen {

  @Override
  protected String getTemplate() {
    if (Context.generatedTypeDef.isEnum())
      return sEnumTemplate;
    else
      return sClassTemplate;
  }

  @Override
  protected String generatePackageDecl() {
    return "";
  }

  @Override
  protected final String generateCopyFromBuilderToImmutable() {
    return "!!! not required !!!";
  }

  @Override
  protected final String generateInitInstanceFields() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      if (f.optional())
        continue;
      String initialValue = 
          f.dataType().getInitInstanceFieldExpr(f);
//          f.defaultValueOrNull();
      s.a(CR, f.instanceName(), " = ", initialValue, ";");
    }
    return content();
  }

  @Override
  protected final String generateSetters() {
    s.setIndent(2);
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      if (f.deprecated())
        s.addSafe(getDeprecationSource());
      DataType d = f.dataType();
      var argName = f.instanceName();

      s.a("pub fn ", f.setterName(), "(& mut self, ", argName, ": ", d.setterArgSignature(argName),
          ") -> &Self", OPEN);
      String targetExpr = "self." + f.instanceName();
      d.sourceSetter(s, f, targetExpr);
      s.a(CR, "self", CLOSE);
      s.br();
    }
    return content();
  }

  @Override
  protected String generateImports(List<String> expressions) {

    // 
    // Like Python, Rust package names differ from Java ones.
    //
    // Java classes have this structure:
    //
    //  File path:         <path> . <filename> 
    //  Import statement:  <package> . <ClassName>
    //
    // Rust classes have this  (we will replace '.' with '::'):
    //
    //  File path:        <path> . <filename>
    //  Import statement: <package> . <filename> . <ClassName>
    //    or what is more useful, 
    //                     <package> . <filename> . *
    //
    // We refer to external types within .dat files in the same way as Java import statements:
    //
    // External ref:      <package> . <ClassName>
    //

    boolean db = DEBUG_RUST_IMPORTS && alert("logging");
    if (db)
      log("generating Rust imports");

    Set<String> uniqueSet = hashSet();

    for (String cn : expressions) {

      if (db)
        log(VERT_SP, "... expression:", cn);

      String importString = cn;
      QualifiedName qn = QualifiedName.parse(cn);
      if (db)
        log(INDENT, "QualifiedName:", INDENT, qn);

      // If the package path is empty, don't import anything
      if (qn.packagePath().isEmpty()) {
        if (db)
          log("...package path is empty; skipping");
        continue;
      }

      // If the combined path equals that of the file being generated, don't import anything
      {
        var current = Context.generatedTypeDef.qualifiedName();
        if (db)
          log("...name for file being generated:", INDENT, current);
        if (current.combined().equals(qn.combined())) {
          if (db)
            log("...same; skipping");
          continue;
        }
      }
      // If one of the package directories is 'gen', we apply our special rules
      if (("." + qn.packagePath() + ".").contains(".gen.")) {

        // Extend the package path to include a snake case form of the class name
        // e.g. gen.Saturn => gen.saturn.Saturn.
        // 
        // Don't do this if the class name is '*'
        //
        if (!qn.className().equals("*")) {
          var toUnder = DataUtil.convertCamelCaseToUnderscores(qn.className());
          qn = qn.withPackageName(qn.packagePath() + "." + toUnder);
          // Replace the class name with '*'
          qn = qn.withClassName("*");
          if (db)
            pr("modified for Rust:", qn);
        }
      }
      importString = qn.combined().replace(".", "::");
      if (uniqueSet.add(importString)) {
        if (db)
          log("...------------------------------> importing:", importString);
        s.a("use ", importString, ";").cr();
      }
    }
    if (DEBUG_RUST_IMPORTS)
      halt("returning content:", INDENT, content());
    return content();
  }

  @Override
  protected final String generateGetters() {
    s.setIndent(2);
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      if (f.deprecated())
        s.addSafe(getDeprecationSource());
      s.a("fn ", f.getterName(), "(&self) -> ", f.dataType().getterReturnTypeExpr(), " ", OPEN, //
          // ampForRef(f), f.dataType().typeName(), " ", OPEN, //
          "  ");
      f.dataType().getterBody(s, f);
      s.a(CLOSE);
      s.br();
    }
    return content();
  }

  @Override
  protected final String generateImmutableToBuilder() {
    return "!!! not required !!!";
  }

  @Override
  protected String generateStringConstants() {
    return NOT_SUPPORTED;
  }

  @Override
  protected String generateInstanceFields() {
    todo("I think we need convenience fn, 'new_builder'");
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    int max = 0;
    for (FieldDef f : def.fields()) {
      max = Math.max(max, f.instanceName().length());
    }
    for (FieldDef f : def.fields()) {
      s.a(f.instanceName(), ":", spaces(1 + max - f.instanceName().length()), f.dataType().typeName(), ",");
      s.a(CR);
    }
    return content();
  }

  @Override
  protected void generateEnumValues(EnumDataType dt) {
    s.in();
    int i = INIT_INDEX;
    for (String label : dt.labels()) {
      s.cr();
      i++;
      s.a(label);
      if (i == 0) {
        s.a(" ", Context.generatedTypeDef.qualifiedName().className(), " = iota");
      }
    }
    s.outNoCr();
  }

  @Override
  protected String generateToJson() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(4);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceSerializeToObject(s, f);
      s.cr();
    }
    return chomp(content());
  }

  @Override
  protected String generateParse() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(4);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceDeserializeFromObject(s, f);
      s.addSemiIfNec();
      s.cr();
    }
    return chomp(content());
  }

  @Override
  protected String generateEquals() {
    return "";
  }

  @Override
  protected String generateHashCode() {
    return "";
  }

  @Override
  protected void addAdditionalTemplateValues(JSMap m) {

    DataType type = Context.generatedTypeDef.wrappedType();

    if (Context.generatedTypeDef.isEnum()) {
      m.put("enum_specific", generateEnumSpecific());
    }

    m.put("static_class", type.qualifiedName(DataType.NAME_ALT).className());
    m.put("class_init_fields_to_defaults", generateInitFieldsToDefaults());
    m.put("class_getter_declaration", generateClassGetterDeclaration());
    m.put("go_builder_getter_implementation", generateBuilderGetterImplementation());
    m.put("builder_name", type.qualifiedName(DataType.NAME_HUMAN).className() + "Builder");
    m.put("interface_name", type.qualifiedName(DataType.NAME_MAIN).className());
    m.put("default_var_name", "default_" + type.qualifiedName(DataType.NAME_HUMAN).className());
  }

  private String generateInitFieldsToDefaults() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(4);
    for (FieldDef f : def.fields()) {
      String defaultValue = f.dataType().getInitInstanceFieldExpr(f);
      s.a(f.instanceName(), " : ", defaultValue, ",", CR);
    }
    return trimRight(content());
  }

  private String generateClassGetterDeclaration() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      s.a("fn ", f.getterName(), "(&self) -> ", f.dataType().getterReturnTypeExpr(), ";");
      s.a(CR);
    }
    return trimRight(content());
  }

  private String generateBuilderGetterImplementation() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      s.a("func (v ", builderName(), ") ", f.getterName(), "() ", f.dataType().typeName(), " ", OPEN, //
          "return v.", f.instanceName(), CLOSE);
      s.br();
    }
    return content();
  }

  private String builderName() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    return def.wrappedType().qualifiedName(DataType.NAME_HUMAN).className() + "Builder";
  }

  private String generateEnumSpecific() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    EnumDataType enumType = def.enumDataType();

    // Add some imports
    s.a(Context.pt.PKGGO_TOOLS, Context.pt.PKGGO_JSON, Context.pt.PKGGO_DATA);

    s.a("var ", def.name(), "EnumInfo = NewEnumInfo(\"", //);
        convertCamelToUnderscore(String.join(" ", enumType.labels())), //
        "\")", CR);
    s.br();
    s.a("func (x ", def.name(), ") String() string ", OPEN, //
        "return ", def.name(), "EnumInfo", ".EnumNames[x]", CLOSE, CR //
    );

    s.br();

    s.a("func (x ", def.name(), ") ParseFrom(m JSMap, key string) ", def.name(), " ", OPEN, //
        "return ", def.name(), "(ParseEnumFromMap(", def.name(), "EnumInfo, m, key, int(Default", def.name(),
        ")))", CLOSE //
    );
    return content();
  }

  @Override
  protected String getDeprecationSource() {
    return "// Deprecated\n";
  }

  private static String sClassTemplate = Files.readString(SourceGen.class, "class_template_rust.txt");
  private static String sEnumTemplate = Files.readString(SourceGen.class, "enum_template_rust.txt");

}
