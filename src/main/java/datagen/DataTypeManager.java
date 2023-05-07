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
import js.json.JSMap;

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

    switch (language()) {
    default: {
      add("byte", new GoIntDataType(8));
      add("short", new GoIntDataType(16));
      add("int", new GoIntDataType(32));
      add("long", new GoIntDataType(64));
      add("float", new GoFloatDataType(32));
      add("double", new GoFloatDataType(64));
      add("bool", new GoBooleanDataType());
      add("string", new GoStringDataType());
    }
      break;
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
      add("JSMap", new PythonJsonMapDataType());
      add("Matrix", new PyMatrixDataType());
      break;
    case JAVA:
      add("byte", JavaPrimitiveByteDataType.PRIM_TYPE);
      add("short", JavaPrimitiveShortDataType.PRIM_TYPE);
      add("int", JavaPrimitiveIntegerDataType.PRIM_TYPE);
      add("long", JavaPrimitiveLongDataType.PRIM_TYPE);
      add("float", JavaPrimitiveFloatDataType.PRIM_TYPE);
      add("double", JavaPrimitiveDoubleDataType.PRIM_TYPE);
      add("File", new JavaFileDataType());
      add(IPoint.DEFAULT_INSTANCE, IPOINT_PARSER);
      add("bool", JavaPrimitiveBooleanDataType.PRIM_TYPE);
      add("string", new JavaStringDataType());
      add("JSMap", JavaJsonMapDataType.TYPE);
      add("JSList", JavaJsonListDataType.TYPE);
      add(IRect.DEFAULT_INSTANCE);
      add(FPoint.DEFAULT_INSTANCE);
      add(FRect.DEFAULT_INSTANCE);
      add(Matrix.DEFAULT_INSTANCE);
      add(Polygon.DEFAULT_INSTANCE);
      break;
    }

  }

  /**
   * Get DataType associated with class name, or null if there isn't one
   */
  public DataType get(String key) {
    return mTypeMap.get(key);
  }

  public void add(String key, DataType dataType) {
    DataType previousMapping = mTypeMap.put(key, dataType);
    checkState(previousMapping == null, "duplicate data type for key:", key);
  }

  public void add(String key, DataType dataType, DefaultValueParser defaultValueParser) {
    add(key, dataType);
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

  public static DataType constructContractDataType() {
    switch (language()) {
    default:
      throw languageNotSupported();
    case JAVA:
      return new JavaContractDataType();
    case PYTHON:
      return new PythonContractDataType();
    case GO:
      return new GoContractDataType();
    }
  }

  /**
   * Construct a DataType for a class that implements the AbstractData
   * interface, and register it
   */
  private void add(AbstractData defaultInstance, DefaultValueParser parser) {
    DataType dataType = constructContractDataType();
    dataType.with(defaultInstance.getClass().getName());
    add(dataType.qualifiedClassName().className(), dataType, parser);
  }

  private void add(AbstractData defaultInstance) {
    add(defaultInstance, null);
  }

  private static final DefaultValueParser IPOINT_PARSER = new DefaultValueParser() {
    @Override
    public String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap js) {
      IPoint value = IPoint.get(js, "");
      String typeName = fieldDef.dataType().typeName();
      classSpecificSource.a("  private static final ", fieldDef.dataType().typeName(), " ",
          fieldDef.constantName(), "  = new ", typeName, "(", value.x, ", ", value.y, ");", CR);
      return fieldDef.constantName();
    }
  };

  public Map<String, DataType> debugMap() {
    return mTypeMap;
  }

  private final Map<String, DataType> mTypeMap;
  private final Map<String, DefaultValueParser> mDefaultValueParserMap;

}
