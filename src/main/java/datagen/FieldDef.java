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

import js.base.BaseObject;

import static js.base.Tools.*;
import static datagen.Context.*;

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
      case GO:
        return new GoFieldDef();
      case RUST:
        return new RustFieldDef();
    }
  }

  public FieldDef() {
    loadTools();
  }

  public final int index() {
    return mIndex;
  }

  public final void init(String name, DataType dataType, boolean deprecated, int index) {
    setName(name);
    mIndex = index;
    mDataType = dataType;
    mDeprecated = deprecated;
  }

  public abstract String provideNameStringConstantUnqualified();

  public abstract String provideNameStringConstantQualified();

  public final DataType dataType() {
    return mDataType;
  }

  public final boolean deprecated() {
    return mDeprecated;
  }

  // ------------------------------------------------------------------
  // Names to appear in source
  // ------------------------------------------------------------------

  public abstract String provideInstanceName();

  public String instanceName() {
    if (mCachedInstanceName == null)
      mCachedInstanceName = provideInstanceName();
    return mCachedInstanceName;
  }

  public abstract String provideSetterName();

  public final String setterName() {
    if (mCachedSetterName == null)
      mCachedSetterName = provideSetterName();
    return mCachedSetterName;
  }

  public abstract String provideGetterName();

  public final String getterName() {
    if (mCachedGetterName == null)
      mCachedGetterName = provideGetterName();
    return mCachedGetterName;
  }

  public abstract String provideConstantName();

  public final String constantName() {
    if (mCachedConstantName == null)
      mCachedConstantName = provideConstantName();
    return mCachedConstantName;
  }

  /**
   * Returns name assigned to the string constant for the field, e.g.
   * "HORSE_WEIGHT"
   */
  public final String nameStringConstantQualified() {
    if (mCachedNameStringQualified == null)
      mCachedNameStringQualified = provideNameStringConstantQualified();
    return mCachedNameStringQualified;
  }

  public final String nameStringConstantUnqualified() {
    if (mCachedNameString == null)
      mCachedNameString = provideNameStringConstantUnqualified();
    return mCachedNameString;
  }

  // ------------------------------------------------------------------

  public void setDefaultValue(String defValueSource) {
    mCachedDefaultValueSource = defValueSource;
  }

  public void setDefaultValueForBuilder(String defValueSource) {
    mCachedDefaultValueSourceForBuilder = defValueSource;
  }

  public String defaultValueSource() {
    if (mCachedDefaultValueSource == null)
      setDefaultValue(dataType().sourceDefaultValue());
    return mCachedDefaultValueSource;
  }

  public final String defaultValueSourceForBuilder() {
    if (mCachedDefaultValueSourceForBuilder == null)
      setDefaultValueForBuilder(dataType().sourceDefaultValueForBuilder());
    return mCachedDefaultValueSourceForBuilder;
  }

  private String mCachedNameString;
  private String mCachedNameStringQualified;
  private int mIndex;
  private DataType mDataType;
  private boolean mDeprecated;
  private String mCachedDefaultValueSource;
  private String mCachedDefaultValueSourceForBuilder;
  private String mCachedInstanceName;
  private String mCachedGetterName;
  private String mCachedSetterName;
  private String mCachedConstantName;

}
