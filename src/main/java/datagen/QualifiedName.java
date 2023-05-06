package datagen;

import js.base.BaseObject;
import js.json.JSMap;

import static js.base.Tools.*;

public final class QualifiedName extends BaseObject {

  public static QualifiedName parse(String expr) {
    int nameStartPos = expr.lastIndexOf('.');
    if (nameStartPos == 0 || nameStartPos == expr.length() - 1)
      throw badArg("from expr:", quote(expr));
    String packagePath = expr.substring(0, Math.max(0, nameStartPos));
    if (false)
      checkNonEmpty(packagePath);
    String className = expr.substring(1 + nameStartPos);
    return new QualifiedName(packagePath, className);
  }

  public static QualifiedName parse(String expr, String defaultPackage) {
    //alertWithSkip(1, "parsing " + expr + ":", expr);
    int nameStartPos = expr.lastIndexOf('.');
    if (nameStartPos == 0 || nameStartPos == expr.length() - 1)
      throw badArg("from expr:", quote(expr));
    String packagePath = expr.substring(0, Math.max(0, nameStartPos));
    packagePath = ifNullOrEmpty(packagePath, nullToEmpty(defaultPackage));
    String className = expr.substring(1 + nameStartPos);

    // Special rules for Python:
    //
    // For class name "Foo", if package contains "gen" and 
    // package doesn't already end with "gen.foo", add ".foo" to it
    //
    //
    todo("We can't use Utils.language() here because of init problems");

    if (Utils.python()) {
      if (split(packagePath, '.').contains("gen")) {
        String suffix = "." + convertCamelToUnderscore(className);
        if (!packagePath.endsWith(suffix))
          packagePath += suffix;
      }
    }
    return new QualifiedName(packagePath, className);
  }

  public String packagePath() {
    return mPackagePath;
  }

  public String className() {
    return mClassName;
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
    return m;
  }

  private final String mPackagePath;
  private final String mClassName;
  private String mCachedCombined;

  public String brief() {
    return "[" + packagePath() + " : " + className() + "]";
  }

}
