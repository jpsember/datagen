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

import datagen.FieldDef;
import datagen.ParseTools;
import datagen.PythonDataType;
import datagen.SourceBuilder;
import datagen.Utils;
import js.parsing.Scanner;

/**
 * For Python, this will generate python dicts, to the extent that they are
 * compatible with JSMaps. For example, keys have to be strings (unlike Python
 * dicts, whose keys can be any immutable type)
 */
public final class PythonJsonMapDataType extends PythonDataType {

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "js.json.JSMap";
  }

  @Override
  public final String provideSourceDefaultValue() {
    if (Utils.python())
      return "[]";
    return ParseTools.PKG_JSMAP + ".DEFAULT_INSTANCE";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    if (true)
      throw notSupported("Default values for Python dicts not supported yet");
    classSpecificSource.a("  private static final ", typeName(), " ", fieldDef.constantName(), " = new ", typeName(), "(",
        scanner.read(STRING).text(), ");", CR);
    return fieldDef.constantName();
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
      s.a("inst.", f.instanceName(), " = obj.get(", f.nameStringConstantQualified(), ", ",
         f.defaultValueOrNull(), ")", CR);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a(f.instanceName(), " = ", PKG_DATAUTIL, ".parseListOfObjects(m.optJSList(", f.nameStringConstantQualified(),
        "), ", f.optional(), ");", CR);
  }

}
