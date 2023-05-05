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
    String pkg = expr.substring(0, Math.max(0, nameStartPos));
    pkg = ifNullOrEmpty(pkg, nullToEmpty(defaultPackage));
    String className = expr.substring(1 + nameStartPos);
    QualifiedName result =  new QualifiedName(pkg, className);
    if (alert("experiment")) {
    

//        QualifiedName result = qualifiedName;
        do {
          if (language != Language.PYTHON)
            break;

          if (!packageContainsGen(result.packagePath()))
            break;

          String pkgElement = "." + convertCamelToUnderscore(result.className());
          if (result.packagePath().endsWith(pkgElement)) {
            pr("*** package path seems to already include class name, which is unexpected:", INDENT,
                result);
            break;
          }
          result = result.withPackageName(result.packagePath() + pkgElement);
        } while (false);

      
     // result = ParseTools.extraUpdateForPython(result);
    }
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
