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
import js.parsing.Scanner;

/**
 * Python IPoints are represented as a list [x,y].
 * 
 * For simplicity, we will assume each such IPoint is immutable; so we won't
 * make defensive copies. We *will* still make defensive copies of lists of
 * IPoints.
 *
 */
public final class PythonIPointDataType extends DataType {

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "python.IPoint";
  }

  @Override
  public final String ourDefaultValue() {
    return "[0, 0]";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    scanner.read(BROP);
    int x = scanner.readInt(NUMBER);
    scanner.read(COMMA);
    int y = scanner.readInt(NUMBER);
    scanner.read(BRCL);
    return String.format("[%d, %d]", x, y);
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a("t = obj.get(", f.nameStringConstant(), ", ", f.defaultValueOrNull(), ")", CR);
    s.doIf(f.optional(), "if t is not None:", OPEN);
    s.a("inst._", f.javaName(), " = t.copy()", CR);
    s.endIf(CLOSE);
  }

  public static final DataType SINGLETON = new PythonIPointDataType();

}
