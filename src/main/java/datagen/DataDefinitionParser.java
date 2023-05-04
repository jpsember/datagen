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

import static js.base.Tools.*;
import static datagen.ParseTools.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import datagen.datatype.ContractDataType;
import datagen.datatype.EnumDataType;
import datagen.gen.PartialType;
import datagen.gen.QualifiedName;
import datagen.gen.TypeStructure;
import js.file.Files;
import js.json.JSMap;
import js.parsing.ScanException;
import js.parsing.Scanner;
import js.parsing.Token;
import js.base.BaseObject;
import js.data.DataUtil;

/**
 * Parse .dat file
 */
final class DataDefinitionParser extends BaseObject {

  /**
   * Parse .dat file; store generated type definition to Context
   */
  public void parse() {
    try {
      prepareHandlers();
      startScanner();
      while (scanner().hasNext()) {
        if (readIf(DEPRECATION)) {
          if (mDeprecationToken != null)
            mReadIfToken.fail("unexpected");
          mDeprecationToken = mReadIfToken;
        }
        if (readIf(UNSAFE)) {
          if (mUnsafeToken != null)
            mReadIfToken.fail("unexpected");
          mUnsafeToken = mReadIfToken;
        }

        handler(read()).run();
        if (mDeprecationToken != null)
          mDeprecationToken.fail("unused");
        if (mUnsafeToken != null)
          mUnsafeToken.fail("unused");
      }

      if (Context.generatedTypeDef == null)
        badArg("No 'fields {...}' specified");
      reportUnusedReferences();
    } catch (Throwable t) {
      if (t instanceof ScanException || SHOW_STACK_TRACES) {
        throw t;
      }
      if (mLastReadToken != null) {
        Throwable t2 = mLastReadToken.fail(t.toString());
        throw new RuntimeException(t2.getMessage(), t);
      }
      throw t;
    }
  }

  private Token mDeprecationToken;
  private Token mUnsafeToken;

  private void reportUnusedReferences() {
    String summary = Context.dataTypeManager.unusedReferencesSummary();
    if (!summary.isEmpty()) {
      if (Context.config.treatWarningsAsErrors()) {
        throw badArg(summary);
      } else {
        if (!Context.config.quietMode())
          pr(summary);
      }
    }
  }

  private void prepareHandlers() {
    mHandlers = hashMap();
    mHandlers.put(EXTERN, () -> processExternalReference(ContractDataType.construct()));
    mHandlers.put(FIELDS, () -> procDataType(false));
    mHandlers.put(CLASS, () -> procDataType(true));
    mHandlers.put(ENUM, () -> procEnum());
  }

  private Runnable handler(Token token) {
    Runnable r = mHandlers.get(token.id());
    if (r == null)
      throw token.fail("No handler for", quote(token.text()), "id:", token.id());
    return r;
  }

  private Scanner scanner() {
    return mScanner;
  }

  private void startScanner() {
    String datPath = Context.datWithSource.datRelPath();
    File absFile = new File(Context.config.datPath(), datPath);
    String fileContent = Files.readString(absFile);
    mScanner = new Scanner(dfa(), fileContent);
    mScanner.setSourceDescription(datPath);
    mLastReadToken = null;
    mPackageName = null;
  }

  private boolean readIf(int type) {
    Token t = scanner().peek();
    boolean result = (t != null && t.id(type));
    if (result)
      mReadIfToken = read();
    return result;
  }

  private Token mReadIfToken;

  private Token read() {
    mLastReadToken = scanner().read();
    return mLastReadToken;
  }

  private String read(int type) {
    Token t = read();
    if (t.id() != type)
      fail("expected token of type:", dfa().tokenName(type), "but got", dfa().tokenName(t.id()));
    return t.text();
  }

  private void fail(Object... messages) {
    throw mLastReadToken.fail(messages);
  }

  /**
   * Process a reference to an externally defined type (either a
   * DataContractDataType or an EnumDataType)
   */
  private void processExternalReference(DataType dataType) {
    String nameExpression = read(ID);
    read(SEMI);
    QualifiedName qualifiedClassName = parseQualifiedName(nameExpression, packageName());
    qualifiedClassName = updateForPython(qualifiedClassName);
    dataType.setQualifiedClassName(qualifiedClassName);
    dataType.setDeclaredFlag();
    Context.dataTypeManager.add(qualifiedClassName.className(), dataType);
  }

  private static boolean sOldStyleWarningIssued;

  private void procDataType(boolean classMode) {
    if (Context.config.classMode())
      classMode = true;
    if (!classMode) {
      if (!sOldStyleWarningIssued && !testMode()) {
        sOldStyleWarningIssued = true;
        pr("Generating older version of source code; recommend using 'class' keyword instead of 'fields'...");
      }
    }

    String typeName = DataUtil.convertUnderscoresToCamelCase(
        Files.removeExtension(new File(Context.datWithSource.datRelPath()).getName()));
    setGeneratedTypeDef(new GeneratedTypeDef(typeName, packageName(), null, classMode));

    if (mDeprecationToken != null) {
      Context.generatedTypeDef.setDeprecated(true);
      mDeprecationToken = null;
    }

    boolean unsafeMode = Context.config.unsafe();
    if (mUnsafeToken != null) {
      unsafeMode = true;
      mUnsafeToken = null;
    }

    Context.generatedTypeDef.setUnsafe(unsafeMode);

    read(BROP);

    while (true) {
      if (readIf(BRCL))
        break;

      TypeStructure structure;
      boolean optional = false;
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
      if (readIf(OPTIONAL))
        optional = true;
      if (readIf(REPEATED))
        structure = TypeStructure.LIST;
      else if (readIf(MAP))
        structure = TypeStructure.KEY_VALUE_MAP;
      else if (readIf(SET))
        structure = TypeStructure.VALUE_SET;
      else
        structure = TypeStructure.SCALAR;

      PartialType primaryType = parsePartialType();

      PartialType auxType = null;
      if (structure == TypeStructure.KEY_VALUE_MAP)
        auxType = parsePartialType();

      String fieldName = read(ID);

      FieldDef fieldDef = Context.generatedTypeDef.addField(deprecated, optional, structure, primaryType,
          auxType, fieldName);

      if (readIf(EQUALS)) {
        checkState(!fieldDef.optional(), "cannot mix optional and default values");

        // Parse the following expression, as one of:
        //
        // A Json map, starting with {
        // A JSList, starting with [
        // A string, starting with "
        // 
        todo("refactor to make these names more sensible");
        JSMap defaultValue = parseDefaultValueAsJsonMap();

        // See if there is a parser for default values for this field.  This can either be the data type's parseDefaultValue() method,
        // or one mapped to the type's class (in case it is outside of the datagen project)
        String key = fieldDef.dataType().typeName();
        DefaultValueParser parser = Context.dataTypeManager.parser(key);
        if (parser == null)
          parser = fieldDef.dataType();
        String defValue = parser.parseDefaultValue(Context.generatedTypeDef.classSpecificSourceBuilder(),
            fieldDef, defaultValue);
        fieldDef.setDefaultValue(defValue);
      }

      read(SEMI);
    }
  }

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
        break;
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

  private PartialType parsePartialType() {
    PartialType.Builder t = PartialType.newBuilder();
    if (readIf(ENUM))
      t.enumFlag(true);
    t.name(read(ID));
    return t.build();
  }

  private void procEnum() {
    DataType enumDataType = EnumDataType.construct();
    // If this is a declaration, an id followed by ;
    if (scanner().peek().id(ID)) {
      processExternalReference(enumDataType);
      return;
    }

    // Otherwise, it's a definition
    //
    String enumName;
    String className2 = chomp(new File(Context.datWithSource.datRelPath()).getName(),
        DOT_EXT_DATA_DEFINITION);
    enumName = DataUtil.convertUnderscoresToCamelCase(className2);
    QualifiedName className = parseQualifiedName(enumName, packageName());
    //EnumDataType enumDataType = new EnumDataType();
    enumDataType.setQualifiedClassName(className);
    setGeneratedTypeDef(new GeneratedTypeDef(className.className(), packageName(), enumDataType, false));

    Context.generatedTypeDef.setDeprecated(readIf(DEPRECATION));

    read(BROP);

    while (true) {
      if (readIf(BRCL))
        break;
      String name = read(ID);
      ((EnumDataType) enumDataType).addLabel(name.toUpperCase());
      while (readIf(COMMA) || readIf(SEMI))
        continue;
    }
  }

  /**
   * Get package for the data type being genearted
   */
  private String packageName() {
    if (mPackageName == null) {
      File datPath = new File(Context.datWithSource.datRelPath());
      String parentName = nullToEmpty(datPath.getParent());
      switch (Context.config.language()) {
      case JAVA:
      case PYTHON:
        mPackageName = parentName.replace('/', '.');
        break;
      case GO: {
        int c = parentName.indexOf('/');
        if (c < 0)
          c = parentName.length();
        mPackageName = parentName.substring(0, c);
      }
        break;
      }
      checkNotNull(mPackageName, "language not supported");
    }
    return mPackageName;
  }

  private void setGeneratedTypeDef(GeneratedTypeDef d) {
    checkState(Context.generatedTypeDef == null, "Multiple data types per file is not allowed");
    Context.generatedTypeDef = d;
  }

  private Scanner mScanner;
  private Token mLastReadToken;
  private String mPackageName;
  private Map<Integer, Runnable> mHandlers;

}
