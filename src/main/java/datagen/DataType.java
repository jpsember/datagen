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
import js.data.DataUtil;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;
import static datagen.Utils.*;

/**
 * Abstract base class for generating source code for a data type
 */
public abstract class DataType implements DefaultValueParser {

  public final boolean python() {
    return language() == Language.PYTHON;
  }

  public final Language language() {
    return Context.config.language();
  }

  // ------------------------------------------------------------------
  // Naming
  // ------------------------------------------------------------------

  final boolean VB = false;

  public final void setQualifiedClassName(QualifiedName qn) {
    todo("why is this an explicit public method?");
    checkState(mClassWithPackage == null);
    mClassWithPackage = qn;
    if (VB)
      pr("mClassWithPackage:", mClassWithPackage);
  }

  public final QualifiedName qualifiedClassName() {
    // If no QualifiedName assigned yet, do so
    if (mClassWithPackage == null) {
      String expression = provideQualifiedClassNameExpr();
      QualifiedName qualifiedName = ParseTools.parseQualifiedName(expression);
      if (language() == Language.JAVA)
        checkArgument(nonEmpty(qualifiedName.packagePath()));
      setQualifiedClassName(qualifiedName);
    }
    return mClassWithPackage;
  }

  /**
   * Provide a qualified class name
   * 
   * <pre>
   * 
   * This is the name of the type, optionally with a fully-qualified path
   * suitable for import statements, e.g.
   * 
   *    java.io.File
   * 
   * For Python, primitive types will not have package names, e.g.
   * 
   *    int
   *    string
   * 
   * </pre>
   */
  protected String provideQualifiedClassNameExpr() {
    throw notSupported("no qualified class name expression provided;", getClass().getName());
  }

  /**
   * Construct name of type, wrapped if necessary within an "import expression"
   * so that we are sure to generate import statements if the type appears in
   * the source.
   * 
   * Can be overridden for compound types (maps, lists) to ensure the other
   * types are wrapped in import expressions as well
   */
  protected String provideTypeName() {
    if (isPrimitive())
      return qualifiedClassName().className();

    // Wrap the type name within an import expression so that whenever it is used,
    // we ensure it is imported
    // The assumption is that import expressions are needed iff type is not primitive
    //
    String importExpr = constructImportExpression();
    return ParseTools.importExpression(importExpr, qualifiedClassName().className());
  }

  /**
   * Get type name, by calling provideTypeName() if necessary
   */
  public final String typeName() {
    if (mTypeName == null) {
      mTypeName = provideTypeName();
      if (VB)
        pr("mTypeName:", mTypeName);
    }
    return mTypeName;
  }

  private String mTypeName;
  private QualifiedName mClassWithPackage;

  //------------------------------------------------------------------

  @Deprecated // why is this required?
  protected final String constructImportExpression() {
    QualifiedName nm = qualifiedClassName();
    if (python()) {
      checkArgument(nonEmpty(nm.packagePath()), nm);
      return "from " + nm.packagePath() + " import " + nm.className();
    }
    return nm.combined();
  }

  /**
   * Determine if the type is a primitive type, e.g. int, short, etc
   */
  public final boolean isPrimitive() {

    if (python()) {
      // If there is no package component to its qualified class name, it is primitive
      //
      return qualifiedClassName().packagePath().isEmpty();
    }

    // If the class name starts with a lower case letter, assume it's a primitive;
    // e.g. int, double, boolean
    // vs File, Integer, Double, Boolean
    //
    return qualifiedClassName().className().charAt(0) >= 'a';
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
   * Get source code for type's default value, in immutable form (if
   * applicable). This is our default value, which may be distinct from
   * compileInitialValue(). For example, our default value for a String is "",
   * whereas Java's is null.
   *
   * The default method calls compilerInitialValue()
   */
  public String provideSourceDefaultValue() {
    return compilerInitialValue();
  }

  public final String sourceDefaultValue() {
    if (mCachedSourceDefaultValue == null) {
      mCachedSourceDefaultValue = provideSourceDefaultValue();
    }
    return mCachedSourceDefaultValue;
  }

  private String mCachedSourceDefaultValue;

  // ------------------------------------------------------------------

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
      throw languageNotSupported();
    case PYTHON:
      s.doIf(f.optional(), "if self._", f.sourceName(), " is not None:", OPEN);
      break;
    case JAVA:
      s.doIf(f.optional(), "if (m", f.sourceName(), " != null)", OPEN);
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
          sourceGenerateSerializeToObjectExpression("self._" + f.sourceName()));
    } else {
      s.a("m.put(", f.nameStringConstant(), ", ",
          sourceGenerateSerializeToObjectExpression("m" + f.sourceName()), ");");
    }
    sourceEndIf(s).cr();
  }

  /**
   * Generate source code to convert value to one appropriate to be stored
   * within a JSMap (or, if Python, compatible with json.dumps)
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
      throw languageNotSupported();
    case PYTHON:
      s.a("inst._", f.sourceName(), " = obj.get(", f.nameStringConstant(), ", ", f.defaultValueOrNull(), ")");
      break;
    case JAVA:
      s.a("m", f.sourceName(), " = m.opt(", f.nameStringConstant(), ", ", f.defaultValueOrNull(), ");");
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
      throw languageNotSupported();
    case PYTHON: {
      todo("!we should have some symbolic constants for things like t, inst");
      todo("!we should optimize later by having a utility class to reduce volume of boilerplate");
      if (f.optional()) {
        s.a("t = obj.get(", f.nameStringConstant(), ", ", f.nullIfOptional("[]"), ")", CR, //
            "if t is not None:", OPEN, //
            "inst._", f.sourceName(), " = t.copy()", CLOSE);
      } else {
        s.a("inst._", f.sourceName(), " = obj.get(", f.nameStringConstant(), ", ", f.nullIfOptional("[]"),
            ").copy()", CR);
      }
    }
      break;
    case JAVA:
      s.a("m", f.sourceName(), " = js.data.DataUtil.parse", typeName(), "List(m, ", f.nameStringConstant(),
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
      throw languageNotSupported();
    case PYTHON:
      s.a("r = r * 37 + hash(self._", f.sourceName(), ")");
      break;
    case JAVA:
      s.a("r = r * 37 + m", f.sourceName(), ".hashCode();");
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
      throw languageNotSupported();
    case PYTHON:
      if (f.optional() /* || isPrimitive() */) {
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
   * Mark this data type as 'used'; for unnecessary import warnings
   */
  public final void setUsedFlag() {
    mUsedFlag = true;
  }

  /**
   * Mark this data type as 'declared'; for unnecessary import warnings
   */
  public final void setDeclaredFlag() {
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

}
