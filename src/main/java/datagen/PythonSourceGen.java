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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import datagen.datatype.EnumDataType;
import js.file.Files;
import js.json.JSMap;
import js.parsing.RegExp;

import static js.base.Tools.*;
import static datagen.SourceBuilder.*;
import static datagen.Context.*;

public class PythonSourceGen extends SourceGen {

  private static final String DEPRECATED_STR = "@{{pycore.base.deprecated|deprecated}}";

  @Override
  protected void addAdditionalTemplateValues(JSMap templateMap) {
    var val = "";
    if (generatedTypeDef.isDeprecated()) {
      val = "@deprecated(\"This class is deprecated\")\n  ";
    }
    templateMap.put("py_depr", val);
  }

  @Override
  protected String getTemplate() {
    if (Context.generatedTypeDef.isEnum())
      return sEnumTemplate;
    else
      return sClassTemplate;
  }

  @Override
  protected void generateEnumValues(EnumDataType dt) {
    s.setIndent(2);
    for (String label : dt.labels()) {
      s.a(label, " = ", quote(label.toLowerCase())).cr();
    }
  }

  @Override
  protected String generateStringConstants() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      s.a(f.nameStringConstantUnqualified(), " = ", quote(f.name()));
      s.br();
    }
    return content();
  }

  @Override
  protected final String generateInitInstanceFields() {
    s.setIndent(4);
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.a("self.", hashFieldName(), " = None", CR);
    for (FieldDef f : def.fields()) {
      s.a("self.", f.instanceName(), " = ", f.defaultValueSource(), CR);
    }
    return content();
  }

  @Override
  protected String generateCopyFromBuilderToImmutable() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(4);
    for (FieldDef f : def.fields()) {
      s.cr();
      s.a("v.", f.instanceName(), " = self.", f.instanceName());
    }
    return content();
  }

  @Override
  protected String generateSetters() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);
    for (FieldDef f : def.fields()) {
      s.a("\\\\", CR);
      DataType d = f.dataType();
      if (f.deprecated())
        s.a(DEPRECATED_STR + "(\"field ", f.name(), " is deprecated\")", CR);
      s.a("def ", f.setterName(), "(self, x: ", f.dataType().typeName(), ") -> ", def.name(), "Builder:",
          OPEN);
      String targetExpr = "self." + f.instanceName();
      d.sourceSetter(s, f, targetExpr);
      s.a(CR, "return self", CLOSE);
    }

    s.a("\\\\", CR);
    for (FieldDef f : def.fields())
      s.a(f.name(), " = property(", def.name(), ".", propertyGetName(f), ", ", f.setterName(), ")", CR);

    return content();
  }

  @Override
  protected String generateToJson() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(4);
    s.a("m = {}", CR);
    for (FieldDef fieldDef : def.fields())
      fieldDef.dataType().sourceSerializeToObject(s, fieldDef);
    s.a("return m");
    return content();
  }

  @Override
  protected String generateImports(List<String> qualifiedClassNames) {
    // 
    // Python package names differ from Java ones.
    //
    // Java classes have this structure:
    //
    //
    //  File path:         <path> . <filename> 
    //  Import statement:  <package> . <ClassName>
    //
    // Python classes have this:
    //
    //  File path:        <path> . <filename>
    //  Import statement: <package> . <filename> . <ClassName>
    //
    // We refer to external types within .dat files in the same way as Java import statements:
    //
    // External ref:      <package> . <ClassName>
    //
    // With the special rule that if no package is specified, it assumes the same package as the current
    // datatype being generated.   *** Maybe not (issue #45) ***
    //
    // When referring to such classes within generated Python code, we will derive and insert a filename
    // if the package includes 'gen'.
    //
    for (String qualName : qualifiedClassNames) {
      int i = qualName.lastIndexOf('.');
      if (i < 0)
        continue;
      String packageName = qualName.substring(0, i);
      String className = qualName.substring(i + 1);
      QualifiedName self = Context.generatedTypeDef.wrappedType().qualifiedName();
      if (self.combined().equals(qualName))
        continue;
      s.a("from ", packageName, " import ", className).cr();
    }
    return content();
  }

  @Override
  protected String generateParse() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(4);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceDeserializeFromObject(s, f);
      s.cr();
    }

    return content();
  }

  @Override
  protected String generateGetters() {

    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(2);

    for (FieldDef f : def.fields()) {
      s.a("\\\\").cr();
      if (f.deprecated())
        s.a(DEPRECATED_STR + "(\"field ", f.name(), " is deprecated\")", CR);
      s.a("def ", propertyGetName(f), "(self) -> ", f.dataType().typeName(), ":", OPEN, //
          "return self.", f.instanceName(), CLOSE);
    }

    s.a("\\\\").cr();
    for (FieldDef f : def.fields())
      s.a(f.name(), " = property(", propertyGetName(f), ")", CR);

    return content();
  }

  @Override
  protected String generateImmutableToBuilder() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(4);
    s.a("x = ", def.name(), "Builder()", CR);
    for (FieldDef f : def.fields()) {
      s.a("x.", f.instanceName(), " = ", "self." + f.instanceName(), CR);
    }
    s.a("return x", CR);
    return content();
  }

  @Override
  protected String generateEquals() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(6);
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
        s.a("and self.", f.instanceName(), " == other.", f.instanceName());
      }
      if (!first)
        s.out();
    }
    return content();
  }

  private static final Pattern PATTERN_HASH_INITIAL_VALUE = RegExp.pattern("r = 1\\s*r = r \\* 37 \\+\\s*");

  @Override
  protected String generateHashCode() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s.setIndent(4);
    String hashVarName = "self." + hashFieldName();
    s.a("if ", hashVarName, " is None:", IN);
    s.a("r = 1", CR);
    for (int pass = 0; pass < 2; pass++) {
      for (FieldDef f : def.fields()) {
        if (pass == 1)
          continue;
        f.dataType().sourceHashCalculationCode(s, f);
        s.cr();
      }
    }
    s.a(hashVarName, " = r").out();
    s.a("return ", hashVarName);

    String c = content();
    Matcher m = PATTERN_HASH_INITIAL_VALUE.matcher(c);
    if (m.find())
      c = c.substring(0, m.start()) + "r = " + c.substring(m.end());
    return c;
  }


  private String propertyGetName(FieldDef f) {
    return verboseVariant("_g" + f.index(), "_get_" + f.name());
  }

  private String hashFieldName() {
    return verboseVariant("_h", "_hash_value");
  }

  private static String sClassTemplate = Files.readString(SourceGen.class, "class_template_py.txt");
  private static String sEnumTemplate = Files.readString(SourceGen.class, "enum_template_py.txt");

}
