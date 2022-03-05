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

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.ParseTools;
import datagen.SourceBuilder;
import js.parsing.Scanner;

public class PyIPointDataType extends DataType {

  @Override
  public String ourDefaultValue() {
    todo("consider using decorators instead of having to have a bunch of classes...?");
    if (mDefValue == null) {
      pr("we are constructing an import expression for the Python data type");
      mDefValue = ParseTools.importExpression(context().constructImportExpression(qualifiedClassName()),
          qualifiedClassName().className() + ".default_instance");
      if (Context.WTF)
        pr("DataContractType, defValue set to:", mDefValue);
    }
    return mDefValue;
  }

  protected String provideQualifiedClassNameExpr() {
    return "pycore.ipoint.IPoint";
  }

  public String getConstructFromX() {
    return "x.build()";
  }

  public String getSerializeToJSONValue(String value) {
    return value + "." + "to_json(False)";
  }

  @Override
  public String parseDefaultValue(Scanner scanner, SourceBuilder sb, FieldDef fieldDef) {
    scanner.read(SQOP);
    int x = scanner.readInt(NUMBER);
    scanner.read(COMMA);
    int y = scanner.readInt(NUMBER);
    scanner.read(SQCL);
    String constName = "DEF" + fieldDef.nameStringConstant(false);
    sb.a(constName, "  = ", typeName(), ".with_x_y(", x, ", ", y, ")", CR);
    return constName;
  }

  private String mDefValue;

}
