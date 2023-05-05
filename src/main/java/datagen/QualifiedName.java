package datagen;

import js.base.BaseObject;
import static js.base.Tools.*;

public final class QualifiedName extends BaseObject {

  public static QualifiedName parse(String expr, String defaultPackage) {
    int nameStartPos = expr.lastIndexOf('.');
    if (nameStartPos == 0 || nameStartPos == expr.length() - 1)
      throw badArg("from expr:", quote(expr));
    String pkg = expr.substring(0, Math.max(0, nameStartPos));
    pkg = ifNullOrEmpty(pkg, nullToEmpty(defaultPackage));
    String className = expr.substring(1 + nameStartPos);
    return new QualifiedName(pkg, className);
  }

  public String packagePath() {
    return mPackagePath;
  }

  public String className() {
    return mClassName;
  }

  public String combined() {
    if (mCachedCombined == null) {
      //String packagePath = q.packagePath();
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

  private final String mPackagePath;
  private final String mClassName;
  private String mCachedCombined;

}
