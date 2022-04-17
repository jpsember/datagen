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
import datagen.gen.QualifiedName;
import js.data.DataUtil;
import js.file.Files;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;
//import static datagen.Utils.*;

public class PythonSourceGen extends SourceGen {

  @Override
  protected String getTemplate() {
    if (Context.generatedTypeDef.isEnum())
      return sEnumTemplate;
    else
      return sClassTemplate;
  }

  @Override
  protected void generateEnumValues(EnumDataType dt) {
    s().in();
    for (String label : dt.labels()) {
      s().a(label, " = ", quote(label.toLowerCase())).cr();
    }
    s().out();
  }

  @Override
  protected String generateStringConstants() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    SourceBuilder s = s().in(0);
    for (FieldDef f : def.fields()) {
      s.br();
      s.a(f.nameStringConstantUnqualified(), " = ", quote(f.name()));
    }
    s.out();
    return content();
  }

  @Override
  protected String generateInitInstanceFields() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    SourceBuilder s = s().in(2);
    for (FieldDef f : def.fields()) {
      s.a(CR, "self._", f.sourceName(), " = ", f.defaultValueOrNull());
    }
    s.out();
    return content();
  }

  @Override
  protected String generateCopyFromBuilderToImmutable() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s().in(2);
    for (FieldDef f : def.fields()) {
      s().cr();
      f.dataType().sourceExpressionToImmutable(s(), f, "v._" + f.sourceName(), "self._" + f.sourceName());
    }
    s().out();
    return content();
  }

  @Override
  protected String generateSetters() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    SourceBuilder s = s().in(0);
    for (FieldDef f : def.fields()) {
      s.a("\\\\", CR);
      DataType d = f.dataType();
      s.a("def set_", setExpr(def, f), "(self, x):", OPEN);
      String targetExpr = "self._" + f.sourceName();
      d.sourceSetter(s, f, targetExpr);
      s.a(CR, "return self", CLOSE);
    }
    s.out();
    return content();
  }

  @Override
  protected String generateToJson() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    SourceBuilder s = s().in(2);
    s.a("m = {}", CR);
    for (FieldDef fieldDef : def.fields())
      fieldDef.dataType().sourceSerializeToObject(s, fieldDef);
    s.a("return m");
    s.a(CLOSE);
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
    // Python have this:
    //
    //  File path:        <path> . <filename>
    //  Import statement: <package> . <filename> . <ClassName>
    //
    // We refer to external types within .dat files in the same way as Java import statements:
    //
    // External ref:      <package> . <ClassName>
    //
    // With the special rule that if no package is specified, it assumes the same package as the current
    // datatype being generated.
    //
    // When referring to such classes within generated Python code, we will derive and insert a filename
    // if the package includes 'gen'.
    //

    for (String cn : qualifiedClassNames) {
      QualifiedName q = ParseTools.assertHasPackage(ParseTools.parseQualifiedName(cn, null));
      s().a("from ", q.packagePath(), " import ", q.className()).cr();
    }
    return content();
  }

  @Override
  protected String generateParse() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    SourceBuilder s = s();
    s.br();
    s().in(2);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceDeserializeFromObject(s, f);
      s.cr();
    }
    s.a(OUT);
    return content();
  }

  @Override
  protected String generateGetters() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    SourceBuilder s = s().in(0);
    for (FieldDef f : def.fields()) {
      s.a("\\\\").cr();
      s.a("def ", f.sourceName(), "(self):", OPEN, //
          "return self._", f.sourceName(), CLOSE);
    }
    s.out();
    return content();
  }

  @Override
  protected String generateImmutableToBuilder() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    SourceBuilder s = s().in(2);
    s.a("x = ", def.name(), "Builder()", CR);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceIfNotNull(s, f);
      s.a("x._", f.sourceName(), " = ", f.dataType().sourceExpressionToMutable("self._" + f.sourceName()),
          CR);
      f.dataType().sourceEndIf(s).cr();
    }
    s.a("return x", CR);
    s.out();
    return content();
  }

  @Override
  protected String generateEquals() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    SourceBuilder s = s().in(4);
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
        s.a("and self.",f.instanceName(), " == other.", f.instanceName() );
      }
      if (!first)
        s.out();
    }
    s.out();
    return content();
  }

  @Override
  protected String generateHashCode() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    SourceBuilder s = s().in(4);
    s.a("r = 1", CR);
    for (FieldDef f : def.fields()) {
      f.dataType().sourceIfNotNull(s, f);
      f.dataType().sourceHashCalculationCode(s, f);
      f.dataType().sourceEndIf(s).cr();
    }
    s.a("self._hash_value = r").out();
    return content();
  }

  @Override
  protected void postGenerate() {
    Files files = Context.files;
    // This is annoying, but to make relative imports work in Python we need to ensure
    // there's an (empty) file '__init__.py' in the same directory as any Python file.
    //
    if (!files.dryRun()) {
      File parent = Files.parent(sourceFile());
      File sentinelFile = new File(parent, "__init__.py");
      Context.generatedFilesSet.add(sentinelFile);
      if (!sentinelFile.exists())
        files.write(DataUtil.EMPTY_BYTE_ARRAY, sentinelFile);
    }
  }

  /**
   * Get the name of a setter
   */
  private String setExpr(GeneratedTypeDef m, FieldDef f) {
    return f.sourceNameLowerFirst();
  }

  private static String sClassTemplate = Files.readString(SourceGen.class, "class_template_py.txt");
  private static String sEnumTemplate = Files.readString(SourceGen.class, "enum_template_py.txt");

}
