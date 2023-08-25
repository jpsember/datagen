package datagen;

import java.io.File;

import datagen.gen.DatagenConfig;
import js.base.BaseObject;
import js.file.Files;
import js.json.JSMap;
import js.parsing.MacroParser;

import static js.base.Tools.*;

public class SqlGen extends BaseObject {

  public void prepare(DatagenConfig config) {
    alertVerbose();
    incState(1);
    mConfig = config;
  }

  public void setActive(boolean state) {
    log("setActive:", state);
    assertState(1);
    mActive = state;
    if (state)
      mWasActive = true;
  }

  public boolean active() {
    return mActive;
  }

  private String determinePackage(GeneratedTypeDef def) {
    String pkgName = def.qualifiedName().packagePath();
    checkArgument(!pkgName.isEmpty(), "Package name is empty");
    pkgName = QualifiedName.lastComponent(pkgName);
    return "package " + pkgName;
  }

  private void setTypeDef(GeneratedTypeDef typeDef) {

    var pkg = determinePackage(typeDef);
    if (mPackageExpr == null) {
      mPackageExpr = pkg;
      log("package expr:", pkg);
    }

    checkState(pkg.equals(mPackageExpr));
  }

  public void generate(GeneratedTypeDef typeDef) {
    log("generate, active:", mActive);
    assertState(1);
    if (!mActive)
      return;

    setTypeDef(typeDef);

    //    mDir = mConfig.sqlDir();
    //    if (Files.empty(mDir))
    //      return;
    //    if (!Context.sql.Active)
    //      return;
    //    todo("if clean, delete sql product directory (but not here)");
    //    Files.S.mkdirs(mDir);

    //var g = new SqlCreateTable();
    mCode.append(new SqlCreateTable().generate(typeDef));
    mCode.append("\n");
  }

  public File directory() {
    if (mCachedDir == null) {
      // Determine where the go source files were written, in order to place
      // the sql source file there as well
      File sample = Context.generatedFilesSet.iterator().next();
      mCachedDir = sample.getParentFile();
      checkState(mCachedDir.exists());
      todo("if clean, delete sql product directory (but not here)");
    }

    return mCachedDir;
  }

  private void incState(int expectedNew) {
    assertState(expectedNew - 1);
    mState = expectedNew;
  }

  private void assertState(int expectedCurrent) {
    checkState(mState == expectedCurrent);
  }

  public void complete() {
    log("complete, was active:", mWasActive);
    incState(2);
    if (!mWasActive)
      return;
    //  
    //  
    //  
    //  //GeneratedTypeDef def = Context.generatedTypeDef;

    JSMap m = map();
    m.put("package_decl", mPackageExpr);
    m.put("code", mCode.toString());

    //  // In this first pass, leave the imports macro unchanged
    //  m.put("imports", "[!imports]");
    //  m.put("class", def.name());
    //
    //  String content = getTemplate();
    //  m.put("deprecated", def.isDeprecated() ? getDeprecationSource() : "");
    //   if (def.isEnum()) {
    //    generateEnumValues(def.enumDataType());
    //    m.put("default_value", def.enumDataType().labels().get(0));
    //    m.put("enum_values", content());
    //    addAdditionalTemplateValues(m);
    //  } else {
    //    m.put("class_getter_implementation", generateGetters());
    //    m.put("copy_to_builder", generateImmutableToBuilder());
    //    m.put("copyfield_from_builder", generateCopyFromBuilderToImmutable());
    //    m.put("equals", generateEquals());
    //    m.put("hashcode", generateHashCode());
    //    m.put("init_instance_fields", generateInitInstanceFields());
    //    m.put("instance_fields", generateInstanceFields());
    //    m.put("parse", generateParse());
    //    m.put("setters", generateSetters());
    //    m.put("string_constants", generateStringConstants());
    //    m.put("to_json", generateToJson());
    //    m.put("to_string", generateToString());
    //    addAdditionalTemplateValues(m);
    //  }
    //
    //  // Get any source that DataTypes may have needed to add;
    //  // must be added here, after all other keys
    //  m.put("class_specific", def.getClassSpecificSource());
    //

    // Perform  macro substitution
    //
    String content = null;
    {
      MacroParser parser = new MacroParser();
      //parser.alertVerbose();
      String template = Files.readString(SourceGen.class, "db_template_go.txt");
      parser.withTemplate(template).withMapper(m);
      content = parser.content();
    }

    //  Strip (or retain) optional comments.  Such comments are denoted by a line with the prefix "@@"
    //  
    content = ParseTools.processOptionalComments(content, Context.config.comments());
    //
    //  //
    //  // Pass 5: remove extraneous linefeeds; insert requested blank lines according to language.  
    //  //         For Python, lines starting with "\\[n]" indicate that n blank lines are to be placed between 
    //  //         the neighboring (non-blank) lines
    //  //
    content = Context.pt.adjustLinefeeds(content);
    File target = new File(directory(), "db.go");
    alert("writing:", target);

    Context.files.mkdirs(Files.parent(target));
    boolean wrote = Context.files.writeIfChanged(target, content);
    // Context.generatedFilesSet.add(target);
    if (wrote)
      log(".....updated:", target);
    else {
      target.setLastModified(System.currentTimeMillis());
      log("...freshened:", target);
    }
    //
    //  postGenerate();
    //  

  }

  //  private static String sGoSourceTemplate = Files.readString(SourceGen.class, "db_template_go.txt");

  private int mState;

  private String mPackageExpr;
  private boolean mActive;
  private boolean mWasActive;
  private File mCachedDir;
  private StringBuilder mCode = new StringBuilder();
  private DatagenConfig mConfig;
}
