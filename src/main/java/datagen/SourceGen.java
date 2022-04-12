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

import js.base.BaseObject;
import js.file.Files;
import js.json.JSMap;
import js.parsing.MacroParser;

import static js.base.Tools.*;

import java.io.File;
import java.util.List;
import java.util.Set;

import datagen.datatype.EnumDataType;
import datagen.gen.QualifiedName;

import static datagen.Utils.*;

/**
 * Generate source code from previously parsed data fields and source templates
 */
public abstract class SourceGen extends BaseObject {

  public static SourceGen construct() {
    switch (Context.config.language()) {
    default:
      throw languageNotSupported();
    case JAVA:
      return new JavaSourceGen();
    case PYTHON:
      return new PythonSourceGen();
    }
  }

  protected SourceGen() {
    mSourceBuilder = new SourceBuilder(Context.config.language());
  }

  // This was derived from the JavaSourceGen method
  public final void generate() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    s().reset();

    JSMap m = map();
    m.put("package_decl", generatePackageDecl(def));
    // In this first pass, leave the imports macro unchanged
    m.put("imports", "[!imports]");
    m.put("class", def.name());

    String content = getTemplate();
    if (def.isEnum()) {
      generateEnumValues(def.enumDataType());
      m.put("default_value", def.enumDataType().labels().get(0));
      m.put("enum_values", content());
    } else {
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

    postGenerate();
  }

  /**
   * Perform any additional tasks after source file has been generated. Default
   * implementation does nothing
   */
  protected void postGenerate() {
  }

  protected final File sourceFile() {
    return new File(Context.config.sourcePath(), sourceFileRelative());
  }

  protected final String sourceFileRelative() {
    return Context.datWithSource.sourceRelPath();
  }

  /**
   * Get SourceBuilder
   */
  protected final SourceBuilder s() {
    return mSourceBuilder;
  }

  /**
   * Get content of SourceBuilder, and reset the SourceBuilder
   */
  protected final String content() {
    return s().content();
  }

  /**
   * <pre>
   * Find references to classes that need importing, eg. "import json"
   * 
   * We will look for expressions of the form "{{xxxx|yyyy}}" and then:
   * 
   * 1) replace that expression with "yyyy" within the source
   * 2) generate an import statement "xxxx" 
   * 
   * Note this is different from the Java technique, but this is a better way
   * </pre>
   */
  protected final String extractImportStatements(String template) {
    Set<String> statementSet = hashSet();
    MacroParser parser = new MacroParser();
    parser.withPattern(ParseTools.IMPORT_REGEXP);
    parser.withTemplate(template);
    String result = parser.content(key -> {
      // See if this occurrence lies within a string constant; if so, return the original text
      {
        int quotesCount = 0;
        for (int cursor = parser.keyCursor(); cursor > 0; cursor--) {
          char c = template.charAt(cursor);
          if (c == '\n')
            break;
          if (c == '"') {
            if (cursor > 0 && template.charAt(cursor - 1) == '\\') {
            } else
              quotesCount++;
          }
        }
        if (quotesCount % 2 != 0)
          return key;
      }
      List<String> subExp = split(key, '|');
      checkArgument(subExp.size() == 2, "can't parse key into subexpressions:", key, subExp);
      String s0 = subExp.get(0);
      String s1 = subExp.get(1);
      QualifiedName qualifiedName = ParseTools.parseQualifiedName(s0);
      statementSet.add(qualifiedName.combined());
      return s1;
    });
    mImportedClasses = statementSet;
    return result;
  }

  /**
   * Get set of import statements constructed from last call to
   * extractImportStatements()
   */
  protected final Set<String> getImports() {
    checkNotNull(mImportedClasses);
    return mImportedClasses;
  }

  /**
   * Call s().in(...) with a particular offset
   */
  protected final void in(int amount) {
    s().in(mInset + amount);
  }

  private SourceBuilder mSourceBuilder;
  private Set<String> mImportedClasses;

  private int mInset;

  private static final String NOT_SUPPORTED = "<<not supported>>";

  protected abstract String getTemplate();

  protected abstract String generatePackageDecl(GeneratedTypeDef def);

  protected abstract void generateEnumValues(EnumDataType dt);

  protected abstract String generateInitInstanceFields(GeneratedTypeDef def);

  protected abstract String generateCopyFromBuilderToImmutable(GeneratedTypeDef def);

  protected abstract String generateSetters(GeneratedTypeDef def);

  protected String generateToString(GeneratedTypeDef def) {
    return NOT_SUPPORTED;
  }

  protected abstract String generateToJson(GeneratedTypeDef def);

  protected abstract String generateImports();

  protected abstract String generateParse(GeneratedTypeDef def);

  protected abstract String generateGetters(GeneratedTypeDef def);

  protected abstract String generateImmutableToBuilder(GeneratedTypeDef def);

  protected abstract String generateStringConstants(GeneratedTypeDef def);

  protected String generateInstanceFields(GeneratedTypeDef def) {
    return NOT_SUPPORTED;
  }

  protected abstract String generateEquals(GeneratedTypeDef def);

  protected abstract String generateHashCode(GeneratedTypeDef def);

}
