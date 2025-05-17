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

import static js.base.Tools.*;
import static datagen.ParseTools.*;
import static datagen.Utils.*;

import java.io.File;
import java.util.List;
import java.util.Set;

import datagen.datatype.EnumDataType;
import datagen.gen.PartialType;
import datagen.gen.TypeStructure;
import js.file.Files;
import js.json.JSMap;
import js.parsing.RegExp;
import js.parsing.ScanException;
import js.parsing.Scanner;
import js.parsing.Token;
import js.base.BaseObject;
import js.data.DataUtil;

/**
 * Parse .dat file
 */
public final class DataDefinitionParser extends BaseObject {

  /**
   * Parse .dat file
   */
  public void parse(File relativeDatPath) {
//    if (todo("reenable try/catch")) {
//      auxParse(relativeDatPath);
//      return;
//    }
    try {
      auxParse(relativeDatPath);
    } catch (Throwable t) {
      if (t instanceof ScanException || SHOW_STACK_TRACES) {
        if (SHOW_STACK_TRACES)
          pr(t);
        throw t;
      }
      if (mLastReadToken != null) {
        mLastReadToken.failWith(t.toString());
      }
      throw t;
    }
  }

  private void auxParse(File relativeDatPath) {
    startScanner(relativeDatPath);

    //  TODO: document this better, esp. with recent refactoring
    //
    //  <dat_file> ::=
    //     <extern_ref>*    ( <class_def> | <enum_def> )
    //
    //  <extern_ref> ::=
    //     (extern | enum) <type_name>;          <-- maybe require extern to precede enum?
    //                                               then we could support multiple classes per file
    //
    //  <class_def> ::=
    //        [-] class { <fields>* }
    //
    //  <enum_def> ::=
    //        [-] enum  { <enum_names>* }
    //
    //

    p54("relativeDatPath:", relativeDatPath);

    todo("!mRelativeDatPath is unnecessary; only the basename of the dat file is useful, the directory can be found in the context");
    mRelativeDatPath = relativeDatPath;
    mGeneratedClassNames = hashSet();

    while (scanner().hasNext()) {

      // Prepare for next data type, etc
      mDeprecated = scanner().readIf(DEPRECATION) != null;

      p54(VERT_SP, "reading next token");
      var t = read();
      p54("read:", t, VERT_SP);

      switch (t.text()) {
        case "extern":
          processExternalReference(DataTypeManager.constructContractDataType());
          break;
        case "class":
          procDataType();
          break;
        case "enum": {
          // If "enum <name> ;", it's an external reference
          //
          var t2 = scanner().peek(1);
          if (t2 != null && t2.id(SEMI))
            processExternalReference(EnumDataType.construct());
          else
            procEnum();
        }
        break;
        default:
          t.failWith("unexpected token:", t.text());
      }
    }

    reportUnusedReferences();
  }

  private void reportUnusedReferences() {
    String summary = Context.dataTypeManager().unusedReferencesSummary();
    if (!summary.isEmpty()) {
      if (Context.config.treatWarningsAsErrors()) {
        throw badArg(summary);
      } else {
        if (!Context.config.quietMode())
          pr(summary);
      }
    }
  }

  private Scanner scanner() {
    return mScanner;
  }

  private void startScanner(File relDatPath) {
    String datPath = relDatPath.toString();
    File absFile = new File(Context.config.datPath(), datPath);
    String fileContent = Files.readString(absFile);
    mScanner = new Scanner(dfa(), fileContent);
    mScanner.setSourceDescription(datPath);
    mLastReadToken = null;
    if (false && alert("verbosity")) {
      mScanner.setVerbose(true);
      p54("scanning:", INDENT, fileContent);
    }
  }

  private Token peek() {
    return scanner().peek();
  }

  private boolean readIf(boolean flag) {
    if (flag)
      read();
    return flag;
  }

  private boolean readIf(String tokenText) {
    Token t = peek();
    return readIf(t != null && t.text().equals(tokenText));
  }

  private boolean readIf(int type) {
    Token t = peek();
    return readIf(t != null && t.id(type));
  }

  private Token read() {
    mLastReadToken = scanner().read();
    return mLastReadToken;
  }

  private String read(int type) {
    Token t = read();
    if (t.id() != type)
      t.failWith("expected token of type:", dfa().tokenName(type), "but got", dfa().tokenName(t.id()));
    return t.text();
  }

  /**
   * Process a reference to an externally defined type (either a
   * ContractDataType or an EnumDataType)
   */
  private void processExternalReference(DataType dataType) {
    if (mDeprecated)
      mLastReadToken.failWith("cannot mark external reference deprecated");

    String nameExpression = read(ID);
    p54("processExternalReference, name_expr:", nameExpression);

    read(SEMI);

    todo("default packageName should be derived from\n the relative path of the dat file");

    QualifiedName q = QualifiedName.parse(nameExpression, packageName());
    dataType.withQualifiedName(q);
    dataType.setDeclaredFlag();
    Context.dataTypeManager().add(q.className(), dataType);
  }

  private String determineSourceName(String dataTypeName) {
    String sourceClassName;

    switch (Context.config.language()) {
      default:
        throw languageNotSupported();
      case JAVA:
        sourceClassName = DataUtil.convertUnderscoresToCamelCase(dataTypeName);
        break;
      case PYTHON:
        sourceClassName = dataTypeName;
        break;
      case GO:
        sourceClassName = dataTypeName;
        break;
      case RUST:
        sourceClassName = dataTypeName;
        break;
    }
    return sourceClassName;
  }


  // If the next token is an identifier, it is the name of the generated class.
  // Otherwise, derive it from the dat file
  //
  private String parseClassNameOrDerive() {
    String className;
    {
      var t = scanner().readIf(ID);
      if (t != null) {
        className = t.text();
      } else {
        className = Files.basename(mRelativeDatPath);
      }
    }
    return className;
  }

  private void ensureClassNameUnique(String className) {
    todo("!actually, it only needs be unique within its directory");
    checkState(!mGeneratedClassNames.contains(className), "duplicate generated class name");
    mGeneratedClassNames.add(className);
  }


  private String determineRelativePath() {
    String relPathExpr;
    {
      File relPath = mRelativeDatPath.getParentFile();
      if (relPath == null)
        relPathExpr = "";
      else {
        relPathExpr = relPath + "/";
      }
    }
    return relPathExpr;
  }

  private void procDataType() {
    String className = parseClassNameOrDerive();
    ensureClassNameUnique(className);

    File relativeClassFile = new File(determineRelativePath() + determineSourceName(className) + "." + sourceFileExtension());

    todo("!process clean later");
//      if (config.clean()) {
    //  File sourceFile = new File(config.sourcePath(), relativeClassFile);
//        // If we haven't yet done so, delete the 'gen' directory that will contain this source file
    //   File genDirectory = determineGenDirectory(sourceFile);
//        discardGenDirectory(discardedDirectoriesSet, genDirectory);
//      }


    Context.prepareForClassOrEnumDefinition(relativeClassFile);

    String typeName = DataUtil.convertUnderscoresToCamelCase(className);
    setGeneratedTypeDef(new GeneratedTypeDef(typeName, packageName(), null));

    Context.generatedTypeDef.setDeprecated(mDeprecated);

    read(BROP);

    while (true) {
      if (readIf(BRCL))
        break;

      TypeStructure structure;
      boolean deprecated = false;

      // A field specification has this syntax:
      //
      //  [-] [?] [* | map | set] <typespec> [<typespec>] [= <default value>]
      //
      // A <typespec> has this syntax:
      //
      //  <typespec> ::=  [enum] <type>
      //
      if (readIf(DEPRECATION))
        deprecated = true;

      if (readIf(OPTIONAL)) {
        throw badArg("optional fields are no longer supported");
      }
      if (readIf(REPEATED))
        structure = TypeStructure.LIST;
      else if (readIf("map"))
        structure = TypeStructure.KEY_VALUE_MAP;
      else if (readIf("set"))
        structure = TypeStructure.VALUE_SET;
      else
        structure = TypeStructure.SCALAR;

      PartialType primaryType = parsePartialType();

      PartialType auxType = null;
      if (structure == TypeStructure.KEY_VALUE_MAP)
        auxType = parsePartialType();

      String fieldName = read(ID);

      {
        String asSnakeCase = DataUtil.convertCamelCaseToUnderscores(fieldName);
        if (!(fieldName.equals(asSnakeCase))) {
          badArg("field name", quote(fieldName), "isn't using 'snake case':", quote(asSnakeCase));
        }
      }

      FieldDef fieldDef = Context.generatedTypeDef.addField(deprecated, false, structure, primaryType,
          auxType, fieldName);

      if (readIf(EQUALS)) {
        // See if there is a parser for default values for this field.  This can either be the data type's parseDefaultValue() method,
        // or one mapped to the type's class (in case it is outside of the datagen project)
        String key = fieldDef.dataType().typeName();
        DefaultValueParser parser = Context.dataTypeManager().parser(key);
        if (parser == null)
          parser = fieldDef.dataType();
        String defaultValueSource = parser.parseDefaultValue(
            Context.generatedTypeDef.classSpecificSourceBuilder(), fieldDef, parseDefaultValueAsJsonMap());
        fieldDef.setDefaultValue(defaultValueSource);
      }

      read(SEMI);
    }

    // Process optional sql information:
    //
    // sql { ...args... }
    //
    //
    if (readIf("sql")) {
      processSqlInfo();
    }

    genSource();

    Context.updateRustModule(relativeClassFile);

  }

  private void genSource() {
    Context.sql.generate();

    // Generate source file in appropriate language
    //
    SourceGen g = SourceGen.construct();
    g.setVerbose(verbose());
    g.generate();
  }


  private void procEnum() {
    String className2 = parseClassNameOrDerive();
    ensureClassNameUnique(className2);
    DataType dataType = EnumDataType.construct();

    var relativeClassFile = new File(determineRelativePath() + determineSourceName(className2) + "." + sourceFileExtension());
    Context.prepareForClassOrEnumDefinition(relativeClassFile);
    todo("see what common code with procDataType there is");

    var enumName = DataUtil.convertUnderscoresToCamelCase(className2);

    var pn = packageName();
    QualifiedName className = QualifiedName.parse(enumName, pn);
    dataType.withQualifiedName(className);

    setGeneratedTypeDef(new GeneratedTypeDef(className.className(), pn, dataType));
    Context.generatedTypeDef.setDeprecated(mDeprecated);

    read(BROP);

    while (true) {
      if (readIf(BRCL))
        break;
      String name = read(ID);
      ((EnumDataType) dataType).addLabel(name);
      while (readIf(COMMA) || readIf(SEMI))
        ;
    }

    genSource();

    Context.updateRustModule(relativeClassFile);

  }

  private void processSqlInfo() {
    boolean db = false;
    if (db)
      scanner().setVerbose();

    SqlGen sql = Context.sql;
    sql.setTypeDef(Context.generatedTypeDef);

    read(BROP);

    while (!readIf(BRCL)) {

      if (readIf("table")) {
        // Optionally has extra arguments (...)
        if (readIf(PAROP)) {
          while (!readIf(PARCL)) {
            read().failWith("unexpected token");
          }
        }
        continue;
      }

      if (readIf("index")) {
        List<String> fields = arrayList();
        if (readIf(PAROP)) {
          while (!readIf(PARCL)) {
            readFieldName(fields);
          }
        } else {
          readFieldName(fields);
        }
        sql.addIndex(fields);
        continue;
      }

      read().failWith("unexpected token");
    }
    if (db)
      scanner().setVerbose(false);
  }

  private void readFieldName(List<String> fields) {
    Token t = read();
    var fieldName = t.text();
    if (!isValidIdentifier(fieldName))
      t.failWith("Not a valid field name");
    fields.add(fieldName);
  }

  private static boolean isValidIdentifier(String s) {
    return RegExp.patternMatchesString("[_a-zA-Z]\\w*", s);
  }

  /**
   * Parse the next series of tokens as a json value, one of:
   *
   * A Json map, starting with {
   *
   * A JSList, starting with [
   *
   * A string, starting with "
   *
   * A boolean or number
   *
   * If the resulting json value is not a JSMap, wrap it in a JSMap with key ""
   *
   * Be forgiving of incorrect json, specifically:
   *
   * 1) allow (one) extra comma at the end of a map or list
   *
   * 2) allow unquoted strings
   */
  private JSMap parseDefaultValueAsJsonMap() {
    StringBuilder sb = new StringBuilder();
    List<Integer> stack = arrayList();
    boolean done = false;
    while (!done) {
      Token t = read();
      switch (t.id()) {
        case NUMBER:
        case STRING:
        case BOOL:
          if (stack.isEmpty())
            done = true;
          break;
        case BROP:
          push(stack, BRCL);
          break;
        case SQOP:
          push(stack, SQCL);
          break;
        case BRCL:
        case SQCL:
          checkState(last(stack) == t.id());
          pop(stack);
          if (stack.isEmpty())
            done = true;
          break;
        default:
          checkState(!stack.isEmpty());
          // If not ',' ':' or boolean, wrap in quotes
          if (!(t.id(COLON) || t.id(COMMA) || t.id(BOOL))) {
            t = withText(t, quote(t.text()));
          }
          break;
      }

      // If we're appending } or ], and previous character is ',', remove previous character (extra comma)
      if (t.id(BRCL) || t.id(SQCL)) {
        int x = sb.length();
        if (x > 0 && sb.charAt(x - 1) == ',') {
          sb.setLength(x - 1);
        }
      }

      sb.append(t.text());
    }
    String result = sb.toString();

    // If it's not a map, wrap it in a map with key ""
    //
    if (!result.startsWith("{")) {
      result = "{\"\":" + result + "}";
    }

    return new JSMap(result);
  }

  private static Token withText(Token t, String text) {
    return new Token(t.source(), t.id(), t.name(), text, t.row(), t.column());
  }

  private PartialType parsePartialType() {
    PartialType.Builder t = PartialType.newBuilder();
    if (readIf("enum"))
      t.enumFlag(true);
    if (peek().id(RESERVEDWORD)) {
      var tk = read();
      tk.failWith("Reserved word encountered:", tk.text());
    }
    t.name(read(ID));
    return t.build();
  }

  /**
   * Get package for the data type being generated
   */
  private String packageName() {
    String name = null;
    String parentName = Context.datDirectoryRelative().toString();
    switch (Context.config.language()) {
      case JAVA:
      case PYTHON:
        name = parentName.replace('/', '.');
        break;
      case GO: {
        // I think we want to set the package name to the last component of the package name
        int c = parentName.lastIndexOf('/');
        name = parentName.substring(c + 1);
      }
      break;
      case RUST: {
        int c = parentName.lastIndexOf(':');
        name = parentName.substring(c + 1);
      }
      break;
      default:
        Utils.languageNotSupported();
        break;
    }
    checkNotNull(name, "language not supported");
    return name;
  }

  private void setGeneratedTypeDef(GeneratedTypeDef d) {
    checkState(Context.generatedTypeDef == null, "Multiple data types per file is not allowed");
    Context.generatedTypeDef = d;
  }

  private Scanner mScanner;
  private Token mLastReadToken;
  private boolean mDeprecated;
  private File mRelativeDatPath;
  private Set mGeneratedClassNames;
}
