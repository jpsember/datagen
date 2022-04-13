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
import js.parsing.Scanner;

/**
 * For Python, this will generate python dicts, to the extent that they are
 * compatible with JSMaps. For example, keys have to be strings (unlike Python
 * dicts, whose keys can be any immutable type)
 */
public final class JsonMapDataType extends DataType {

  @Override
  protected String provideQualifiedClassNameExpr() {
    todo("!Is this needed when generating Python code?");
    return "js.json.JSMap";
  }

  @Override
  public final String ourDefaultValue() {
    if (python())
      return "[]";
    return ParseTools.PKG_JSMAP + ".DEFAULT_INSTANCE";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    if (python())
      throw notSupported("Default values for Python dicts not supported yet");
    String constName = "DEF_" + fieldDef.nameStringConstant();
    classSpecificSource.a("  private static final ", typeName(), " ", constName, " = new ", typeName(), "(",
        scanner.read(STRING).text(), ");", CR);
    return constName;
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    if (python()) {
      s.a("inst._", f.sourceName(), " = obj.get(", f.nameStringConstant(), ", ",
         f.defaultValueOrNull(), ")", CR);
      return;
    }

    s.open();
    if (!f.optional())
      s.a("m", f.sourceName(), " = ", f.defaultValueOrNull(), ";", CR);
    s.a(typeName(), " x = m.optJSMap(", f.nameStringConstant(), ");", CR, //
        "if (x != null)", OPEN, //
        "m", f.sourceName(), " = x.lock();", //
        CLOSE //
    );
    s.close();
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("m", f.sourceName(), " = ", PKG_DATAUTIL, ".parseListOfObjects(m.optJSList(", f.nameStringConstant(),
        "), ", f.optional(), ");", CR);
  }

}
