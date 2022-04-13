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

import java.util.Map;

import datagen.datatype.*;
import js.base.BaseObject;
import js.data.AbstractData;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.Matrix;
import js.geometry.Polygon;
import js.parsing.Scanner;

import static datagen.ParseTools.*;
import static js.base.Tools.*;
import static datagen.Utils.*;

/**
 * A map of class names to DataTypes
 */
public final class DataTypeManager extends BaseObject {

  public DataTypeManager() {
    mTypeMap = concurrentHashMap();
    mDefaultValueParserMap = concurrentHashMap();

    DataType tmp;

    switch (Context.config.language()) {
    default:
      throw languageNotSupported();
    case PYTHON:
      tmp = new PyIntDataType();
      add("byte", tmp);
      add("short", tmp);
      add("int", tmp);
      add("long", tmp);
      // There is no distinction between floats and doubles in Python 
      tmp = new PyFloatDataType();
      add("float", tmp);
      add("double", tmp);
      add("File", new PyStringDataType());
      add("IPoint", new PyIPointDataType());
      add("bool", new PyBooleanDataType());
      add("string", new PyStringDataType());
      break;
    case JAVA:
      add("byte", new PrimitiveByteDataType());
      add("short", new PrimitiveShortDataType());
      add("int", new PrimitiveIntegerDataType());
      add("long", new PrimitiveLongDataType());
      add("float", new PrimitiveFloatDataType());
      add("double", new PrimitiveDoubleDataType());
      add("File", new FileDataType());
      add(IPoint.DEFAULT_INSTANCE, IPOINT_PARSER);
      add("bool", new BooleanDataType());
      add("string", new StringDataType());
      break;
    }
    add("JSMap", new JsonMapDataType());
    add("JSList", new JsonListDataType());

    add(IRect.DEFAULT_INSTANCE);
    add(FPoint.DEFAULT_INSTANCE);
    add(FRect.DEFAULT_INSTANCE);
    add(Matrix.DEFAULT_INSTANCE);
    add(Polygon.DEFAULT_INSTANCE);
  }

  /**
   * Get DataType associated with class name, or null if there isn't one
   */
  public DataType get(String key) {
    return mTypeMap.get(key);
  }

  public void add(String key, DataType dataType) {
    add(key, dataType, null);
  }

  public void add(String key, DataType dataType, DefaultValueParser defaultValueParser) {
    DataType previousMapping = mTypeMap.put(key, dataType);
    checkState(previousMapping == null, "duplicate data type for key:", key);
    if (defaultValueParser != null)
      mDefaultValueParserMap.put(dataType.typeName(), defaultValueParser);
  }

  public String unusedReferencesSummary() {
    StringBuilder sb = new StringBuilder();
    Map<String, DataType> sortedMap = treeMap();
    for (DataType t : mTypeMap.values()) {
      if (t.isPrimitive())
        continue;
      sortedMap.put(t.typeName(), t);
    }
    for (DataType x : sortedMap.values()) {
      if (x.declaredFlag() && !x.usedFlag()) {
        if (sb.length() == 0)
          sb.append("Unused references:\n");
        sb.append("  ");
        sb.append(x.qualifiedClassName().combined());
      }
    }
    return sb.toString();
  }

  public DefaultValueParser parser(String key) {
    return mDefaultValueParserMap.get(key);
  }

  /**
   * Construct a DataType for a class that implements the AbstractData
   * interface, and register it
   */
  private void add(AbstractData defaultInstance, DefaultValueParser parser) {
    DataType dataType = new DataContractDataType();
    dataType.setQualifiedClassName(ParseTools.parseQualifiedName(defaultInstance.getClass().getName()));
    add(dataType.qualifiedClassName().className(), dataType, parser);
  }

  private void add(AbstractData defaultInstance) {
    add(defaultInstance, null);
  }

  private static final DefaultValueParser IPOINT_PARSER = new DefaultValueParser() {
    @Override
    public String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource, FieldDef fieldDef) {
      String typeName = fieldDef.dataType().typeName();
      scanner.read(SQOP);
      int x = scanner.readInt(NUMBER);
      scanner.read(COMMA);
      int y = scanner.readInt(NUMBER);
      scanner.read(SQCL);
      String constName = "DEF_" + fieldDef.nameStringConstant();
      classSpecificSource.a("  private static final ", fieldDef.dataType().typeName(), " ", constName,
          "  = new ", typeName, "(", x, ", ", y, ");", CR);
      return constName;
    }

  };

  private final Map<String, DataType> mTypeMap;
  private final Map<String, DefaultValueParser> mDefaultValueParserMap;

}
