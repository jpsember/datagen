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

import datagen.Context;
import datagen.DataType;
import datagen.FieldDef;
import datagen.GoDataType;
import datagen.SourceBuilder;
import js.data.DataUtil;
import js.json.JSMap;
import static js.base.Tools.*;

public final class GoFileDataType extends GoDataType {

  public static final DataType TYPE = new GoFileDataType();

  private GoFileDataType() {
    loadTools();
    with("github.com/jpsember/golang-base/files.Path");
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public String compilerInitialValue() {
    return Context.pt.PKGGO_FILE + ".EmptyPath";
  }

  @Override
  public final String provideSourceDefaultValue() {
    return Context.pt.PKGGO_FILE + "EmptyPath";
  }

  @Override
  public final String parseDefaultValue(SourceBuilder classSpecificSource, FieldDef fieldDef, JSMap map) {
    return "NewPathOrEmptyM(" + DataUtil.escapeChars(map.get(""), true) + ")";
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a("n.", f.instanceName(), " = ", Context.pt.PKGGO_FILE, "NewPathOrEmptyM(",
        f.nameStringConstantQualified(), ", ", f.defaultValueOrNull(), ")");
  }

}
