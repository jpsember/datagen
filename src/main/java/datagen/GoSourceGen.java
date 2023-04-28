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
import js.data.DataUtil;
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
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      s.a(CR);
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
    s.in(2);
    for (FieldDef f : def.fields()) {
      if (f.optional())
        continue;

      // We don't need an explicit initializer if the desired initial value equals the Java default value
      String initialValue = f.defaultValueOrNull();
      if (initialValue.equals(f.dataType().compilerInitialValue()))
        continue;
      s.a(CR, f.instanceName(), " = ", initialValue, ";");
    }
    s.out();
    return content();
  }

  @Override
  protected final String generateSetters() {

    GeneratedTypeDef def = Context.generatedTypeDef;
    //s.in(2);
    for (FieldDef f : def.fields()) {
      s.br();
      DataType d = f.dataType();

      s.a("func (v *", def.name(), "Builder) ", f.setterName(), "(", f.instanceName(), " ", d.typeName(),
          ") *", def.name(), "Builder", OPEN);
      String targetExpr = "v." + f.instanceName();
      d.sourceSetter(s, f, targetExpr);
      s.a(CR, "return v", CLOSE);
    }
    //s.out();
    return content();
  }

  //  @Override
  //  protected String generateToString() {
  //    s.a("**generateSetters");
  //      s.in(0);
  //    s.a("@Override", CR, //
  //        "public String toString()", OPEN, //
  //        "return toJson().prettyPrint();", CLOSE, //
  //        CR, OUT);
  //    return content();
  //  }

  //  @Override
  //  protected String generateToJson() {
  //    GeneratedTypeDef def = Context.generatedTypeDef;
  //    s.in(0);
  //
  //    s.a("@Override", CR, //
  //        "public JSMap toJson()", OPEN);
  //
  //    s.a(ParseTools.PKG_JSMAP, " m = new ", ParseTools.PKG_JSMAP, "();", CR);
  //
  //    for (FieldDef fieldDef : def.fields())
  //      fieldDef.dataType().sourceSerializeToObject(s, fieldDef);
  //
  //    s.a("return m;");
  //    s.a(CLOSE, OUT);
  //    return content();
  //  }

  @Override
  protected String generateImports(List<String> qualifiedClassNames) {
    //s.a("**generateImports");
    s.in(2);
    s.a(". \"js/base\"").cr();
    s.a(". \"js/json\"").cr();

    for (String cn : qualifiedClassNames) {
      // We also don't need to import anything from the local package
      // Assumes the class name includes a package
      String packageName = cn.substring(0, cn.lastIndexOf('.'));
      if (packageName.equals(Context.generatedTypeDef.packageName()))
        continue;

      s.a("import ", cn, ";").cr();
    }
    s.out();
    return content();
  }

  //  @Override
  //  protected String generateParse() {
  //    GeneratedTypeDef def = Context.generatedTypeDef;
  //    s.br();
  //    s.in(0);
  //    s.a("@Override", CR, //
  //        "public ", def.name(), " parse(Object obj)", OPEN, //
  //        "return new ", def.name(), "((JSMap) obj);", CLOSE, //
  //        BR, //
  //        "private ", def.name(), "(JSMap m)", OPEN);
  //    for (FieldDef f : def.fields()) {
  //      f.dataType().sourceDeserializeFromObject(s, f);
  //      s.cr();
  //    }
  //    s.a(CLOSE, OUT);
  //    return content();
  //  }

  @Override
  protected final String generateGetters() {
    // func (v *Crow) Name() string {
    //   return v.name
    // }
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      s.br();
      s.a("func (v *", def.name(), ") ", f.getterName(), "() ", f.dataType().typeName(), " ", OPEN, //
          "return v.", f.instanceName(), CLOSE);
    }
    return content();
  }

  @Override
  protected final String generateImmutableToBuilder() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.in(0);
    for (FieldDef f : def.fields()) {
      // d.rage = v.rage
      String expr = "v." + f.instanceName();
      if (Context.debugMode()) {
        f.dataType().sourceExpressionToImmutable(s, f, "d." + f.instanceName(), expr);
        s.a(CR);
      } else {
        s.a("d." + f.instanceName(), " = ", expr, CR);
      }
    }
    s.out();
    return content();
  }

  @Override
  protected String generateStringConstants() {
    s.a("**GenerateStringConstants");
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.in(0);
    for (FieldDef f : def.fields()) {
      s.br();
      s.a("protected static final String ", f.nameStringConstantQualified(), " = \"", f.name(), "\";");
    }
    s.out();
    return content();
  }

  @Override
  protected String generateInstanceFields() {
    todo(
        "fields are generated in strange order for go; perhaps go sourcegen should override methods to enforce order?");
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.in();
    int i = INIT_INDEX;
    for (FieldDef f : def.fields()) {
      i++;
      if (i != 0)
        s.a(CR);
      s.a(f.instanceName(), " ", f.dataType().typeName());
    }
    s.out();
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
    int i = INIT_INDEX;
    for (FieldDef f : def.fields()) {
      i++;
      if (i != 0)
        s.a(CR);
      s.a("m.Put(\"", f.instanceName(), "\", v.", f.instanceName(), ")  // need type-specific PutXXX calls!");
    }
    return content();
  }

  @Override
  protected String generateParse() {
    //  n.name = s.GetString("name")
    //  n.rage = s.GetInt32("rage")
    GeneratedTypeDef def = Context.generatedTypeDef;
    int i = INIT_INDEX;
    for (FieldDef f : def.fields()) {
      i++;
      if (i != 0)
        s.a(CR);
      s.a("n.", f.instanceName(), " = s.Get", DataUtil.capitalizeFirst(f.dataType().typeName()), "(\"",
          f.instanceName(), "\")");
    }
    return content();
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
    m.put("class_getter_declaration", generateClassGetterDeclaration());
    //    m.put("go_builder_getter_declaration", generateBuilderGetterDeclaration());
    m.put("go_builder_getter_implementation", generateBuilderGetterImplementation());
  }

  private String generateClassGetterDeclaration() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      s.a(CR);
      s.a(f.getterName(), "() ", f.dataType().typeName());
    }
    return content();
  }

  private String generateBuilderGetterImplementation() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    for (FieldDef f : def.fields()) {
      s.br();
      s.a("func (v *", def.name(), "Builder) ", f.getterName(), "() ", f.dataType().typeName(), " ", OPEN, //
          "return v.m.", f.instanceName(), CLOSE);
    }
    return content();
  }

  private static String sClassTemplate = Files.readString(SourceGen.class, "class_template_go.txt");
  private static String sEnumTemplate = Files.readString(SourceGen.class, "enum_template_go.txt");

}
