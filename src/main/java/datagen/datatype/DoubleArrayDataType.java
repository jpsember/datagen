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

import static datagen.ParseTools.*;
import static js.base.Tools.*;

import java.util.List;

import datagen.FieldDef;
import datagen.ParseTools;
import datagen.SourceBuilder;
import js.parsing.Scanner;

public class DoubleArrayDataType extends JavaDataContractDataType {

  @Override
  public String provideSourceDefaultValue() {
    return ParseTools.PKG_DATAUTIL + ".EMPTY_DOUBLE_ARRAY";
  }

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "java.lang.double[]";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    List<String> parsedNumbers = arrayList();

    {
      scanner.read(SQOP);
      for (int index = 0;; index++) {
        if (scanner.readIf(SQCL) != null)
          break;
        if (index > 0) {
          scanner.read(COMMA);
          // Allow an extraneous trailing comma
          if (scanner.readIf(SQCL) != null)
            break;
        }
        parsedNumbers.add(scanner.read(NUMBER).text());
      }
    }

    SourceBuilder sb = classSpecificSource;
    String constName = "DEF_" + fieldDef.nameStringConstant();
    sb.a("  private static final ", typeName(), " ", constName, " = ");
    sb.a("{");
    int index = INIT_INDEX;
    for (String numberText : parsedNumbers) {
      index++;
      if (index > 0) {
        sb.a(",");
      }
      sb.a(numberText);
    }
    sb.a("};").cr();

    return constName;
  }

  public String getSerializeDataType() {
    return ParseTools.PKG_JSLIST;
  }

  public String getSerializeToJSONValue(String value) {
    return ParseTools.PKG_DOUBLE_ARRAY + ".with(" + value + ").toJson()";
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    throw notSupported();
  }

  @Override
  public String getConstructFromX() {
    return ParseTools.PKG_DOUBLE_ARRAY + ".DEFAULT_INSTANCE.parse(x).array()";
  }

  @Override
  public String pythonDeserializeExpr(FieldDef f, String expr) {
    return expr + ".copy()";
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    String defaultValue = f.defaultValueOrNull();
    if (defaultValue.equals("null"))
      s.a(targetExpr, " = x;");
    else
      s.a(targetExpr, " = ", "(x == null) ? ", defaultValue, " : x;");
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
    s.a("r = r * 37 + ", ParseTools.PKG_ARRAYS, ".hashCode(", "m", f.sourceName(), ");");
  }

}
