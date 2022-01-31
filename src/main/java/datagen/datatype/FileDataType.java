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

public final class FileDataType extends DataType {

  private FileDataType() {
  }

  @Override
  protected String provideQualifiedClassNameExpr() {
    return "java.io.File";
  }

  @Override
  public final String ourDefaultValue() {
    return PKG_FILES + ".DEFAULT";
  }

  @Override
  public final String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource,
      FieldDef fieldDef) {
    String constName = "DEF_" + fieldDef.nameStringConstant();
    classSpecificSource.a("  private static final ", typeName(), " ", constName, " = new File(",
        scanner.read(STRING).text(), ");", CR);
    return constName;
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.open();
    if (!f.optional())
      s.a("m", f.javaName(), " = ", f.defaultValueOrNull(), ";", CR);
    s.a("String x = m.opt(", f.nameStringConstant(), ", (String) null);", CR, //
        "if (x != null)", OPEN, //
        "m", f.javaName(), " = new ", typeName(), "(x);", //
        CLOSE //
    );
    s.close();
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return valueExpression + ".toString()";
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    s.a(OPEN, //
        PKG_LIST, "<", typeName(), "> result = ", f.nullIfOptional(PKG_MUTABLELIST), ";", CR, //
        ParseTools.PKG_JSLIST, " j = m.optJSList(", f.nameStringConstant(), ");", CR, //
        "if (j != null)", OPEN, //
        "result = new ", ParseTools.PKG_ARRAYLIST, "<>(j.size());", CR, //
        "for (Object z : j.wrappedList())", OPEN, //
        typeName(), " y = ", ourDefaultValue(), ";", CR, //
        "if (z != null)", OPEN, //
        getSerializeDataType(), " x = (", getSerializeDataType(), ") z;", CR, //
        "y = ", getConstructFromX(), ";", CLOSE, //
        "result.add(y);", CLOSE, //
        CLOSE, //
        "m", f.javaName(), " = ", ParseTools.immutableCopyOfList("result"), ";", //
        CLOSE);
  }

  private String getSerializeDataType() {
    return "String";
  }

  private String getConstructFromX() {
    return "new " + typeName() + "(x)";
  }

  @Override
  public String deserializeJsonToJavaValue(String jsonValue) {
    return "new " + typeName() + "((String) " + jsonValue + ")";
  }

  public static final DataType SINGLETON = new FileDataType();

}
