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

import java.util.List;
import java.util.Set;

import static js.base.Tools.*;
import static datagen.Utils.*;

import datagen.datatype.JavaListDataType;
import datagen.datatype.JavaMapDataType;
import datagen.datatype.JavaSetDataType;
import datagen.datatype.PythonListDataType;
import datagen.datatype.EnumDataType;
import datagen.datatype.GoListDataType;
import datagen.gen.PartialType;
import datagen.gen.TypeStructure;
import js.base.BaseObject;
import js.json.JSMap;

/**
 * Encapsulates a generated data type or enum
 */
public final class GeneratedTypeDef extends BaseObject {

  public GeneratedTypeDef(String name, String packageName, DataType enumTypeOrNull, boolean classMode) {
    setName(name);
    mPackageName = nullToEmpty(packageName);
    mEnumDataType = (EnumDataType) enumTypeOrNull;
    mClassMode = classMode;
  }

  public void setDeprecated(boolean f) {
    mDeprecated = f;
  }

  public void setUnsafe(boolean f) {
    mUnsafe = f;
  }

  public boolean isDeprecated() {
    return mDeprecated;
  }

  public boolean isUnsafe() {
    return mUnsafe;
  }

  private boolean mDeprecated;
  private boolean mUnsafe;

  public String packageName() {
    return mPackageName;
  }

  public List<FieldDef> fields() {
    return mFields;
  }

  public boolean isEnum() {
    return mEnumDataType != null;
  }

  public boolean classMode() {
    return mClassMode;
  }

  /**
   * Get EnumDataType for this type (must be an enum)
   */
  public EnumDataType enumDataType() {
    checkState(isEnum());
    return mEnumDataType;
  }

  @Override
  public JSMap toJson() {
    JSMap m = super.toJson();
    checkState(!isEnum(), "not supported yet");
    for (FieldDef f : mFields)
      m.put(f.name(), f.toJson());
    if (nonEmpty(mPackageName))
      m.put("java_package_name", mPackageName);
    return m;
  }

  /**
   * Add a field to this data type
   */
  public FieldDef addField(boolean deprecated, boolean optional, TypeStructure structure,
      PartialType primaryType, PartialType auxType, String fieldName) {
    if (verbose())
      log(structure, fieldName, logStr(primaryType), logStr(auxType), optional);
    boolean added = mFieldNames.add(fieldName);
    checkState(added, "duplicate field name: " + fieldName);

    DataType dataType = registerType(primaryType);
    DataType dataType2 = null;
    if (auxType != null)
      dataType2 = registerType(auxType);

    if (structure == TypeStructure.KEY_VALUE_MAP || structure == TypeStructure.VALUE_SET) {
      dataType = dataType.getOptionalVariant();
      if (dataType2 != null)
        dataType2 = dataType2.getOptionalVariant();
    }

    if (optional)
      dataType = dataType.getOptionalVariant();
    DataType complexType;

    switch (structure) {
    case SCALAR:
      complexType = dataType;
      break;
    case LIST:
      // Convert list to particular scalar types in special cases
      complexType = dataType.getListVariant();
      if (complexType == null) {
        switch (language()) {
        default:
          throw notSupported();
        case JAVA:
          complexType = new JavaListDataType(dataType);
          break;
        case PYTHON:
          complexType = new PythonListDataType(dataType);
          break;
        case GO:
          complexType = new GoListDataType(dataType);
          break;
        }
      }
      break;
    case KEY_VALUE_MAP: {
      switch (language()) {
      default:
        throw languageNotSupported();
      case JAVA:
        complexType = new JavaMapDataType((JavaDataType) dataType, (JavaDataType) dataType2);
        break;
      }
    }
      break;
    case VALUE_SET: {
      switch (language()) {
      default:
        throw languageNotSupported();
      case JAVA:
        complexType = new JavaSetDataType((JavaDataType) dataType);
        break;
      }
    }
      break;
    default:
      throw notSupported("datatype structure", structure);
    }
    complexType.setUsedFlag();
    FieldDef f = FieldDef.construct();
    f.init(fieldName, complexType, optional, deprecated, mFields.size());
    mFields.add(f);
    return f;
  }

  /**
   * Helper method for logging
   */
  private static String logStr(PartialType p) {
    if (p == null)
      return "";
    String suffix = (p.enumFlag()) ? "(enum)" : "";
    return p.name() + suffix;
  }

  /**
   * Get buffer for writing class-specific source
   */
  public SourceBuilder classSpecificSourceBuilder() {
    if (mClassSpecificSourceBuilder == null) {
      checkState(mClassSpecificSource == null, "source already retrieved");
      mClassSpecificSourceBuilder = new SourceBuilder(language());
    }
    return mClassSpecificSourceBuilder;
  }

  /**
   * Get the accumulated class-specific source
   */
  public String getClassSpecificSource() {
    if (mClassSpecificSource == null) {
      mClassSpecificSource = chomp(classSpecificSourceBuilder().getContent());
      mClassSpecificSourceBuilder = null;
    }
    return mClassSpecificSource;
  }

  private DataType registerType(PartialType partialType) {
    DataTypeManager dataTypes = Context.dataTypeManager;
    DataType dataType = dataTypes.get(partialType.name());
    if (dataType == null) {
      QualifiedName qualName = QualifiedName.parse(partialType.name(),
          Context.generatedTypeDef.packageName());
      dataType = dataTypes.get(qualName.className());
      if (dataType == null) {

        if (partialType.enumFlag()) {
          dataType = EnumDataType.construct();
        } else
          dataType = DataTypeManager.constructContractDataType();

        dataType.withQualifiedName(qualName);
        dataTypes.add(dataType.qualifiedClassName().className(), dataType);
      }

      if (partialType.enumFlag())
        checkState(dataType instanceof EnumDataType, "enum used with non-enum type");
    }
    dataType.setUsedFlag();
    return dataType;
  }

  private final String mPackageName;
  private final EnumDataType mEnumDataType;
  private final List<FieldDef> mFields = arrayList();
  private final Set<String> mFieldNames = hashSet();
  private final boolean mClassMode;
  private SourceBuilder mClassSpecificSourceBuilder;
  private String mClassSpecificSource;
}
