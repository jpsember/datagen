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

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import datagen.DataType;
import datagen.FieldDef;
import datagen.ParseTools;
import datagen.SourceBuilder;
import js.data.DataUtil;

/**
 * DataType that wraps objects that implement the DataType interface
 */
public class DataContractDataType extends DataType {

  @Override
  public String ourDefaultValue() {
    if (mDefValue == null) {
      if (python()) {
        String filename = DataUtil.convertCamelCaseToUnderscores(qualifiedClassName().className());
        mDefValue = ParseTools
            .importExpression(
                "from " + qualifiedClassName().packagePath() + "." + filename + " import "
                    + qualifiedClassName().className(),
                qualifiedClassName().className() + ".default_instance");
        if (todo("!IPoint python impl generates bad data"))
          checkState(!mDefValue.contains("IPoint"));
      } else {
        mDefValue = ParseTools.importExpression(qualifiedClassName().combined(),
            qualifiedClassName().className()) + ".DEFAULT_INSTANCE";
      }
    }
    return mDefValue;
  }

  protected String mDefValue;

  public String getConstructFromX() {
    if (python()) {
      todo("!this won't work for non-primitive data types");
      return "x.copy()";
    } else
      return ourDefaultValue() + ".parse(x)";
  }

  public String getSerializeDataType() {
    return "Object";
  }

  public String getSerializeToJSONValue(String value) {
    if (python())
      return value + ".to_dict()";
    else
      return value + ".toJson()";
  }

  // Make this final for now to avoid unintended overriding
  @Override
  public final void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    if (python()) {
      s.a("x = obj.get(", f.nameStringConstant(), ", ", f.defaultValueOrNull(), ")", CR);
      if (f.optional()) {
        s.a("if x is not None:", IN);
      }
      s.a("inst._", f.javaName(), " = ", getConstructFromX(), CR);
      if (f.optional()) {
        s.a(OUT);
      }
      return;
    }

    s.open();
    if (!f.optional())
      s.a("m", f.javaName(), " = ", f.defaultValueOrNull(), ";", CR);

    String typeExpr = getSerializeDataType();
    if (typeExpr.equals(ParseTools.PKG_JSMAP)) {
      s.a(ParseTools.PKG_JSMAP, " x = m.optJSMap(", f.nameStringConstant(), ");", CR);
    } else if (typeExpr.equals(ParseTools.PKG_JSLIST)) {
      s.a(ParseTools.PKG_JSLIST, " x = m.optJSList(", f.nameStringConstant(), ");", CR);
    } else {
      String castExpr = "m.optUnsafe";
      if (!typeExpr.equals("Object"))
        castExpr = "(" + typeExpr + ") " + castExpr;
      s.a(getSerializeDataType(), " x = ", castExpr, "(", f.nameStringConstant(), ");", CR);
    }
    s.a("if (x != null)", OPEN, //
        "m", f.javaName(), " = ", getConstructFromX(), ";", CLOSE //
    );

    s.close();
  }

  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    if (python()) {
      super.sourceDeserializeFromList(s, f);
      return;
    }
    s.a("m", f.javaName(), " = ", ParseTools.PKG_DATAUTIL, ".parseListOfObjects(", typeName(),
        ".DEFAULT_INSTANCE, m.optJSList(", f.nameStringConstant(), "), ", f.optional(), ");");
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return getSerializeToJSONValue(valueExpression);
  }

  @Override
  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    if (python()) {
      if (!f.optional()) {
        s.a("if x is None:", OPEN);
        s.a("x = ", f.defaultValueOrNull(), CLOSE);
        s.a(targetExpr, " = x.build()");
      } else {
        s.a("if x is not None:", OPEN);
        s.a("x = x.build()", CLOSE);
        s.a(targetExpr, " = x");
      }
      return;
    }
    String defaultValue = f.defaultValueOrNull();
    s.a(targetExpr, " = ", "(x == null) ? ", defaultValue, " : x.build();");
  }

  @Override
  public String deserializeJsonToJavaValue(String jsonValue) {
    return ourDefaultValue() + ".parse((JSMap) " + jsonValue + ")";
  }

}
