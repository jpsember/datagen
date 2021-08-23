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
import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import datagen.DataType;
import datagen.FieldDef;
import datagen.ParseTools;
import datagen.SourceBuilder;
import datagen.parsing.Scanner;

public class JsonListDataType extends DataType {

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "js.json.JSList";
  }

  @Override
  public final String ourDefaultValue() {
    return ParseTools.PKG_JSLIST + ".DEFAULT_INSTANCE";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    String constName = "DEF_" + fieldDef.nameStringConstant();
    classSpecificSource.a("  private static final ", typeName(), " ", constName, " = new ", typeName(), "(",
        scanner.read(STRING).text(), ");", CR);
    return constName;
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.open();
    if (!f.optional())
      s.a("m", f.javaName(), " = ", f.defaultValueOrNull(), ";", CR);
    s.a(typeName(), " x = m.optJSList(", f.nameStringConstant(), ");", CR, //
        "if (x != null)", OPEN, //
        "m", f.javaName(), " = x.lock();", //
        CLOSE //
    );
    s.close();
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("m", f.javaName(), " = ", PKG_DATAUTIL, ".parseListOfObjects(m.optJSList(", f.nameStringConstant(),
        "), ", f.optional(), ");", CR);
  }

  private JsonListDataType() {
  }

  public static final DataType SINGLETON = new JsonListDataType();

}
