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

import java.util.List;

import datagen.datatype.EnumDataType;
import js.file.Files;
import js.json.JSMap;

public final class GoSourceGen extends SourceGen {

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
    String pkgName = def.qualifiedName().packagePath();
    checkArgument(!pkgName.isEmpty(), "Package name is empty");
    pkgName = QualifiedName.lastComponent(pkgName);
    return "package " + pkgName;
  }

  @Override
  protected final String generateCopyFromBuilderToImmutable() {
    return "!!! not required !!!";
  }

  @Override
  protected final String generateInitInstanceFields() {
    s.a("**generateInitInstanceFields");
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      if (f.optional())
        continue;

      // We don't need an explicit initializer if the desired initial value equals the Java default value
      String initialValue = f.defaultValueOrNull();
      if (initialValue.equals(f.dataType().compilerInitialValue()))
        continue;
      s.a(CR, f.instanceName(), " = ", initialValue, ";");
    }
    return content();
  }

  @Override
  protected final String generateSetters() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      if (f.deprecated())
        s.addSafe(getDeprecationSource());
      DataType d = f.dataType();
      s.a("func (v ", builderName(), ") ", f.setterName(), "(", f.instanceName(), " ", d.typeName(), ") ",
          builderName(), OPEN);
      String targetExpr = "v." + f.instanceName();
      d.sourceSetter(s, f, targetExpr);
      s.a(CR, "return v", CLOSE);
      s.br();
    }

    return content();
  }

  @Override
  protected String generateImports(List<String> expressions) {
    log("generating golang imports");

    s.setIndent(2);

    for (String cn : expressions) {
      log("... expression:", cn);
      String importString = chompPrefix(cn, "!");
      if (importString != cn) {
        // Import the (rest of the) expression verbatim
        log("...------------------------------> importing:", importString);
        s.a(importString).cr();
        continue;
      }

      QualifiedName qn = QualifiedName.parse(cn);
      log(INDENT, "QualifiedName:", INDENT, qn);

      // Don't import anything if there is no package info
      if (qn.packagePath().isEmpty()) {
        log("...package path is empty; skipping");
        continue;
      }

      // If the package name is the same as that of the file being generated, no import necessary.

      if (qn.packagePath().equals(Context.generatedTypeDef.qualifiedName().packagePath())) {
        log("...same package as the generated type; skipping");
        continue;
      }

      importString = qn.packagePath().replace('.', '/');
      log("...------------------------------> importing:", importString);
      s.a(". \"", importString, "\"").cr();
    }
    return content();
  }

  @Override
  protected final String generateGetters() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {

      if (false) {
        DataType d = def.wrappedType();
        pr("DataType qualNames:");
        pr(d.qualifiedName(0));
        pr(d.qualifiedName(1));
        pr(d.qualifiedName(2));
        halt();
      }

      if (f.deprecated())
        s.addSafe(getDeprecationSource());
      s.a("func (v *", def.wrappedType().qualifiedName(DataType.NAME_ALT).className(), ") ", f.getterName(),
          "() ", f.dataType().typeName(), " ", OPEN, //
          "return v.", f.instanceName(), CLOSE);
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
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    int max = 0;
    for (FieldDef f : def.fields()) {
      max = Math.max(max, f.instanceName().length());
    }
    for (FieldDef f : def.fields()) {
      s.a(f.instanceName(), spaces(1 + max - f.instanceName().length()), f.dataType().typeName());
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
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceSerializeToObject(s, f);
      s.cr();
    }
    return chomp(content());
  }

  @Override
  protected String generateParse() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceDeserializeFromObject(s, f);
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
    } else {
      SourceBuilder s = Context.generatedTypeDef.classSpecificSourceBuilder();
      s.a(Context.pt.PKGGO_TOOLS, Context.pt.PKGGO_JSON);
    }

    m.put("static_class", type.qualifiedName(DataType.NAME_ALT).className());
    m.put("class_init_fields_to_defaults", generateInitFieldsToDefaults());
    m.put("class_getter_declaration", generateClassGetterDeclaration());
    m.put("go_builder_getter_implementation", generateBuilderGetterImplementation());
    m.put("builder_name", type.qualifiedName(DataType.NAME_HUMAN).className() + "Builder");
    m.put("interface_name", type.qualifiedName(DataType.NAME_MAIN).className());
    m.put("default_var_name", "Default" + type.qualifiedName(DataType.NAME_HUMAN).className());
  }

  private String generateInitFieldsToDefaults() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      // We don't need an explicit initializer if the desired initial value equals Go's default value
      String defaultValue = f.defaultValueOrNull();
      String compilerInitialValue = f.dataType().compilerInitialValue();
      if (defaultValue.equals(compilerInitialValue))
        continue;
      s.a("m.", f.instanceName(), " = ", defaultValue, CR);
    }
    return trimRight(content());
  }

  private String generateClassGetterDeclaration() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      s.a(f.getterName(), "() ", f.dataType().typeName());
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

  private static String sClassTemplate = Files.readString(SourceGen.class, "class_template_go.txt");
  private static String sEnumTemplate = Files.readString(SourceGen.class, "enum_template_go.txt");

}
