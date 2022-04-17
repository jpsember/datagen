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
package datagen.datatype;

import static js.base.Tools.*;

import datagen.DataType;
import datagen.FieldDef;
import datagen.ParseTools;
import datagen.PythonDataType;
import datagen.SourceBuilder;

public class PythonMapDataType extends PythonDataType {

  public PythonMapDataType(DataType wrappedKeyType, DataType wrappedValueType) {
    mWrappedKeyType = wrappedKeyType;
    mWrappedValueType = wrappedValueType;
    setQualifiedClassName(
        ParseTools.parseQualifiedName("java.util.Map<" + wrappedKeyType.qualifiedClassName().className() + ","
            + wrappedValueType.qualifiedClassName().className() + ">", null));
  }

  public DataType wrappedKeyType() {
    return mWrappedKeyType;
  }

  public DataType wrappedValueType() {
    return mWrappedValueType;
  }

  @Override
  protected String provideTypeName() {
    // We need to generate import statements for the wrapped types as well as the wrapper; but we don't want 
    // to confuse the macro substitution process 
    String wrappedImport1 = ParseTools.importCodeExpr(wrappedKeyType().qualifiedClassName().combined(), "");
    String wrappedImport2 = ParseTools.importCodeExpr(wrappedValueType().qualifiedClassName().combined(), "");
    String wrapperImport = ParseTools.PKG_MAP + "<" + wrappedKeyType().qualifiedClassName().className() + ","
        + wrappedValueType().qualifiedClassName().className() + ">";
    return wrappedImport1 + wrappedImport2 + wrapperImport;
  }

  @Override
  public String provideSourceDefaultValue() {
      return "{}";
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    notSupported("Maps not supported in Python (yet)");
  }

  /**
   * Constructs a mutable copy of a map. Note that while it creates a copy of
   * the map, it doesn't create copies of its elements; the references to those
   * elements are stored in the new map unchanged.
   */
  @Override
  public String sourceExpressionToMutable(String valueExpression) {
   throw notSupported("Maps not supported in Python (yet)");
  }

  @Override
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    notSupported("Maps not supported in Python (yet)");
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    notSupported("Maps not supported in Python (yet)");
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    notSupported("Maps not supported in Python (yet)");
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    notSupported("Maps not supported in Python (yet)");
  }

  private final DataType mWrappedKeyType;
  private final DataType mWrappedValueType;
}
