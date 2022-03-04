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
import datagen.gen.QualifiedName;
import js.base.BaseObject;
import js.data.AbstractData;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.Matrix;
import js.geometry.Polygon;

import static datagen.ParseTools.*;
import static js.base.Tools.*;

/**
 * A map of class names to DataTypes
 */
public final class DataTypeManager extends BaseObject {

  public DataTypeManager(Context context) {
    mContext = context;
    mTypeMap = concurrentHashMap();
    mDefaultValueParserMap = concurrentHashMap();

    switch (context.language()) {
    default:
      throw notSupported();
    case PYTHON:
      add("byte", PrimitiveLongDataType.SINGLETON);
      add("short", PrimitiveLongDataType.SINGLETON);
      add("int", PrimitiveLongDataType.SINGLETON);
      add("long", PrimitiveLongDataType.SINGLETON);
      // There is no distinction between floats and doubles in Python; just use PrimitiveDoubleDataType
      add("float", PrimitiveDoubleDataType.SINGLETON);
      add("double", PrimitiveDoubleDataType.SINGLETON);
      add("File", StringDataType.SINGLETON);
      addPython("pycore.ipoint.IPoint");
      break;
    case JAVA:
      add("byte", PrimitiveByteDataType.SINGLETON);
      add("short", PrimitiveShortDataType.SINGLETON);
      add("int", PrimitiveIntegerDataType.SINGLETON);
      add("long", PrimitiveLongDataType.SINGLETON);
      add("float", PrimitiveFloatDataType.SINGLETON);
      add("double", PrimitiveDoubleDataType.SINGLETON);
      add("File", FileDataType.SINGLETON);
      add(IPoint.DEFAULT_INSTANCE, IPOINT_PARSER);
      break;
    }
    add("string", StringDataType.SINGLETON);
    add("bool", BooleanDataType.SINGLETON);
    add("JSMap", JsonMapDataType.SINGLETON);
    add("JSList", JsonListDataType.SINGLETON);

    todo("!avoid including IPoint and other variants if Python; instead, supply some other");
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

    // Store a reference to the context within this type, and any of its variants
    //
    dataType.setContext(mContext);
    if (dataType.optionalVariant() != dataType)
      dataType.optionalVariant().setContext(mContext);
    if (dataType.listVariant() != null)
      dataType.listVariant().setContext(mContext);
    if (defaultValueParser != null)
      mDefaultValueParserMap.put(dataType.typeName(), defaultValueParser);
  }

  public String unusedReferencesSummary() {
    StringBuilder sb = new StringBuilder();
    Map<String, DataType> sortedMap = treeMap();
    for (DataType t : mTypeMap.values())
      sortedMap.put(t.typeName(), t);
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

  /**
   * Add a DataContractDataType for Python that supports the abstract data type
   * contract
   */
  private void addPython(String qualifiedName) {
    DataContractDataType dataType = new DataContractDataType();
    QualifiedName qn = ParseTools.parseQualifiedName(qualifiedName);
    dataType.setQualifiedClassName(qn);
    add(qn.className(), dataType);
  }

  private void add(AbstractData defaultInstance) {
    add(defaultInstance, null);
  }

  private static final DefaultValueParser IPOINT_PARSER = (scanner, classSpecificSource, fieldDef) -> {
    todo("maybe use the contract data type for this?");
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
  };

  private final Context mContext;
  private final Map<String, DataType> mTypeMap;
  private final Map<String, DefaultValueParser> mDefaultValueParserMap;

}
