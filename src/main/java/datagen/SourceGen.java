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
import js.parsing.MacroParser;

import static js.base.Tools.*;

import java.io.File;
import java.util.List;
import java.util.Set;

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

  public abstract void generate();

  protected SourceGen() {
    mSourceBuilder = new SourceBuilder(Context.config.language());
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

  private SourceBuilder mSourceBuilder;
  private Set<String> mImportedClasses;

}
