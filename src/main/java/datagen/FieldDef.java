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

import static datagen.Utils.*;
import static js.base.Tools.*;

/**
 * Represents a field within a generated data type
 */
public abstract class FieldDef extends BaseObject {

  public static FieldDef construct() {
    switch (language()) {
    default:
      throw languageNotSupported();
    case JAVA:
      return new JavaFieldDef();
    case PYTHON:
      return new PythonFieldDef();
    }
  }

  public FieldDef() {
    loadTools();

  }

  public final int index() {
    return mIndex;
  }

  public final void init(String name, DataType dataType, boolean optional, int index) {
    setName(name);
    mIndex = index;
    mDataType = dataType;
    mOptional = optional;
  }

  protected abstract String provideNameStringConstantUnqualified();

  protected abstract String provideNameStringConstantQualified();

  public final DataType dataType() {
    return mDataType;
  }

  public final boolean optional() {
    return mOptional;
  }

  protected abstract String provideSourceName();

  protected abstract String provideSourceNameLowerFirst();

  public String sourceName() {
    if (mSourceName == null) {
      mSourceNameCapFirst = provideSourceName();
      mSourceName = provideSourceNameLowerFirst();
    }
    return mSourceNameCapFirst;
  }

  public String sourceNameLowerFirst() {
    todo("have better names for sourceName, sourceNameLowerFirst, and providers");
    sourceName();
    return mSourceName;
  }

  /**
   * Returns name assigned to the string constant for the field, e.g.
   * "HORSE_WEIGHT"
   */
  public final String nameStringConstantQualified() {
    if (mNameStringConstantQualified == null)
      mNameStringConstantQualified = provideNameStringConstantQualified();
    return mNameStringConstantQualified;
  }

  public final String nameStringConstantUnqualified() {
    if (mNameStringConstant == null)
      mNameStringConstant = provideNameStringConstantUnqualified();
    return mNameStringConstant;
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
    return Utils.nullExpr();
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
      setDefaultValue(dataType().sourceDefaultValue());
    return mDefaultValueSource;
  }

  private String mNameStringConstant;
  private String mNameStringConstantQualified;
  private int mIndex;
  private DataType mDataType;
  private boolean mOptional;
  private String mSourceName;
  private String mSourceNameCapFirst;
  private String mDefaultValueSource;

}
