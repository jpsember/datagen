package datagen;

import js.base.BaseObject;
import js.json.JSMap;

import static datagen.Utils.*;
import static js.base.Tools.*;

import datagen.gen.Language;

public final class QualifiedName extends BaseObject {

  public static QualifiedName parse(String expr, String defaultPackage, Language language) {
    int nameStartPos = expr.lastIndexOf('.');
    if (nameStartPos == 0 || nameStartPos == expr.length() - 1)
      throw badArg("from expr:", quote(expr));
    String packagePath = expr.substring(0, Math.max(0, nameStartPos));
    packagePath = ifNullOrEmpty(packagePath, nullToEmpty(defaultPackage));
    String className = expr.substring(1 + nameStartPos);

    // Do some language-specific things

    switch (language) {
    case PYTHON: {
      // See PythonSourceGen.generateImports() for more info.
      if (("." + packagePath + ".").contains("." + GEN_SUBDIR_NAME + ".")) {
        String suffix = "." + convertCamelToUnderscore(className);
        if (!packagePath.endsWith(suffix)) {
          packagePath = packagePath + suffix;
        }
      }
    }
      break;

    default:
      break;
    }

    QualifiedName result = new QualifiedName(packagePath, className);
    return result;
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

}
