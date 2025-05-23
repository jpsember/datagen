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

import java.io.File;
import java.util.List;
import java.util.Set;

import static js.base.Tools.*;
import static datagen.Context.*;

import datagen.datatype.*;
import datagen.gen.PartialType;
import datagen.gen.TypeStructure;
import js.base.BaseObject;
import js.file.Files;

/**
 * Encapsulates a generated data type or enum
 */
public final class GeneratedTypeDef extends BaseObject {

  public GeneratedTypeDef(String name, String packageName, DataType enumTypeOrNull) {
    mEnumDataType = (EnumDataType) enumTypeOrNull;

    DataType dataType;

    switch (language()) {
      case GO:
        dataType = new GoContractDataType();
        break;
      case JAVA:
        dataType = new JavaContractDataType();
        break;
      case PYTHON:
        dataType = new PythonContractDataType();
        break;
      case RUST:
        dataType = new RustContractDataType();
        break;
      default:
        throw notFinished();
    }

    QualifiedName qn = QualifiedName.parse(packageName + "." + name);
    dataType.withQualifiedName(qn);
    mDataType = dataType;
    setName(dataType.typeName());
  }

  public void setDeprecated(boolean f) {
    mDeprecated = f;
  }

  public boolean isDeprecated() {
    return mDeprecated;
  }

  public QualifiedName qualifiedName() {
    return mDataType.qualifiedName();
  }

  public DataType wrappedType() {
    return mDataType;
  }

  private DataType mDataType;
  private boolean mDeprecated;

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

  /**
   * Add a field to this data type
   */
  public FieldDef addField(boolean deprecated, boolean unusedOptional, TypeStructure structure,
                           PartialType primaryType, PartialType auxType, String fieldName) {
    if (verbose())
      log(structure, fieldName, logStr(primaryType), logStr(auxType));
    boolean added = mFieldNames.add(fieldName);
    checkState(added, "duplicate field name: " + fieldName);

    // Allow the golang code to replace 'int64' with 'int' in certain situations:
    DataType dataType = registerType(primaryType).modifyTypeFilter(structure);
    DataType dataType2 = null;
    if (auxType != null)
      dataType2 = registerType(auxType).modifyTypeFilter(structure);

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
              throw notSupported("lists not supported for this language");
            case JAVA:
              complexType = new JavaListDataType(dataType);
              break;
            case PYTHON:
              complexType = new PythonListDataType(dataType);
              break;
            case GO:
              complexType = new GoListDataType(dataType);
              break;
            case RUST:
              complexType = new RustListDataType(dataType);
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
          case GO:
            complexType = new GoMapDataType((GoDataType) dataType, (GoDataType) dataType2);
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
    f.init(fieldName, complexType, deprecated, mFields.size());
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
      mClassSpecificSourceBuilder = new SourceBuilder();
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

  public FieldDef fieldWithName(String name) {
    for (FieldDef field : fields()) {
      if (field.name().equals(name))
        return field;
    }
    throw badArg("can't find field with name:", name);
  }

  private DataType registerType(PartialType partialType) {
    DataTypeManager dataTypes = Context.dataTypeManager();
    DataType dataType = dataTypes.get(partialType.name());
    if (dataType == null) {
      var defaultPackage = Context.generatedTypeDef.qualifiedName().packagePath();

      // Issue #45: 
      //
      // With Python,
      //
      // Remove the last element of the package, e.g., "gen.layer" => "gen"
      // (I'm not sure this is the permanant solution, but until some related problem occurs, I'll go with it)
      // but DON'T do this if the 'alternate source path' parameter is in effect.
      // 
      if (python() && Files.empty(Context.config.pythonSourcePath())) {
        int i = defaultPackage.lastIndexOf('.');
        if (i >= 0)
          defaultPackage = defaultPackage.substring(0, i);
      }

      QualifiedName qualName = QualifiedName.parse(partialType.name(), defaultPackage);
      dataType = dataTypes.get(qualName.className());
      if (dataType == null) {
        if (partialType.enumFlag()) {
          dataType = EnumDataType.construct();
        } else
          dataType = DataTypeManager.constructContractDataType();
        dataType.withQualifiedName(qualName);
        dataTypes.add(dataType.qualifiedName().className(), dataType);
      }

      if (partialType.enumFlag())
        checkState(dataType instanceof EnumDataType, "enum used with non-enum type");
    }
    dataType.setUsedFlag();
    return dataType;
  }

  public void setSourceFile(File f) {
    mSourceFile = f;
  }

  public File sourceFile() {
    return mSourceFile;
  }

  private final EnumDataType mEnumDataType;
  private final List<FieldDef> mFields = arrayList();
  private final Set<String> mFieldNames = hashSet();
  private SourceBuilder mClassSpecificSourceBuilder;
  private String mClassSpecificSource;
  private File mSourceFile;
}
