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
    String pkgName = def.packageName();
    checkArgument(!pkgName.isEmpty(), "Package name is empty");
    return "package " + pkgName;
  }

  @Override
  protected final String generateCopyFromBuilderToImmutable() {
    s.setIndent(2);
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      s.cr();
      String targetExpression = "b." + f.instanceName();
      String valueExpression = "s." + f.instanceName();
      s.a(targetExpression, " = ", valueExpression);
    }
    return content();
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
      DataType d = f.dataType();
      s.a("func (v *", def.name(), "Builder) ", f.setterName(), "(", f.instanceName(), " ", d.typeName(),
          ") *", def.name(), "Builder", OPEN);
      String targetExpr = "v.m." + f.instanceName();
      d.sourceSetter(s, f, targetExpr);
      s.a(CR, "return v", CLOSE);
      s.br();
    }

    return content();
  }

  @Override
  protected String generateImports(List<String> qualifiedClassNames) {
    s.setIndent(2);
    s.a(". \"js/base\"").cr();
    //s.comment("Add the fancy qualified stuff to automatically determine which imports are necessary");
    s.a(". \"js/json\"").cr();

    for (String cn : qualifiedClassNames) {
      // We also don't need to import anything from the local package
      // Assumes the class name includes a package
      String packageName = cn.substring(0, cn.lastIndexOf('.'));
      if (packageName.equals(Context.generatedTypeDef.packageName()))
        continue;

      s.a("import ", cn, ";").cr();
    }
    return content();
  }

  @Override
  protected final String generateGetters() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      s.a("func (v *", def.name(), ") ", f.getterName(), "() ", f.dataType().typeName(), " ", OPEN, //
          "return v.", f.instanceName(), CLOSE);
      s.br();
    }
    return content();
  }

  @Override
  protected final String generateImmutableToBuilder() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      String expr = "v." + f.instanceName();
      if (Context.debugMode()) {
        f.dataType().sourceExpressionToImmutable(s, f, "d." + f.instanceName(), expr);
      } else {
        s.a("d." + f.instanceName(), " = ", expr);
      }
      s.a(CR);
    }
    return chomp(content());
  }

  @Override
  protected String generateStringConstants() {
    s.a("**GenerateStringConstants");
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      s.br();
      s.a("protected static final String ", f.nameStringConstantQualified(), " = \"", f.name(), "\";");
    }
    return content();
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
    s.a("**GenerateEnumValues");
    s.in();
    int i = INIT_INDEX;
    for (String label : dt.labels()) {
      i++;
      if (i > 0)
        s.a(", ");
      s.a(label);
    }
    s.a(";");
    s.out();
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
    m.put("class_init_fields_to_defaults", generateInitFieldsToDefaults());
    m.put("class_getter_declaration", generateClassGetterDeclaration());
    m.put("go_builder_getter_implementation", generateBuilderGetterImplementation());
    m.put("builder_name", Context.generatedTypeDef.name() + "Builder");
    m.put("interface_name", "I" + Context.generatedTypeDef.name());
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
      s.a("func (v *", def.name(), "Builder) ", f.getterName(), "() ", f.dataType().typeName(), " ", OPEN, //
          "return v.m.", f.instanceName(), CLOSE);
      s.br();
    }
    return content();
  }

  private static String sClassTemplate = Files.readString(SourceGen.class, "class_template_go.txt");
  private static String sEnumTemplate = Files.readString(SourceGen.class, "enum_template_go.txt");

}
