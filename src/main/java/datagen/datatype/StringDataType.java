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
import static datagen.SourceBuilder.*;

import datagen.DataType;
import datagen.FieldDef;
import datagen.SourceBuilder;
import datagen.parsing.Scanner;

public final class StringDataType extends DataType {

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "java.lang.String";
  }

  @Override
  public final String ourDefaultValue() {
    return "\"\"";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    return scanner.read(STRING).text();
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    if (python()) {
      s.a("inst._", f.javaName(), " = obj.get(", f.nameStringConstant(), ", ", f.defaultValueOrNull(), ")");
      return;
    }
    s.a("m", f.javaName(), " = m.opt(", f.nameStringConstant(), ", ");
    if (!f.optional())
      s.a(f.defaultValueOrNull());
    else
      s.a("(String) null");
    s.a(");", CR);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    if (python()) {
      s.a("t = obj.get(", f.nameStringConstant(), ", ", f.nullIfOptional("[]"), ")", CR);
      s.doIf(f.optional(), "if t is not None:", OPEN);
      s.a("inst._", f.javaName(), " = t.copy()", CR);
      s.endIf(CLOSE);
      return;
    }
    s.a("m", f.javaName(), " = ", PKG_DATAUTIL, ".parseListOfObjects(m.optJSList(", f.nameStringConstant(),
        "), ", f.optional(), ");", CR);
  }

  @Override
  public String deserializeJsonToJavaValue(String jsonValue) {
    return "(String) " + jsonValue;
  }

  @Override
  public String deserializeStringToJavaValue(String jsonStringValue) {
    return jsonStringValue;
  }

  public static final DataType SINGLETON = new StringDataType();

}
