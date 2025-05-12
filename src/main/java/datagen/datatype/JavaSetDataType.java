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

import datagen.Context;
import datagen.FieldDef;
import datagen.JavaDataType;
import datagen.SourceBuilder;

public class JavaSetDataType extends JavaDataType {

  public JavaSetDataType(JavaDataType wrappedType) {
    mWrappedValueType = wrappedType;
    with("java.util.Set<" + wrappedType.qualifiedName().className() + ">");
  }

  public JavaDataType wrappedValueType() {
    return mWrappedValueType;
  }

  @Override
  public String provideSourceDefaultValue() {
    return Context.pt.PKG_DATAUTIL + ".emptySet()";
  }

  @Override
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    s.a(OPEN, //
        Context.pt.PKG_JSLIST, " j = new ", Context.pt.PKG_JSLIST, "();", CR, //
        "for (", wrappedValueType().typeName(), " e : ", f.instanceName(), ")", IN, //
        "j.add(", wrappedValueType().sourceGenerateSerializeToObjectExpression("e"), ");", OUT, //
        "m.put(", f.nameStringConstantQualified(), ", j);", //
        CLOSE, CR);
    sourceEndIf(s);
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.open();
    if (!f.optional())
      s.a(f.instanceName(), " = ", f.defaultValueOrNull(), ";", CR);

    s.a(OPEN, //
        "JSList m2 = m.optJSList(", QUOTE, f.name(), ");", CR, //
        "if (m2 != null && !m2.isEmpty())", OPEN, //
        "Set<", wrappedValueType().typeName(), "> mp = new ", Context.pt.PKG_HASH_SET, "<>();", CR, //
        "for (Object e : m2.wrappedList())", IN, //
        "mp.add(", wrappedValueType().deserializeJsonToMapValue("e"), ");", OUT);
    String expr = "mp";
    if (Context.debugMode())
      expr = Context.pt.immutableCopyOfSet(expr);
    s.a(f.instanceName(), " = ", expr, ";", CLOSE, //
        CLOSE);
    s.close();
  }

  @Override
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    s.a("r = r * 37 + ", f.instanceName(), ".hashCode();");
  }

  private final JavaDataType mWrappedValueType;
}
