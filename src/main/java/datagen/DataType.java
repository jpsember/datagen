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
package datagen;

import datagen.gen.Language;
import datagen.gen.QualifiedName;
import js.parsing.Scanner;
import js.data.DataUtil;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

import java.util.List;

/**
 * Abstract base class for generating source code for a data type
 */
public abstract class DataType implements DefaultValueParser {

  public final void setContext(Context context) {
    mContext = context;
  }

  public final Context context() {
    return mContext;
  }

  public final boolean python() {
    return context().python();
  }

  public final Language language() {
    return context().config.language();
  }

  public final void setQualifiedClassName(QualifiedName qn) {
    checkState(mClassWithPackage == null);
    mClassWithPackage = qn;
  }

  public final QualifiedName qualifiedClassName() {
    // If no QualifiedName assigned yet, do so
    if (mClassWithPackage == null) {
      String expr = provideQualifiedClassNameExpr();
      setQualifiedClassName(ParseTools.parseQualifiedName(expr));
    }
    return mClassWithPackage;
  }

  /**
   * Specify the qualified class name
   */
  protected String provideQualifiedClassNameExpr() {
    throw notSupported("no qualified class name expression provided");
  }

  /**
   * Get qualified package and class name for the type
   */
  public final String typeName() {
    if (mTypeName == null)
      mTypeName = provideTypeName();
    return mTypeName;
  }

  protected String provideTypeName() {
    return ParseTools.importExpression(qualifiedClassName().combined(), qualifiedClassName().className());
  }

  public List<String> auxilliaryImportExpressions() {
    return DataUtil.emptyList();
  }

  /**
   * Get text that generates an import for this type, but doesn't generate any
   * source. Used for composite types, e.g. (List{type})
   */
  public final String compositeTypeName(DataType wrappedType) {
    if (mTypeName == null) {
      String exprForWrapped = "";
      if (wrappedType != null)
        exprForWrapped = ParseTools.importExpression(wrappedType.qualifiedClassName().combined(), "");
      mTypeName = ParseTools.importExpression(qualifiedClassName().combined(),
          qualifiedClassName().className()) + exprForWrapped;
    }
    return mTypeName;
  }

  private String mTypeName;

  public void assertNotPython() {
    if (python())
      throw notSupported("unexpected for python");
  }

  /**
   * Determine if the type is a primitive type, e.g. int, short, etc
   */
  public final boolean isPrimitive() {
    // If the class name starts with a lower case letter, assume it's a primitive;
    // e.g. int, double, boolean
    // vs File, Integer, Double, Boolean
    //
    return qualifiedClassName().className().charAt(0) >= 'a';
  }

  /**
   * Get source code for type's default value, in immutable form (if
   * applicable). This is our default value, which may be distinct from Java's
   * default value. For example, our default value for a String is "", whereas
   * Java's is null.
   *
   * The default method calls compilerInitialValue()
   */
  public String ourDefaultValue() {
    return compilerInitialValue();
  }

  // ------------------------------------------------------------------
  // Default values
  // ------------------------------------------------------------------

  /**
   * Get source code for type's implicit default value (the one that instance
   * fields are set to, implicitly, before any subsequent assignments)
   */
  public String compilerInitialValue() {
    return nullExpr();
  }

  /**
   * Get the language-specific expression for "null" (i.e., "None" if Python)
   */
  public final String nullExpr() {
    switch (language()) {
    default:
      throw notSupported();
    case PYTHON:
      return "None";
    case JAVA:
      return "null";
    }
  }

  @Override
  public String parseDefaultValue(Scanner scanner, SourceBuilder classSpecificSource, FieldDef fieldDef) {
    throw notSupported("default value for:", typeName());
  }

  /**
   * Generate source code to convert a value to an mutable form, for storing
   * within a builder.
   *
   * Default returns the expression unchanged. ListDataType overrides this to
   * construct a mutable copy instead. If more structured types are added, I'll
   * override this method appropriately.
   * 
   */
  public String sourceExpressionToMutable(String valueExpression) {
    return valueExpression;
  }

  /**
   * Generate source code to convert a value to an immutable form
   *
   * Default returns the expression unchanged
   */
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    s.a(targetExpression, " = ", valueExpression);
  }

  /**
   * If field is optional, generate an 'if' statement that checks if the field
   * is non-null
   */
  public final void sourceIfNotNull(SourceBuilder s, FieldDef f) {
    switch (language()) {
    default:
      throw notSupported();
    case PYTHON:
      s.doIf(f.optional(), "if self._", f.javaName(), " is not None:", OPEN);
      break;
    case JAVA:
      s.doIf(f.optional(), "if (m", f.javaName(), " != null)", OPEN);
      break;
    }
  }

  /**
   * Close previous sourceIfNotNull() call
   */
  public final SourceBuilder sourceEndIf(SourceBuilder s) {
    return s.endIf(CLOSE);
  }

  // ------------------------------------------------------------------
  // Serialization
  // ------------------------------------------------------------------

  /**
   * Generate source code to serialize an instance field.
   * 
   * Used to generate a data type's "toJson()" method (or "to_dict" if Python)
   */
  public void sourceSerializeToObject(SourceBuilder s, FieldDef f) {
    sourceIfNotNull(s, f);
    if (python()) {
      s.a("m[", f.nameStringConstant(), "] = ",
          sourceGenerateSerializeToObjectExpression("self._" + f.javaName()));
    } else {
      s.a("m.put(", f.nameStringConstant(), ", ",
          sourceGenerateSerializeToObjectExpression("m" + f.javaName()), ");");
    }
    sourceEndIf(s).cr();
  }

  /**
   * Generate source code to convert value to one appropriate to be stored
   * within a JSMap (or dict, if Python)
   *
   * Default implementation returns the input value
   */
  public String sourceGenerateSerializeToObjectExpression(String valueExpression) {
    return valueExpression;
  }

  /**
   * Generate source code for deserializing a value from a JSMap
   */
  public void sourceDeserializeFromObject(SourceBuilder s, FieldDef f) {
    switch (language()) {
    default:
      throw notSupported();
    case PYTHON:
      s.a("inst._", f.javaName(), " = obj.get(", f.nameStringConstant(), ", ", f.defaultValueOrNull(), ")");
      break;
    case JAVA:
      s.a("m", f.javaName(), " = m.opt(", f.nameStringConstant(), ", ", f.defaultValueOrNull(), ");");
      break;
    }
  }

  /**
   * Generate source code for deserializing a list of values from a JSList (or
   * Python list)
   *
   * If Java, default implementation assumes there's a DataUtil method
   * parseXXXList(...)
   */
  public void sourceDeserializeFromList(SourceBuilder s, FieldDef f) {
    switch (language()) {
    default:
      throw notSupported();
    case PYTHON: {
      todo("!we should have some symbolic constants for things like t, inst");
      todo("!we should optimize later by having a utility class to reduce volume of boilerplate");
      if (f.optional()) {
        s.a("t = obj.get(", f.nameStringConstant(), ", ", f.nullIfOptional("[]"), ")", CR, //
            "if t is not None:", OPEN, //
            "inst._", f.javaName(), " = t.copy()", CLOSE);
      } else {
        s.a("inst._", f.javaName(), " = obj.get(", f.nameStringConstant(), ", ", f.nullIfOptional("[]"),
            ").copy()", CR);
      }
    }
      break;
    case JAVA:
      s.a("m", f.javaName(), " = js.data.DataUtil.parse", typeName(), "List(m, ", f.nameStringConstant(),
          ", ", f.optional(), ");");
      break;
    }
  }

  /**
   * Generate source code to convert a string to a Java value. Used to
   * deserialize JSMap keys to Map keys
   */
  public String deserializeStringToJavaValue(String stringValueExpression) {
    if (isPrimitive())
      return typeName() + ".parse" + DataUtil.capitalizeFirst(typeName()) + "(" + stringValueExpression + ")";
    throw notSupported("deserializeStringToJavaValue for dataType:", getClass());
  }

  /**
   * Generate source code to deserialize a JSON value to a Java value. Used to
   * deserialize JSMap values to Map values
   * 
   * @param jsonValue
   *          representation of a JSMap value (from a key/value pair), or a
   *          JSList value
   */
  public String deserializeJsonToJavaValue(String jsonValue) {
    if (isPrimitive())
      return jsonValue;
    throw notSupported("deserializeJsonToJavaValue for dataType:" + getClass());
  }

  // ------------------------------------------------------------------
  // Hashcode and Equals methods
  // ------------------------------------------------------------------

  public void sourceGenerateEquals(SourceBuilder s, String a, String b) {
    if (isPrimitive())
      s.a(a, " == ", b);
    else
      s.a(a, ".equals(", b, ")");
  }

  /**
   * Generate source code to continue the calculation of a value for hashCode().
   *
   * Default implementation assumes value is a non-null Object reference, and
   * calls its hashCode() method
   */
  public void sourceHashCalculationCode(SourceBuilder s, FieldDef f) {
    switch (language()) {
    default:
      throw notSupported();
    case PYTHON:
      s.a("r = r * 37 + hash(self._", f.javaName(), ")");
      break;
    case JAVA:
      s.a("r = r * 37 + m", f.javaName(), ".hashCode();");
      break;
    }
  }

  /**
   * If this DataType has an alternate suitable for optional types, return it;
   * default returns this. Required for supporting boxed java primitive types
   */
  public DataType optionalVariant() {
    return this;
  }

  /**
   * If array versions of this DataType have an alternate scalar type, return
   * it; else, null
   */
  public DataType listVariant() {
    return null;
  }

  public void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr) {
    switch (language()) {
    default:
      throw notSupported();
    case PYTHON:
      if (f.optional() || isPrimitive()) {
        s.a(targetExpr, " = ", //
            sourceExpressionToMutable("x"));
      } else {
        s.a(targetExpr, " = ", //
            f.defaultValueOrNull(), " if x is None else ", sourceExpressionToMutable("x"));
      }
      break;
    case JAVA:
      if (f.optional() || isPrimitive()) {
        s.a(targetExpr, " = ", //
            sourceExpressionToMutable("x"));
      } else {
        s.a(targetExpr, " = ", //
            sourceExpressionToMutable("(x == null) ? " + f.defaultValueOrNull() + " : x"));
      }
      s.a(";");
      break;
    }
  }

  // ------------------------------------------------------------------
  // Bookkeeping
  // ------------------------------------------------------------------

  /**
   * Mark this data type as 'used'. This is a flag used to warn about
   * unnecessary import statements
   */
  public final void setUsedFlag() {
    if (mUsedFlag)
      return;
    mUsedFlag = true;
  }

  /**
   * Mark this data type as 'used'. This is a flag used to warn about
   * unnecessary import statements
   */
  public final void setDeclaredFlag() {
    if (mDeclared)
      return;
    mDeclared = true;
  }

  public final boolean usedFlag() {
    return mUsedFlag;
  }

  public final boolean declaredFlag() {
    return mDeclared;
  }

  private boolean mDeclared;
  private boolean mUsedFlag;
  private QualifiedName mClassWithPackage;
  private Context mContext;

}
