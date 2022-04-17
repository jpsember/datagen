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

import datagen.FieldDef;
import datagen.ParseTools;
import datagen.SourceBuilder;
import static datagen.Utils.*;

public class FloatArrayDataType extends JavaDataContractDataType {

  @Override
  public String provideSourceDefaultValue() {
    return ParseTools.PKG_DATAUTIL + ".EMPTY_FLOAT_ARRAY";
  }

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "java.lang.float[]";
  }

  public String getSerializeDataType() {
    return ParseTools.PKG_JSLIST;
  }

  public String getSerializeToJSONValue(String value) {
    switch (language()) {
    default:
      throw languageNotSupported();
    case JAVA:
      return ParseTools.PKG_FLOAT_ARRAY + ".with(" + value + ").toJson()";
    }
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    throw notSupported();
  }

  @Override
  public String getConstructFromX() {
    switch (language()) {
    default:
      throw languageNotSupported();
    case JAVA:
      return ParseTools.PKG_FLOAT_ARRAY + ".DEFAULT_INSTANCE.parse(x).array()";
    }
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    switch (language()) {
    default:
      throw languageNotSupported();
    case JAVA:
      String defaultValue = f.defaultValueOrNull();
      if (defaultValue.equals("null"))
        s.a(targetExpr, " = x;");
      else
        s.a(targetExpr, " = ", "(x == null) ? ", defaultValue, " : x;");
      break;
    }
  }

  //------------------------------------------------------------------
  // Hashcode and Equals methods
  // ------------------------------------------------------------------

  @Override
  public void sourceGenerateEquals(SourceBuilder s, String a, String b) {
    s.a(ParseTools.PKG_ARRAYS, ".equals(", a, ", ", b, ")");
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    switch (language()) {
    default:
      throw languageNotSupported();
    case JAVA:
      s.a("r = r * 37 + ", ParseTools.PKG_ARRAYS, ".hashCode(", "m", f.sourceName(), ");");
      break;
    }
  }

}
