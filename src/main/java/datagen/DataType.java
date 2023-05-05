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

import datagen.gen.QualifiedName;

import static js.base.Tools.*;
import static datagen.Utils.*;

/**
 * Abstract base class for generating source code for a data type
 */
public abstract class DataType implements DefaultValueParser {

  // ------------------------------------------------------------------
  // Naming
  // ------------------------------------------------------------------

  public final DataType with(String qualClassNameExpr) {
    setQualifiedClassName(qualClassNameExpr);
    return this;
  }

  public void setQualifiedClassName(QualifiedName qualifiedName) {
    checkState(mClassWithPackage == null);
    mClassWithPackage = qualifiedName;
  }

  public final void setQualifiedClassName(String expr) {
    setQualifiedClassName(ParseTools.updateForPython(ParseTools.parseQualifiedName(expr, null)));
  }

  public final QualifiedName qualifiedClassName() {
    // If no QualifiedName assigned yet, do so
    if (mClassWithPackage == null) {
      alert("************ No qualified class name explicitly set for:", getClass().getSimpleName());
      setQualifiedClassName(
          ParseTools.updateForPython(ParseTools.parseQualifiedName(provideQualifiedClassNameExpr(), null)));
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
    todo(
        "Explicitly set qualified name, with appropriate wrapping; typeName() should just refer to that variable");
    if (isPrimitive())
      return qualifiedClassName().className();

    // Wrap the type name within an import expression so that whenever it is used,
    // we ensure it is imported
    // The assumption is that import expressions are needed iff type is not primitive
    //
    return ParseTools.importedClassExpr(null, qualifiedClassName().combined());
  }

  /**
   * Get type name, by calling provideTypeName() if necessary
   */
  public final String typeName() {
    if (mTypeName == null) {
      mTypeName = provideTypeName();
    }
    return mTypeName;
  }

  /**
   * Get the 'alternate' type name, calling provideAlternateTypeName() if
   * necessary.
   * 
   * For the Go language, when manipulating contract data types (e.g. dog.dat),
   * we will usually refer to the type by its interface: IDog. The alternate
   * form will be the non-interface, built form: "Dog"; this method can be
   * overridden to make this distinction.
   *
   * Languages that don't need this feature should not override this method.
   */
  protected String provideAlternateTypeName() {
    return typeName();
  }

  public final String alternateTypeName() {
    if (mAlternateTypeName == null) {
      mAlternateTypeName = provideAlternateTypeName();
    }
    return mAlternateTypeName;
  }

  private String mTypeName;
  private String mAlternateTypeName;
  private QualifiedName mClassWithPackage;

  //------------------------------------------------------------------

  /**
   * Determine if the type is a primitive type, e.g. int, short, etc
   */
  public abstract boolean isPrimitive();

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
    if (mCachedSourceDefaultValue == null)
      mCachedSourceDefaultValue = provideSourceDefaultValue();
    return mCachedSourceDefaultValue;
  }

  private String mCachedSourceDefaultValue;

  // ------------------------------------------------------------------

  /**
   * Generate source code to convert a value to a mutable form, for storing
   * within a builder.
   *
   * Default returns the expression unchanged. ListDataType overrides this to
   * construct a mutable copy instead. If more structured types are added, I'll
   * override this method appropriately.
   * 
   * Per issue #33, this will only have an effect if user is defining the
   * datatype using the old 'fields' keyword.
   */
  public String sourceExpressionToMutable(String valueExpression) {
    // Suspect this method isn't required if classMode
    checkState(!Context.generatedTypeDef.classMode());
    return valueExpression;
  }

  /**
   * Generate source code to convert a value to an immutable form
   *
   * Default returns the expression unchanged
   * 
   * Per issue #33, this will only have an effect if user is defining the
   * datatype using the old 'fields' keyword.
   */
  public void sourceExpressionToImmutable(SourceBuilder s, FieldDef fieldDef, String targetExpression,
      String valueExpression) {
    s.a(targetExpression, " = ", valueExpression);
  }

  /**
   * If field is optional, generate an 'if' statement that checks if the field
   * is non-null
   */
  public abstract void sourceIfNotNull(SourceBuilder s, FieldDef f);

  /**
   * Close previous sourceIfNotNull() call
   */
  public abstract SourceBuilder sourceEndIf(SourceBuilder s);

  /**
   * Generate an 'if' statement that checks if an expression is non-null
   */
  public abstract void sourceIfNotNull(SourceBuilder s, String expr);

  // ------------------------------------------------------------------
  // Serialization
  // ------------------------------------------------------------------

  public abstract void sourceSerializeToObject(SourceBuilder s, FieldDef f);

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
   * Generate source code for deserializing a value from a JSMap (or dict, if
   * Python)
   */
  public abstract void sourceDeserializeFromObject(SourceBuilder s, FieldDef f);

  /**
   * Generate source code for deserializing a list of values from a JSList (or
   * Python list)
   *
   * If Java, default implementation assumes there's a DataUtil method
   * parseXXXList(...)
   */
  public abstract void sourceDeserializeFromList(SourceBuilder s, FieldDef f);

  // ------------------------------------------------------------------
  // Hashcode and Equals methods
  //------------------------------------------------------------------

  /**
   * Generate source code to compare two values for equality
   */
  public abstract void sourceGenerateEquals(SourceBuilder s, String a, String b);

  /**
   * Generate source code to continue the calculation of a value for hashCode().
   */
  public abstract void sourceHashCalculationCode(SourceBuilder s, FieldDef f);

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

  public final DataType getOptionalVariant() {
    if (!mPreparedVariantOptional) {
      mPreparedVariantOptional = true;
      mOptionalVariant = optionalVariant();
    }
    return mOptionalVariant;
  }

  public final DataType getListVariant() {
    if (!mPreparedVariantList) {
      mPreparedVariantList = true;
      mListVariant = listVariant();
    }
    return mListVariant;
  }

  private boolean mPreparedVariantOptional;
  private boolean mPreparedVariantList;
  private DataType mOptionalVariant;
  private DataType mListVariant;

  public abstract void sourceSetter(SourceBuilder s, FieldDef f, String targetExpr);

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
