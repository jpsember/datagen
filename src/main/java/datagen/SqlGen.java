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
    incState(1);
    mConfig = config;
    
    mDir = config.sqlDir();
    if (Files.empty(mDir))
      return;
    if (!Context.sql.Active)
      return;
    todo("if clean, delete sql product directory (but not here)");
    Files.S.mkdirs(mDir);
    
    
    pr(VERT_SP,"prepare:",config);
    
    
    
    
    
    
    
    
    
    
    
    
    
    
  }
  
  private DatagenConfig mConfig;
private String mPackageExpr;
  

private String determinePackage(GeneratedTypeDef def) {
  String pkgName = def.qualifiedName().packagePath();
  checkArgument(!pkgName.isEmpty(), "Package name is empty");
  pkgName = QualifiedName.lastComponent(pkgName);
  return "package " + pkgName;
}

private void setTypeDef(GeneratedTypeDef typeDef) {
     
  var pkg = determinePackage(typeDef);
  pr("setTypeDef:",typeDef,"pkg expr:",pkg);
  if (mPackageExpr == null) {
    mPackageExpr = pkg;
  }

  checkState(pkg.equals(mPackageExpr));
}

  public void generate(GeneratedTypeDef typeDef) {
    assertState(1);
    setTypeDef(typeDef);
    
//    mDir = mConfig.sqlDir();
//    if (Files.empty(mDir))
//      return;
//    if (!Context.sql.Active)
//      return;
//    todo("if clean, delete sql product directory (but not here)");
//    Files.S.mkdirs(mDir);

    if (TableFlag) {
      var g = new SqlCreateTable();
      g.generate();
    }

  }

  public File directory() {
    return mDir;
  }

  private void incState(int expectedNew) {
    assertState(expectedNew-1);
    mState = expectedNew;
  }
  
  private void assertState(int expectedCurrent) {
    checkState(mState == expectedCurrent);
  }
  
  public void complete() {
    incState(2);
    
//  
//  
//  
//  //GeneratedTypeDef def = Context.generatedTypeDef;
  
    JSMap m = map();
  m.put("package_decl", mPackageExpr);
  
  
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
//  // Perform pass 1 of macro substitution
//  //
//  {
//    MacroParser parser = new MacroParser();
//    //parser.alertVerbose();
//    parser.withTemplate(content).withMapper(m);
//    content = parser.content();
//  }
//
//  if (false && alert("showing content")) {
//    pr(DASHES, CR, "Content after pass 1:", CR, DASHES, CR, content);
//  }
//
//  // Pass 2: extract import expressions
//  //
//  content = extractImportExpressions(content);
//
//  // Pass 3: generate the import statements
//  //
//  m.clear();
//  m.put("imports", generateImports(mImportExpressions));
//  {
//    MacroParser parser = new MacroParser();
//    parser.withTemplate(content).withMapper(m);
//    content = parser.content();
//  }
//
//  // Pass 4: Strip (or retain) optional comments.  Such comments are denoted by a line with the prefix "@@"
//  //
//  content = ParseTools.processOptionalComments(content, Context.config.comments());
//
//  //
//  // Pass 5: remove extraneous linefeeds; insert requested blank lines according to language.  
//  //         For Python, lines starting with "\\[n]" indicate that n blank lines are to be placed between 
//  //         the neighboring (non-blank) lines
//  //
//  content = Context.pt.adjustLinefeeds(content);
//  File target = sourceFile();
//  Context.files.mkdirs(Files.parent(target));
//  boolean wrote = Context.files.writeIfChanged(target, content);
//  Context.generatedFilesSet.add(target);
//  if (wrote)
//    log(".....updated:", sourceFileRelative());
//  else {
//    target.setLastModified(System.currentTimeMillis());
//    log("...freshened:", sourceFileRelative());
//  }
//
//  postGenerate();
//  
  

  }
  

  private static String sGoSourceTemplate = Files.readString(SourceGen.class, "db_template_go.txt");

  private int mState;
  public boolean Active;
  public boolean TableFlag;
  private File mDir;

}
