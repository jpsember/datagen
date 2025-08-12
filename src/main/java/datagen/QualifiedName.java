package datagen;

import js.base.BaseObject;
import js.json.JSMap;

import static js.base.Tools.*;
import static datagen.Context.*;

public final class QualifiedName extends BaseObject {

  public static QualifiedName parse(String expr) {
    return parse(expr, null);
  }

  public static QualifiedName parse(String expr, String defaultPackage) {

    if (Context.rust()) {
      checkArgument(!expr.contains(":"), "Convert '::' expressions to '.'");
      expr = expr.replace('/', '.');

      String packagePath = "";
      String className = "";

      var a = expr.lastIndexOf('.');
      className = expr.substring(a + 1);
      if (a > 0)
        packagePath = expr.substring(0, a);

      pmod("packagePath now:", packagePath);
      if (packagePath.startsWith("gen.") || packagePath.equals("gen"))
        packagePath = "crate." + packagePath;
      else if (defaultPackage != null && defaultPackage.startsWith("crate.")) {
        packagePath = defaultPackage;
      }

      QualifiedName result = new QualifiedName(packagePath, className);
      pmod("result:",INDENT,result);

      return result;
    }

    if (go()) {
      var a = expr.lastIndexOf('/');
      var b = expr.lastIndexOf('.');
      var nameStartPos = Math.max(a, b);
      if (nameStartPos == 0 || nameStartPos == expr.length() - 1)
        throw badArg("from expr:", quote(expr));
      String packagePath = expr.substring(0, Math.max(0, nameStartPos));
      packagePath = ifNullOrEmpty(packagePath, nullToEmpty(defaultPackage));
      String className = expr.substring(1 + nameStartPos);
      QualifiedName result = new QualifiedName(packagePath, className);
      return result;
    }

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
    if (python())
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
    // If no embedded name is defined yet, set it to the default: {{combined|class_name}}
    if (mEmbeddedName == null) {
      String arg = combined();
      String prefix = arg;
      String suffix = "";
      switch (language()) {
        case JAVA:
          // If there's a type parameter <xxx>, use only the text preceding it as the embedded type expression
        {
          int i = arg.indexOf('<');
          if (i >= 0) {
            prefix = arg.substring(0, i);
            suffix = arg.substring(i);
          }
        }
        break;
        case PYTHON:
          // Handle list types
        {
          int i = arg.indexOf('[');
          if (i >= 0) {
            prefix = arg.substring(0, i);
            suffix = arg.substring(i);
          }
        }
        break;
        default:
          break;
      }

      String typeName = ParseTools.importedClassExpr(prefix).toString();
      withEmbeddedName(typeName + suffix);
    }
    return mEmbeddedName;
  }

  public String combined() {
    if (mCachedCombined == null) {
      String combined = className();
      if (!packagePath().isEmpty()) {
        combined = packagePath() + "." + className();
      }
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

  public static String lastComponent(String pkgName) {
    int i = pkgName.lastIndexOf('/');
    return pkgName.substring(i + 1);
  }

  private final String mPackagePath;
  private final String mClassName;
  private String mEmbeddedName;
  private String mCachedCombined;

}
