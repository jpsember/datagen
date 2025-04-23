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

import static js.base.Tools.*;

import java.util.List;

import datagen.FieldDef;
import datagen.RustDataType;
import datagen.SourceBuilder;

import static datagen.SourceBuilder.*;
import static datagen.Utils.*;

public class RustEnumDataType extends RustDataType implements EnumDataType {

  @Override
  public String provideSourceDefaultValue() {
    return "default_" + typeName() + "()";
  }

  @Override
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return valueExpression + ".to_json()";
  }

  @Override
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    s.a(OPEN, "let x = m.opt(", f.nameStringConstantQualified(), ");", CR, //
        "if !x.is_null()", OPEN, //
        "n.", f.instanceName(), " = parse_", qualifiedName(NAME_HUMAN).className(), "(x.clone())?;", CLOSE, //
        CLOSE);
  }

  
  
  
  
  
  
  
  
  
  
  
  
  
  @Override
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    throw languageNotSupported("deserializing list of Go enums from list");
  }

  @Override
  public String setterArgSignature(String expr) {
    return qualifiedName(NAME_HUMAN).className();
  }

  @Override
  public String buildRustJsonValueFrom(String expr) {
    return "parse_" + qualifiedName(NAME_HUMAN).className() + "(" + expr + ")";
  }

  //------------------------------------------------------------------
  // EnumDataType interface
  // ------------------------------------------------------------------

  @Override
  public void addLabel(String label) {
    String label2 = convertUnderscoreToCamel(label);
    checkArgument(!mLabels.contains(label2), "duplicate label:", label);
    mLabels.add(label2);
  }

  @Override
  public List<String> labels() {
    return mLabels;
  }

  private List<String> mLabels = arrayList();

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public String setterArgUsage(String expr) {
    return expr;
  }

  @Override
  public String wrapInBuildExpression(String expr) {
    return expr;
  }

}
