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
import static datagen.Context.*;

import java.io.File;
import java.util.List;

import datagen.datatype.EnumDataType;
import datagen.gen.PartialType;
import datagen.gen.TypeStructure;
import js.file.Files;
import js.json.JSMap;
import js.parsing.Lexeme;
import js.parsing.Lexer;
import js.parsing.LexerException;
import js.parsing.RegExp;
import js.base.BaseObject;
import js.data.DataUtil;

/**
 * Parse .dat file
 */
public final class DataDefinitionParser extends BaseObject {

  public void parse(File relativeDatPath) {
    try {
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

      mRelativeDatPath = relativeDatPath;

      while (scanner().hasNext()) {

        mDeprecated = scanner().readIf(DEPRECATION);

        var t = read();

        switch (t.text()) {
          case "extern":
            processExternalReference(DataTypeManager.constructContractDataType());
            break;
          case "fields":
            pr("*** Please use the word 'class' instead of 'fields'; ", t.locInfo());
            procDataType();
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
    } catch (Throwable t) {
      if (t instanceof LexerException || SHOW_STACK_TRACES) {
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

  private void reportUnusedReferences() {
    String summary = Context.dataTypeManager().unusedReferencesSummary();
    if (!summary.isEmpty()) {
      if (!Context.config.quietMode())
        pr(summary);
    }
  }

  private Lexer scanner() {
    return mScanner;
  }

  private void startScanner(File relDatPath) {
    String datPath = relDatPath.toString();
    File absFile = new File(Context.config.datPath(), datPath);
    String fileContent = Files.readString(absFile);
    mScanner = new Lexer(dfa()).withText(fileContent).withSourceDescription(datPath);
    mLastReadToken = null;
  }

  private Lexeme peek() {
    return scanner().peek();
  }

  private boolean readIf(boolean flag) {
    if (flag)
      read();
    return flag;
  }

  private boolean readIf(String tokenText) {
    var t = peek();
    return readIf(t != null && t.text().equals(tokenText));
  }

  private boolean readIf(int type) {
    var t = peek();
    return readIf(t != null && t.id(type));
  }

  private Lexeme read() {
    mLastReadToken = scanner().read();
    return mLastReadToken;
  }

  private String read(int type) {
    var t = read();
    if (!t.id(type))
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

    read(SEMI);

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
    if (scanner().readIf(ID))
      return scanner().token().text();
    else
      return Files.basename(mRelativeDatPath);
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

  private void prepare(GeneratedTypeDef type) {
    setGeneratedTypeDef(type);
    type.setDeprecated(mDeprecated);
    type.setSourceFile(Files.join(Context.config.sourcePath(), Context.sourceRelPath()));
  }

  private void procDataType() {
    String datClassName = parseClassNameOrDerive();
    var relativeClassFile = new File(determineRelativePath() + determineSourceName(datClassName) + "." + sourceFileExtension());
    Context.prepareForClassOrEnumDefinition(relativeClassFile);
    var typeName = DataUtil.convertUnderscoresToCamelCase(datClassName);
    var pn = packageName();
    prepare(new GeneratedTypeDef(typeName, pn, null));

    read(BROP);

    while (true) {
      if (readIf(BRCL))
        break;

      TypeStructure structure;

      // A field specification has this syntax:
      //
      //  [-] [?] [* | map | set] <typespec> [<typespec>] [= <default value>]
      //
      // A <typespec> has this syntax:
      //
      //  <typespec> ::=  [enum] <type>
      //
      var deprecated = readIf(DEPRECATION);

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
    String datClassName = parseClassNameOrDerive();
    var relativeClassFile = new File(determineRelativePath() + determineSourceName(datClassName) + "." + sourceFileExtension());
    Context.prepareForClassOrEnumDefinition(relativeClassFile);
    var typeName = DataUtil.convertUnderscoresToCamelCase(datClassName);
    var pn = packageName();
    QualifiedName className = QualifiedName.parse(typeName, pn);
    DataType dataType = EnumDataType.construct();
    dataType.withQualifiedName(className);
    prepare(new GeneratedTypeDef(className.className(), pn, dataType));

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
    var t = read();
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
      var t = read();
      var text = t.text();
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
            text = quote(text);
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

      sb.append(text);
    }
    String result = sb.toString();

    // If it's not a map, wrap it in a map with key ""
    //
    if (!result.startsWith("{")) {
      result = "{\"\":" + result + "}";
    }

    return new JSMap(result);
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
      default:
        throw languageNotSupported();
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
    }
    return name;
  }

  private void setGeneratedTypeDef(GeneratedTypeDef d) {
    checkState(Context.generatedTypeDef == null, "Multiple data types per file is not allowed");
    Context.generatedTypeDef = d;
  }

  private Lexer mScanner;
  private Lexeme mLastReadToken;
  private boolean mDeprecated;
  private File mRelativeDatPath;
}
