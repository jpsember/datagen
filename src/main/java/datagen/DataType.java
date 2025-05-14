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

import static js.base.Tools.*;

import java.util.Map;

import datagen.gen.TypeStructure;

import static datagen.Utils.*;

/**
 * Abstract base class for generating source code for a data type
 */
public abstract class DataType implements DefaultValueParser {

  public static final int NAME_MAIN = 0;
  public static final int NAME_ALT = 1;
  public static final int NAME_HUMAN = 2;

  // ------------------------------------------------------------------
  // Naming
  // ------------------------------------------------------------------

  /**
   * Construct name of type, wrapped if necessary within an "import expression"
   * so that we are sure to generate import statements if the type appears in
   * the source.
   * 
   * <pre>
   * 
   * This is the name of the type, optionally with a fully-qualified path, e.g.
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
  public final DataType with(String qualNameExpr) {
    return with(NAME_MAIN, qualNameExpr);
  }

  public final DataType with(int index, String qualNameExpr) {
    QualifiedName q = QualifiedName.parse(qualNameExpr);
    return with(index, q);
  }

  public final DataType with(int index, QualifiedName qualName) {
    QualifiedName qOld = mQualNameMap.put(index, qualName);
    if (qOld != null)
      badState("duplicate qualified name for index", index, ":", INDENT, qualName, "was:", qOld);
    return this;
  }

  /**
   * This is the method that subclasses should override to use alternate naming
   * conventions
   */
  public DataType withQualifiedName(QualifiedName qualifiedName) {
    return with(NAME_MAIN, qualifiedName);
  }

  public final QualifiedName qualifiedName() {
    return qualifiedName(NAME_MAIN);
  }

  public QualifiedName qualifiedName(int index) {
    QualifiedName q = optName(index);
    if (q == null) {
      if (index == NAME_MAIN)
        throw badState("no qualified name with index", index, "for", getClass().getSimpleName());
      q = qualifiedName(NAME_MAIN);
      with(index, q);
    }
    return q;
  }

  private QualifiedName optName(int index) {
    return mQualNameMap.get(index);
  }

  private Map<Integer, QualifiedName> mQualNameMap = hashMap();

  public final String typeName() {
    return qualifiedName(NAME_MAIN).embeddedName();
  }

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

  @Deprecated
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

  public void dumpNames(Object... messages) {
    pr(insertStringToFront("Names:", messages));
    for (int i = 0; i < 8; i++) {
      QualifiedName q = mQualNameMap.get(i);
      if (q == null)
        continue;
      pr(INDENT, i, INDENT, q);
    }
  }

  // ------------------------------------------------------------------
  // Type substitution
  // ------------------------------------------------------------------

  public DataType modifyTypeFilter(TypeStructure structure) {
    return this;
  }

  public String sqlType() {
    return "!!!UNKNOWN sqlType";
  }

  // ------------------------------------------------------------------
  // Debug comments
  // ------------------------------------------------------------------

  public void comment(SourceBuilder s, Object msg) {
    if (RUST_COMMENTS) {
      s.a(commentWithSkip(1, msg)).cr();
    }
  }

  public String comment(Object msg) {
    return commentWithSkip(1, msg);
  }

  public String comment() {
    return commentWithSkip(1, "");
  }

  public String commentWithSkip(int skip, Object msg) {
    if (RUST_COMMENTS) {
      var x = getStackTraceElement(1 + skip);
      return " /*** " + x + " " + msg + " ***/";
    }
    return "";
  }

  // ------------------------------------------------------------------
  // Language-specific stuff
  // ------------------------------------------------------------------

  public String setterArgSignature(String expr) {
    throw languageNotSupported("setterArgSignature for type:", getClass().getName());
  }

  public String setterArgUsage(String expr) {
    throw languageNotSupported("setterArgUsage for type:", getClass().getName());
  }

  public String getterReturnTypeExpr() {
    throw languageNotSupported("getterReturnTypeExpr for type:", getClass().getName());
  }

  public void getterBody(SourceBuilder s, FieldDef f) {
    throw languageNotSupported("getterBody for type:", getClass().getName());
  }

  public String getInitInstanceFieldExpr(FieldDef f) {
    return f.defaultValueOrNull();
  }

  public String buildRustJsonValueFrom(String expr) {
    throw languageNotSupported("buildRustJsonValueFrom for type:", getClass().getName());
  }

  public void generateSerializeListOf(SourceBuilder s, FieldDef f) {
    throw languageNotSupported("generateSerializeListOf for type:", getClass().getName());
  }

  public String wrapInBuildExpression(String expr) {
    throw languageNotSupported("wrapInBuildExpression for type:", getClass().getName());
  }

  /**
   * Modify a code expression, if necessary, to copy a field from a static object to its builder form.
   *
   * Default returns the input expression.
   *
   * For example, a StringArray should construct a mutable copy of the input array.
   */
  public String staticToBuilder(String expr) {
    return expr;
  }

  public String builderToStatic(String expr) {
    return expr;
  }


}
