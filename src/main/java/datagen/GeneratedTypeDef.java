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

import datagen.datatype.ListDataType;
import datagen.datatype.MapDataType;
import datagen.datatype.DataContractDataType;
import datagen.datatype.EnumDataType;
import datagen.gen.QualifiedName;
import datagen.gen.TypeStructure;
import js.base.BaseObject;
import js.json.JSMap;

import static js.base.Tools.*;

/**
 * Encapsulates a generated data type or enum
 */
public final class GeneratedTypeDef extends BaseObject {

  public GeneratedTypeDef(String name, String packageName, EnumDataType enumTypeOrNull) {
    setName(name);
    mPackageName = nullToEmpty(packageName);
    mEnumDataType = enumTypeOrNull;
  }

  public String packageName() {
    return mPackageName;
  }

  public List<FieldDef> fields() {
    return mFields;
  }

  public boolean isEnum() {
    return mEnumDataType != null;
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
  public FieldDef addField(TypeStructure structure, String fieldName, String typeName, String type2Name,
      boolean optional) {
    log(structure, typeName, type2Name, fieldName, optional);
    boolean added = mFieldNames.add(fieldName);
    checkState(added, "duplicate field name: " + fieldName);

    DataType dataType = registerType(typeName);
    DataType dataType2 = null;
    if (!nullOrEmpty(type2Name))
      dataType2 = registerType(type2Name);

    if (optional)
      dataType = dataType.optionalVariant();
    DataType complexType;

    switch (structure) {
    case SCALAR:
      complexType = dataType;
      break;
    case LIST:
      // Convert list to particular scalar types in special cases
      complexType = dataType.listVariant();
      if (complexType == null)
        complexType = new ListDataType(dataType);
      break;
    case KEY_VALUE_MAP:
      complexType = new MapDataType(dataType, dataType2);
      break;
    default:
      throw notSupported("datatype structure", structure);
    }
    complexType.setUsedFlag();

    FieldDef f = new FieldDef(fieldName, complexType, optional);
    mFields.add(f);
    return f;
  }

  /**
   * Get buffer for writing class-specific source
   */
  public SourceBuilder classSpecificSourceBuilder() {
    checkState(mClassSpecificSource == null, "source already retrieved");
    if (mClassSpecificSourceBuilder == null)
      mClassSpecificSourceBuilder = new SourceBuilder(Context.config.language());
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

  private DataType registerType(String typeName) {
    DataTypeManager dataTypes = Context.dataTypeManager;
    DataType dataType = dataTypes.get(typeName);
    if (dataType == null) {
      QualifiedName className = ParseTools.parseQualifiedName(typeName,null);
      dataType = dataTypes.get(className.className());
      if (dataType == null) {
        {
          // Verify that a .dat file exists matching this type, in the same directory as the one we're compiling
          String datFilename = convertCamelToUnderscore(typeName) + ParseTools.DOT_EXT_DATA_DEFINITION;
          File currentDatFile = new File(Context.config.datPath(), Context.datWithSource.datRelPath());
          File datFile = new File(currentDatFile.getParentFile(), datFilename);
          if (!datFile.exists())
            badArg("No definition file found at", datFile, INDENT, "...use 'extern' to declare its location");
        }
        DataContractDataType contractType = new DataContractDataType();
        contractType.parseQualifiedName(typeName);
        dataType = contractType;
        dataTypes.add(dataType.qualifiedClassName().className(), dataType);
      }
    }
    dataType.setUsedFlag();
    return dataType;
  }

  private final String mPackageName;
  private final EnumDataType mEnumDataType;
  private final List<FieldDef> mFields = arrayList();
  private final Set<String> mFieldNames = hashSet();
  private SourceBuilder mClassSpecificSourceBuilder;
  private String mClassSpecificSource;
}
