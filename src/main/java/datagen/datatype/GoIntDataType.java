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
import datagen.GoDataType;
import datagen.SourceBuilder;
import datagen.gen.TypeStructure;
import js.data.DataUtil;
import js.json.JSMap;

public final class GoIntDataType extends GoDataType {

  public DataType modifyTypeFilter(TypeStructure structure) {
    DataType type = this;
    if (structure == TypeStructure.SCALAR && mBits == 32) {
      type = GENERIC_INT;
    }
    return type;
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public String compilerInitialValue() {
    return "0";
  }

  @Override
  public DataType listVariant() {
    if (mBits == 8) {
      return GoByteArrayDataType.TYPE;
    }
    return super.listVariant();
  }

  private static GoIntDataType GENERIC_INT = new GoIntDataType(64, true);

  public GoIntDataType(int nbits) {
    this(nbits, false);
  }

  private GoIntDataType(int nbits, boolean generic) {
    switch (nbits) {
    case 8:
      with("byte");
      break;
    case 16:
    case 32:
    case 64:
      if (generic) {
        with("int");
      } else {
        with("int" + nbits);
      }
      break;
    default:
      throw badArg("nbits:", nbits);
    }
    mBits = nbits;
  }

  private final int mBits;

  @Override
  public final String provideSourceDefaultValue() {
    return "0";
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap map) {
    return Integer.toString(map.getInt(""));
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = s.Opt", DataUtil.capitalizeFirst(qualifiedName().className()), "(",
        f.nameStringConstantQualified(), ", ", f.defaultValueSource(), ")", CR);
  }

  @Override
  protected String parseElementFromJsonValue(FieldDef f, String jsentityExpression) {
    String expr = jsentityExpression + ".ToInteger()";
    if (mBits != 64) {
      expr = "int" + mBits + "(" + expr + ")";
    }
    return expr;
  }

  public String sqlType() {
    return "INTEGER";
  }
}
