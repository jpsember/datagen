package datagen;

import js.base.BaseObject;
import js.json.JSMap;

import static js.base.Tools.*;

public final class QualifiedName extends BaseObject {

  public static QualifiedName parse(String expr) {
    return parse(expr, null);
  }

  public static QualifiedName parse(String expr, String defaultPackage) {
    // alertWithSkip(1, "parsing " + expr);
    int nameStartPos = expr.lastIndexOf('.');
    if (nameStartPos == 0 || nameStartPos == expr.length() - 1)
      throw badArg("from expr:", quote(expr));
    String packagePath = expr.substring(0, Math.max(0, nameStartPos));
    packagePath = ifNullOrEmpty(packagePath, nullToEmpty(defaultPackage));
    String className = expr.substring(1 + nameStartPos);

    QualifiedName result = new QualifiedName(packagePath, className);

    // Special rules for Python:
    //
    // For class name "Foo", if package contains "gen" and 
    // package doesn't already end with ".foo", add ".foo" to it
    //
    if (Utils.python())
      result = result.convertToPython();
    return result;
  }

  private QualifiedName convertToPython() {
    if (split(mPackagePath, '.').contains("gen")) {
      String suffix = "." + convertCamelToUnderscore(mClassName);
      if (!mPackagePath.endsWith(suffix)) {
        return withPackageName(mPackagePath + suffix);
      }
    }
    return this;
  }

  public String packagePath() {
    return mPackagePath;
  }

  public String className() {
    return mClassName;
  }

  public String embeddedName() {
    if (mEmbeddedName == null)  
      throw badState("no embedded name yet for:", INDENT, this);
    return mEmbeddedName;
  }

  public String combined() {
    if (mCachedCombined == null) {
      String combined = className();
      if (!packagePath().isEmpty())
        combined = packagePath() + "." + className();
      mCachedCombined = combined;
    }
    return mCachedCombined;
  }

  public QualifiedName withClassName(String className) {
    return new QualifiedName(packagePath(), className);
  }

  public QualifiedName withPackageName(String packageName) {
    return new QualifiedName(packageName, className());
  }

  private QualifiedName(String packagePath, String className) {
    mPackagePath = packagePath;
    mClassName = className;
  }

  @Override
  protected String supplyName() {
    return combined();
  }

  @Override
  public JSMap toJson() {
    JSMap m = map();
    m.put("class_name", className()).put("package_path", packagePath()).put("combined", combined());
    if (mEmbeddedName != null)
      m.put("embedded_name", mEmbeddedName);
    return m;
  }

  public QualifiedName withEmbeddedName(String typeName) {
    checkState(mEmbeddedName == null, "already set to:", mEmbeddedName);
    mEmbeddedName = typeName;
    return this;
  }

  private final String mPackagePath;
  private final String mClassName;
  private String mEmbeddedName;
  private String mCachedCombined;

  public String brief() {
    return "[" + packagePath() + " : " + className() + "]";
  }

}
