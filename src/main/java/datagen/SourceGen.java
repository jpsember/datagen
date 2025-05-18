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

import java.io.File;
import java.util.List;
import java.util.Set;

import static js.base.Tools.*;
import static datagen.Utils.*;

import js.base.BaseObject;
import js.file.Files;
import js.json.JSMap;
import js.parsing.MacroParser;
import datagen.datatype.EnumDataType;

/**
 * Generate source code from previously parsed data fields and source templates
 */
public abstract class SourceGen extends BaseObject {

  public static SourceGen construct() {
    switch (Context.pt.language()) {
      default:
        throw languageNotSupported();
      case JAVA:
        return new JavaSourceGen();
      case PYTHON:
        return new PythonSourceGen();
      case GO:
        return new GoSourceGen();
      case RUST:
        return new RustSourceGen();
    }
  }

  public final void generate() {
    GeneratedTypeDef def = Context.generatedTypeDef;
    JSMap m = map();
    m.put("package_decl", generatePackageDecl());
    // In this first pass, leave the imports macro unchanged
    m.put("imports", "[!imports]");
    m.put("class", def.name());

    String content = getTemplate();
    m.put("deprecated", def.isDeprecated() ? getDeprecationSource() : "");

    if (def.isEnum()) {
      generateEnumValues(def.enumDataType());
      m.put("enum_values", content());
      m.put("default_value", def.enumDataType().labels().get(0));
      addAdditionalTemplateValues(m);
    } else {
      m.put("class_getter_implementation", generateGetters());
      m.put("copy_to_builder", generateImmutableToBuilder());
      m.put("copyfield_from_builder", generateCopyFromBuilderToImmutable());
      m.put("equals", generateEquals());
      m.put("hashcode", generateHashCode());
      m.put("init_instance_fields", generateInitInstanceFields());
      m.put("instance_fields", generateInstanceFields());
      m.put("parse", generateParse());
      m.put("setters", generateSetters());
      m.put("string_constants", generateStringConstants());
      m.put("to_json", generateToJson());
      m.put("to_string", generateToString());
      addAdditionalTemplateValues(m);
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

    if (DEBUG_RUST_IMPORTS && alert("showing content")) {
      pr(DASHES, CR, "Content after pass 1:", CR, DASHES, CR, content);
    }

    // Pass 2: extract import expressions
    //
    content = extractImportExpressions(content);

    // Pass 3: generate the import statements
    //
    m.clear();
    m.put("imports", generateImports(mImportExpressions));
    {
      MacroParser parser = new MacroParser();
      parser.withTemplate(content).withMapper(m);
      content = parser.content();
    }

    // Pass 4: Strip (or retain) optional comments.  Such comments are denoted by a line with the prefix "@@"
    //
    content = ParseTools.processOptionalComments(content, Context.config.comments());

    //
    // Pass 5: remove extraneous linefeeds; insert requested blank lines according to language.  
    //         For Python, lines starting with "\\[n]" indicate that n blank lines are to be placed between 
    //         the neighboring (non-blank) lines
    //
    content = Context.pt.adjustLinefeeds(content);
    File target = def.sourceFile();

    boolean success = Context.generatedFilesSet.add(target);
    checkState(success, "duplicate file generated:", target);

    Context.cleanIfNecessary(target);
    var parent = Files.parent(target);


    Context.files.mkdirs(parent);
    boolean wrote = Context.files.writeIfChanged(target, content);
    var sourceFileRelative = Context.sourceRelPath();
    if (wrote)
      log(".....updated:", sourceFileRelative);
    else {
      target.setLastModified(System.currentTimeMillis());
      log("...freshened:", sourceFileRelative);
    }

    Context.registerGeneratedSource(target,Context.generatedTypeDef);
    todo("is postGenerate used anywhere?");
     postGenerate();
  }

  /**
   * Add any additional key/value pairs to a template, i.e. for other languages
   */
  protected void addAdditionalTemplateValues(JSMap templateMap) {
  }

  /**
   * Perform any additional tasks after source file has been generated. Default
   * implementation does nothing
   */
  protected void postGenerate() {
  }

  protected String getDeprecationSource() {
    return "@Deprecated\n";
  }

  @Deprecated
  protected final File sourceFile() {
    return Files.join(Context.config.sourcePath(), Context.sourceRelPath());
  }

  protected SourceBuilder s = new SourceBuilder(Context.pt.language());

  /**
   * Get content of SourceBuilder, and reset the SourceBuilder
   */
  protected final String content() {
    String content = s.getContent();
    s = new SourceBuilder(Context.pt.language());
    return content;
  }

  /**
   * <pre>
   * Find references to classes that need importing, eg. "import json"
   *
   * We will look for expressions of the form "{{xxxx|yyyy}}" and then:
   *
   * 1) replace that expression with "yyyy" within the source
   * 2) generate an import statement for the expression "xxxx", which might be
   *    a fully qualified class name
   *
   * </pre>
   */
  private String extractImportExpressions(String template) {

    final var db = DEBUG_RUST_IMPORTS;

    Set<String> expressionSet = hashSet();
    MacroParser parser = new MacroParser();
    parser.withPattern(ParseTools.IMPORT_REGEXP);
    parser.withTemplate(template);
    String result = parser.content(key -> {
      // See if this occurrence lies within a string constant; if so, return the original text
      {
        if (db)
          pr("====> found import expr: >>>" + key + "<<<");
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
        if (quotesCount % 2 != 0) {
          if (db)
            pr("====> found key:", key);
          return key;
        }
      }
      List<String> subExp = split(key, '|');
      checkArgument(subExp.size() == 2, "can't parse key into subexpressions:", key, subExp);
      String s0 = subExp.get(0);
      String s1 = subExp.get(1);
      if (db)
        pr("========== adding import expr:", s0);
      expressionSet.add(s0);
      return s1;
    });
    List<String> lst = arrayList();
    lst.addAll(expressionSet);
    lst.sort(null);
    mImportExpressions = lst;
    return result;
  }

  private List<String> mImportExpressions;

  public static final String NOT_SUPPORTED = "<<not supported>>";

  protected abstract String getTemplate();

  protected String generatePackageDecl() {
    return NOT_SUPPORTED;
  }

  protected abstract void generateEnumValues(EnumDataType dt);

  protected abstract String generateInitInstanceFields();

  protected abstract String generateCopyFromBuilderToImmutable();

  protected abstract String generateSetters();

  protected String generateToString() {
    return NOT_SUPPORTED;
  }

  protected abstract String generateToJson();

  protected abstract String generateImports(List<String> expressions);

  protected abstract String generateParse();

  protected abstract String generateGetters();

  protected abstract String generateImmutableToBuilder();

  protected abstract String generateStringConstants();

  protected String generateInstanceFields() {
    return NOT_SUPPORTED;
  }

  protected abstract String generateEquals();

  protected abstract String generateHashCode();

}
