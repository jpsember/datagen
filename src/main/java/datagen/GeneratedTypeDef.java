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

import java.io.File;
import java.util.List;
import java.util.Set;

import static js.base.Tools.*;
import static datagen.ParseTools.*;
import static datagen.Utils.*;

import datagen.datatype.JavaListDataType;
import datagen.datatype.JavaMapDataType;
import datagen.datatype.JavaSetDataType;
import datagen.datatype.PythonListDataType;
import datagen.datatype.ContractDataType;
import datagen.datatype.EnumDataType;
import datagen.gen.PartialType;
import datagen.gen.QualifiedName;
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
    checkState(mDeprecated == null);
    mDeprecated = f;
  }

  public boolean isDeprecated() {
    return mDeprecated == Boolean.TRUE;
  }

  private Boolean mDeprecated;

  public String packageName() {
    return mPackageName;
  }

  public List<FieldDef> fields() {
    return mFields;
  }

  public boolean isEnum() {
    return mEnumDataType != null;
  }

  @Deprecated
  public boolean isOldStyle() {
    return !classMode();
  }

  public boolean classMode() {return mClassMode;
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
      if (complexType == null)
        complexType = python() ? new PythonListDataType(dataType) : new JavaListDataType(dataType);
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
    checkState(mClassSpecificSource == null, "source already retrieved");
    if (mClassSpecificSourceBuilder == null)
      mClassSpecificSourceBuilder = new SourceBuilder(language());
    return mClassSpecificSourceBuilder;
  }

  /**
   * Get the accumulated class-specific source
   */
  public String getClassSpecificSource() {
    if (mClassSpecificSource == null)
      mClassSpecificSource = classSpecificSourceBuilder().content();
    return mClassSpecificSource;
  }

  private DataType registerType(PartialType partialType) {
    DataTypeManager dataTypes = Context.dataTypeManager;
    DataType dataType = dataTypes.get(partialType.name());
    if (dataType == null) {
      QualifiedName className = ParseTools.parseQualifiedName(partialType.name(),
          Context.generatedTypeDef.packageName());
      dataType = dataTypes.get(className.className());
      if (dataType == null) {
        // If a package was specified, treat as if it was defined using 'extern'
        //
        if (!className.combined().equals(partialType.name())) {
          //
          // Verify that a .dat file exists matching this type, in the same directory as the one we're compiling
          //
          String datFilename = convertCamelToUnderscore(partialType.name())
              + ParseTools.DOT_EXT_DATA_DEFINITION;
          File currentDatFile = new File(Context.config.datPath(), Context.datWithSource.datRelPath());
          File datFile = new File(currentDatFile.getParentFile(), datFilename);
          if (!datFile.exists())
            badArg("No definition file found at", datFile, INDENT, "...use 'extern' to declare its location");
        }

        if (partialType.enumFlag()) {
          dataType = EnumDataType.construct();
        } else
          dataType = ContractDataType.construct();

        className = updateForPython(className);
        dataType.setQualifiedClassName(className);
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
