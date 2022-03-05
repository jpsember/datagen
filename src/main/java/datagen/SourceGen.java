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

import datagen.gen.DatagenConfig;
import datagen.gen.Language;
import datagen.gen.QualifiedName;

/**
 * Generate source code from previously parsed data fields and source templates
 */
public abstract class SourceGen extends BaseObject {

  public static SourceGen construct(Context context) {
    SourceGen result = null;
    switch (context.language()) {
    case JAVA:
      result = new JavaSourceGen();
      break;
    case PYTHON:
      result = new PythonSourceGen();
      break;
    }
    if (result == null)
      notSupported(context.language());
    result.prepare(context);
    return result;
  }

  private void prepare(Context context) {
    mContext = context;
    mSourcebuilder = new SourceBuilder(context.language());
  }

  public final DatagenConfig config() {
    return context().config;
  }

  public final String sourceFileExtension() {
    return sourceFileExtension(context().language());
  }

  public static String sourceFileExtension(Language language) {
    switch (language) {
    default:
      throw notSupported(language);
    case JAVA:
      return "java";
    case PYTHON:
      return "py";
    }
  }

  public abstract void generate();

  public final File sourceFile() {
    return new File(config().sourcePath(), sourceFileRelative());
  }

  public final String sourceFileRelative() {
    return context().datWithSource.sourceRelPath();
  }

  protected String content() {
    return s().content();
  }

  protected SourceBuilder s() {
    return mSourcebuilder;
  }

  protected Context context() {
    return mContext;
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
  public final String extractImportStatements(String template) {
    MacroParser parser = new MacroParser();
    if (false && Context.WTF)
      pr(VERT_SP,DASHES,CR, "extracting import statements, template:",CR,template,DASHES);
    if (Context.WTF)
      pr("extracting import statements");
    
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
      Context.checkWTF(key);
      context().includeImport(qualifiedName.combined());
      return s1;
    });
    return result;
  }

  private Context mContext;
  private SourceBuilder mSourcebuilder;

}
