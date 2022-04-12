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

import static datagen.ParseTools.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.Map;

import datagen.datatype.DataContractDataType;
import datagen.datatype.EnumDataType;
import datagen.gen.QualifiedName;
import datagen.gen.TypeStructure;
import js.file.Files;
import js.parsing.ScanException;
import js.parsing.Scanner;
import js.parsing.Token;
import js.base.BaseObject;
import js.data.DataUtil;

/**
 * Parse .dat file
 */
final class DataDefinitionParser extends BaseObject {

  public void parse() {
    try {
      prepareHandlers();
      startScanner();
      while (scanner().hasNext())
        handler(read()).run();

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

  private void reportUnusedReferences() {
    String summary = Context.dataTypeManager.unusedReferencesSummary();
    if (!summary.isEmpty()) {
      if (Context.config.treatWarningsAsErrors()) {
        throw badArg(summary);
      } else
        pr(summary);
    }
  }

  private void prepareHandlers() {
    mHandlers = hashMap();
    mHandlers.put(EXTERN, () -> procExtern());
    mHandlers.put(FIELDS, () -> procDataType());
    mHandlers.put(ENUM, () -> procEnum());
  }

  private Runnable handler(Token token) {
    Runnable r = mHandlers.get(token.id());
    if (r == null)
      throw token.fail("No handler for", quote(token.text()));
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

  private Token readIf(int type) {
    Token t = scanner().peek();
    if (t != null && t.id(type))
      return read();
    return null;
  }

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

  private void procExtern() {
    auxProcClass(new DataContractDataType());
  }

  private void auxProcClass(DataType dataType) {
    String nameExpression = read(ID);
    read(SEMI);
    QualifiedName qualifiedClassName = parseQualifiedName(nameExpression, determinePackageName());
    dataType.setQualifiedClassName(qualifiedClassName);
    dataType.setDeclaredFlag();
    Context.dataTypeManager.add(qualifiedClassName.className(), dataType);
  }

  private void procDataType() {
    checkState(Context.generatedTypeDef == null, "Multiple data types per file is not allowed");
    String typeName = DataUtil.convertUnderscoresToCamelCase(
        Files.removeExtension(new File(Context.datWithSource.datRelPath()).getName()));
    GeneratedTypeDef msg = new GeneratedTypeDef(typeName);
    Context.generatedTypeDef = msg;
    msg.setPackageName(determinePackageName());

    read(BROP);

    while (true) {
      if (readIf(BRCL) != null)
        break;

      TypeStructure structure = null;
      boolean optional = false;

      while (true) {
        if (readIf(OPTIONAL) != null) {
          checkState(!optional);
          optional = true;
        } else if (readIf(REPEATED) != null) {
          checkState(structure == null);
          structure = TypeStructure.LIST;
        } else if (readIf(MAP) != null) {
          checkState(structure == null);
          structure = TypeStructure.KEY_VALUE_MAP;
        } else
          break;
      }
      if (structure == null)
        structure = TypeStructure.SCALAR;
      String type = read(ID);

      String auxType = null;
      if (structure == TypeStructure.KEY_VALUE_MAP) {
        auxType = read(ID);
      }

      String name = read(ID);

      FieldDef fieldDef = msg.addField(structure, name, type, auxType, optional);

      if (readIf(EQUALS) != null) {
        checkState(!optional, "cannot mix optional and default values");

        // See if there is a parser for default values for this field.  This can either be the data type's parseDefaultValue() method,
        // or one mapped to the type's class (in case it is outside of the datagen project)
        String key = fieldDef.dataType().typeName();
        DefaultValueParser parser = Context.dataTypeManager.parser(key);
        if (parser == null)
          parser = fieldDef.dataType();
        String defValue = parser.parseDefaultValue(scanner(), msg.classSpecificSourceBuilder(), fieldDef);
        fieldDef.setDefaultValue(defValue);
      }

      read(SEMI);
    }
  }

  private void procEnum() {
    // If this is a declaration, an id followed by ;
    if (scanner().peek().id(ID)) {
      auxProcClass(new EnumDataType());
      return;
    }

    // Otherwise, it's a definition
    //
    String enumName;
    String className2 = chomp(new File(Context.datWithSource.datRelPath()).getName(),
        DOT_EXT_DATA_DEFINITION);
    enumName = DataUtil.convertUnderscoresToCamelCase(className2);
    QualifiedName className = parseQualifiedName(enumName, determinePackageName());
    EnumDataType enumDataType = new EnumDataType();
    enumDataType.setQualifiedClassName(className);

    {
      GeneratedTypeDef msg = new GeneratedTypeDef(className.className());
      msg.setEnum(enumDataType);
      msg.setPackageName(determinePackageName());
      Context.generatedTypeDef = msg;
    }

    read(BROP);

    while (true) {
      if (readIf(BRCL) != null)
        break;
      String name = read(ID);
      enumDataType.addLabel(name.toUpperCase());
      while (readIf(COMMA) != null || readIf(SEMI) != null)
        continue;
    }
  }

  private String determinePackageName() {
    if (mPackageName == null) {
      File datPath = new File(Context.datWithSource.datRelPath());
      String parentName = nullToEmpty(datPath.getParent());
      mPackageName = parentName.replace('/', '.');
    }
    return mPackageName;
  }

  private Scanner mScanner;
  private Token mLastReadToken;
  private String mPackageName;
  private Map<Integer, Runnable> mHandlers;
}
