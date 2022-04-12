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
import js.data.DataUtil;

import static js.base.Tools.*;

/**
 * Represents a field within a generated data type
 */
public final class FieldDef extends BaseObject {

  public FieldDef(String name, DataType dataType,   boolean optional) {
    loadTools();
    mName = name;
    mDataType = dataType;
    mOptional = optional;
    if (dataType.python()) {
      // If we add a '_' prefix, the Python inspection reports a warning about a 'protected member';
      // but that is ok for our code; we need to have underscore prefixes for the instance fields anyways
      mNameStringConstant = "_key_" + javaName();
      mNameStringConstantQualified = Context.generatedTypeDef.name() + "." + mNameStringConstant;
    } else {
      mNameStringConstant = name.toUpperCase();
      mNameStringConstantQualified = mNameStringConstant;
    }
  }

  @Override
  protected String supplyName() {
    return mName;
  }

  public DataType dataType() {
    return mDataType;
  }

  public boolean optional() {
    return mOptional;
  }

  public String javaName() {
    if (mJavaName == null) {
      if (dataType().python()) {
        mJavaNameCapFirst = mJavaName = DataUtil.lowerFirst(mName);
      } else {
        mJavaNameCapFirst = DataUtil.convertUnderscoresToCamelCase(mName);
        mJavaName = DataUtil.lowerFirst(mJavaNameCapFirst);
      }
    }
    return mJavaNameCapFirst;
  }

  public String javaNameLowerFirst() {
    javaName();
    return mJavaName;
  }

  /**
   * Returns name assigned to the Java string constant for the field, e.g.
   * "HORSE_WEIGHT"
   */
  public final String nameStringConstant() {
    return nameStringConstant(true);
  }

  public final String nameStringConstant(boolean qualified) {
    return qualified ? mNameStringConstantQualified : mNameStringConstant;
  }

  public void setDefaultValue(String defValueSource) {
    mDefaultValueSource = defValueSource;
  }

  /**
   * If field is optional, return 'null' (or 'None'); else, the provided
   * expression
   */
  public String nullIfOptional(String nonNullExpr) {
    if (!optional())
      return nonNullExpr;
    return dataType().nullExpr();
  }

  /**
   * Return field's default value, or 'null' (or 'None') if the field is
   * optional
   */
  public String defaultValueOrNull() {
    return nullIfOptional(defaultValueSource());
  }

  // final for the time being
  public final String defaultValueSource() {
    if (mDefaultValueSource == null)
      setDefaultValue(dataType().ourDefaultValue());
    return mDefaultValueSource;
  }

  private final String mName;
  private final String mNameStringConstant;
  private final String mNameStringConstantQualified;
  private final DataType mDataType;
  private final boolean mOptional;
  private String mJavaName;
  private String mJavaNameCapFirst;
  private String mDefaultValueSource;

}
