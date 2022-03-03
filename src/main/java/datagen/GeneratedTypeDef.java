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

  public GeneratedTypeDef(Context context, String name) {
    mContext = context;
    mName = name;
  }

  public Context context() {
    return mContext;
  }

  @Override
  protected String supplyName() {
    return mName;
  }

  public void setEnum(EnumDataType dataType) {
    mEnumDataType = dataType;
  }

  public boolean isEnum() {
    return mEnumDataType != null;
  }

  public EnumDataType enumDataType() {
    checkState(isEnum());
    return mEnumDataType;
  }

  @Override
  public JSMap toJson() {
    JSMap m = super.toJson();
    checkState(!isEnum(), "not supported yet");
    for (FieldDef f : mFields) {
      m.put(f.name(), f.toJson());
    }
    if (mPackageName != null)
      m.put("java_package_name", mPackageName);
    return m;
  }

  private DataType registerType(String typeName) {
    DataTypeManager dataTypes = context().dataTypeManager;
    DataType dataType = dataTypes.get(typeName);
    if (dataType == null) {
      QualifiedName className = ParseTools.parseQualifiedName(typeName);
      if (!className.packagePath().isEmpty())
        throw badArg("Unexpected package in type name:", typeName);
      dataType = dataTypes.get(className.className());
      if (dataType == null) {
        {
          // Verify that a .dat file exists matching this type, in the same directory as the one we're compiling
          String datFilename = convertCamelToUnderscore(typeName) + ParseTools.DOT_EXT_DATA_DEFINITION;
          File currentDatFile = new File(context().config.datPath(), context().datWithSource.datRelPath());
          File datFile = new File(currentDatFile.getParentFile(), datFilename);
          if (!datFile.exists())
            badArg("No definition file found at", datFile, INDENT, "...use 'extern' to declare its location");
        }
        DataContractDataType contractType = new DataContractDataType();
        contractType.parseQualifiedName(mContext, typeName);
        dataType = contractType;
        dataTypes.add(dataType.qualifiedClassName().className(), dataType);
      }
    }
    dataType.setUsedFlag();
    return dataType;
  }

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
    case LIST: {
      // Convert list to particular scalar types in special cases
      DataType alternate = dataType.listVariant();
      if (alternate != null)
        complexType = alternate;
      else
        complexType = new ListDataType(dataType);
    }
      break;
    case KEY_VALUE_MAP:
      complexType = new MapDataType(dataType, dataType2);
      break;
    default:
      throw die("unsupported datatype structure: " + structure);
    }
    complexType.setUsedFlag();

    FieldDef f = new FieldDef(fieldName, complexType, dataType2, optional);
    mFields.add(f);
    return f;
  }

  public List<FieldDef> fields() {
    return mFields;
  }

  public void setPackageName(String n) {
    mPackageName = n;
  }

  public String packageName() {
    return nullToEmpty(mPackageName);
  }

  public SourceBuilder classSpecificSourceBuilder() {
    checkState(mClassSpecificSource == null, "source already retrieved");
    if (mClassSpecificSourceBuilder == null)
      mClassSpecificSourceBuilder = new SourceBuilder(context().language());
    return mClassSpecificSourceBuilder;
  }

  public String getClassSpecificSource() {
    if (mClassSpecificSource == null)
      mClassSpecificSource = classSpecificSourceBuilder().content();
    return mClassSpecificSource;
  }

  private final Context mContext;
  private final String mName;
  private String mPackageName;
  private EnumDataType mEnumDataType;
  private List<FieldDef> mFields = arrayList();
  private Set<String> mFieldNames = hashSet();
  private SourceBuilder mClassSpecificSourceBuilder;
  private String mClassSpecificSource;
}
