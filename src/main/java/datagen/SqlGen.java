package datagen;

import java.io.File;
import java.util.List;
import java.util.Set;

import datagen.gen.DatagenConfig;
import datagen.gen.Language;
import js.base.BaseObject;
import js.base.BasePrinter;
import js.data.DataUtil;
import js.file.Files;
import js.json.JSMap;
import js.parsing.MacroParser;

import static datagen.SourceBuilder.*;
import static js.base.Tools.*;

public class SqlGen extends BaseObject {

  public void prepare(DatagenConfig config) {
    //alertVerbose();
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

  private String determinePackage() {
    String pkgName = mGeneratedTypeDef.qualifiedName().packagePath();
    checkArgument(!pkgName.isEmpty(), "Package name is empty");
    pkgName = QualifiedName.lastComponent(pkgName);
    return "package " + pkgName;
  }

  private void setTypeDef(GeneratedTypeDef typeDe) {
    mGeneratedTypeDef = typeDe;
    var pkg = determinePackage();
    if (mPackageExpr == null) {
      mPackageExpr = pkg;
      log("package expr:", pkg);
    }

    checkState(pkg.equals(mPackageExpr));
  }

  public void generate(GeneratedTypeDef generatedTypeDef) {
    log("generate, active:", mActive);
    assertState(1);
    if (!mActive)
      return;

    setTypeDef(generatedTypeDef);

    createTable();
    createRecord();
    updateRecord();
    readRecord();
    createIndexes();

    includeVars();
    includeMiscCode();

    generatedTypeDef = null;
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

  private void updateRecord() {
    var s = sourceBuilder();
    var d = mGeneratedTypeDef;

    createConstantOnce(s, "var ObjectNotFoundError = errors.New(`object with requested id not found`)");

    var objNameGo = d.qualifiedName().className();
    var objName = DataUtil.convertCamelCaseToUnderscores(objNameGo);
    var stName = "stmtUpdate" + objNameGo;

    createConstantOnce(s, "var " + stName + " *sql.Stmt");

    s.a("func Update", objNameGo, "(db *sql.DB, obj ", objNameGo, ") error", OPEN);

    s.a("if ", stName, " == nil ", OPEN, //
        stName, " = CheckOkWith(db.Prepare(`UPDATE ", objName, " SET "); //INSERT INTO ", objName, " (");

    boolean needComma = false;

    List<FieldDef> filtFields = arrayList();

    for (var fieldDef : d.fields()) {
      if (fieldDef.name().equals("id"))
        continue;
      filtFields.add(fieldDef);

      if (needComma) {
        s.a(", ");
      }
      needComma = true;
      s.a(fieldDef.name(), " = ?");
    }
    s.a(" WHERE id = ?`))", CLOSE);

    s.a("var err error", CR);
    s.a("for", OPEN, //
        "result, err1 := ", stName, ".Exec(");

    needComma = false;
    for (var fieldDef : filtFields) {
      if (needComma) {
        s.a(", ");
      }
      needComma = true;
      s.a("obj.", fieldDef.getterName(), "()");
    }
    s.a(", obj.Id())", CR);
    s.a("err = err1", CR);
    s.a("if err != nil { break }", CR);

    s.a("count, err2 := result.RowsAffected()", CR, //
        "err = err2", CR, //
        "if err != nil { break } ", CR, //
        "if count != 1 { err = ObjectNotFoundError }", CR, //
        "break", CLOSE);
    s.a("return err", CLOSE);
    addChunk(s);
  }

  private SourceBuilder sourceBuilder() {
    return new SourceBuilder(Language.GO);
  }

  private void readRecord() {
    var d = mGeneratedTypeDef;
    var s = sourceBuilder();

    createConstantOnce(s, "var ObjectNotFoundError = errors.New(`object with requested id not found`)");

    var objNameGo = d.qualifiedName().className();
    var objName = DataUtil.convertCamelCaseToUnderscores(objNameGo);
    var stName = "stmtRead" + objNameGo;

    createConstantOnce(s, "var " + stName + " *sql.Stmt");

    var scanFuncName = "scan" + objNameGo;
    var addScanFunc = firstTimeInSet(scanFuncName);
    if (addScanFunc) {
      generateScanFunc(d, s, objNameGo, objName, scanFuncName);
    }

    s.a("func Read", objNameGo, "(db *sql.DB, objId int) (", objNameGo, ", error)", OPEN);

    s.a("if ", stName, " == nil ", OPEN, //
        stName, " = CheckOkWith(db.Prepare(`SELECT * FROM ", objName, " WHERE id = ?`))", CLOSE);

    s.a("rows := ", stName, ".QueryRow(objId)", CR, //
        "result, err := ", scanFuncName, "(rows)", CR, //
        "return result, err", CLOSE);
    addChunk(s);
  }

  private void generateScanFunc(GeneratedTypeDef d, SourceBuilder s, String objNameGo, String objName,
      String funcName) {

    s.a("func ", funcName, "(rows *sql.Row) (", objNameGo, "Builder, error)", OPEN);

    s.a("var b ", objNameGo, "Builder", CR);

    List<String> fieldNames = arrayList();
    List<FieldDef> filtFields = d.fields();
    for (FieldDef fieldDef : filtFields) {

      var fn = DataUtil.convertCamelCaseToUnderscores(fieldDef.name());
      fieldNames.add(fn);
      s.a("var ", fn, " ", fieldDef.dataType().qualifiedName().className(), CR);
    }

    s.a("err := rows.Scan(");
    boolean needComma = false;
    for (var v : fieldNames) {
      if (needComma) {
        s.a(", ");
      }
      needComma = true;
      s.a("&", v);
    }
    s.a(")", CR);

    s.a("if err ==  sql.ErrNoRows", OPEN, "err = ObjectNotFoundError } else {", CR, //
        "if err == nil", OPEN, //
        "b = New", objNameGo, "()", CR);
    var i = INIT_INDEX;
    for (var v : filtFields) {
      i++;
      s.a("b.", v.setterName(), "(", fieldNames.get(i), ")", CR);
    }
    s.a(CLOSE);
    s.a(CLOSE);

    s.a("return b, err", CLOSE);

    //func (db Database) scanUser(rows *sql.Row) UserBuilder {
    //
    //b := NewUser()
    //
    //var id int
    //var name string
    //var user_state string
    //var user_class string
    //var email string
    //var password string
    //
    //errHolder := NewErrorHolder()
    //
    //err := rows.Scan(&id, &name, &user_state, &user_class, &email, &password)
    //if err != sql.ErrNoRows {
    //  errHolder.Add(err)
    //  b = NewUser()
    //  b.SetId(id)
    //  b.SetName(name)
    //  b.SetState(UserState(UserStateEnumInfo.FromString(user_state, errHolder)))
    //  b.SetEmail(email)
    //  b.SetPassword(password)
    //}
    //db.setError(errHolder.First())
    //return b
    //}

  }

  private boolean createConstantOnce(SourceBuilder s, String expr) {
    if (firstTimeInSet(expr)) {
      s.br().a(expr).br();
      return true;
    }
    return false;
  }

  private boolean firstTimeInSet(String object) {
    return unique.add(object);
  }

  private String tableNameSql() {
    if (mCachedTableNameSql == null) {
      mCachedTableNameSql = DataUtil.convertCamelCaseToUnderscores(tableNameGo());
    }
    return mCachedTableNameSql;
  }

  private String mCachedTableNameSql;

  private String tableNameGo() {
    if (mCachedTableNameGo == null) {
      mCachedTableNameGo = mGeneratedTypeDef.qualifiedName().className();
    }
    return mCachedTableNameGo;
  }

  private String mCachedTableNameGo;

  private void createTable() {
    var s = sourceBuilder();

    var d = mGeneratedTypeDef;

    var tableNameGo = d.qualifiedName().className();
    var tableName = DataUtil.convertCamelCaseToUnderscores(tableNameGo);

    s.a("func CreateTable", tableNameGo, "(db *sql.DB)", OPEN, //
        " _, err := db.Exec(`CREATE TABLE IF NOT EXISTS ", tableName, " (", CR);

    var i = INIT_INDEX;
    for (FieldDef f : d.fields()) {
      i++;
      if (i != 0) {
        s.a(",", CR);
      }
      var name = f.name();
      String sqlType = f.dataType().sqlType();
      boolean isId = name.equals("id");
      if (isId) {
        if (!sqlType.equals("INTEGER"))
          badState("id doesn't look like an integer: ", f.name(), f.dataType().qualifiedName().className(),
              sqlType);
        checkState(i == 0, "'id' should be first field");
      }
      s.a(name, " ");
      checkArgument(!sqlType.startsWith("!!!"), "no sql type for", f.name(), ";", f.dataType().getClass());
      s.a(sqlType);
      if (isId) {
        s.a(" PRIMARY KEY");
      }

    }
    s.cr();
    s.a(");`)").cr();
    s.a("  CheckOk(err, \"failed to create table\")", CR, //
        CLOSE);
    addChunk(s);
  }

  private void createRecord() {
    var d = mGeneratedTypeDef;
    var s = sourceBuilder();

    var objNameGo = d.qualifiedName().className();
    var objName = DataUtil.convertCamelCaseToUnderscores(objNameGo);
    var stName = "stmtCreate" + objNameGo;

    s.a("var ", stName, " *sql.Stmt", CR);

    s.a("func Create", objNameGo, "(db *sql.DB, obj ", objNameGo, ") (", objNameGo, ", error)", OPEN);

    s.a("Pr(`Create:`,obj)", CR);

    s.a("if ", stName, " == nil ", OPEN, //
        stName, " = CheckOkWith(db.Prepare(`INSERT INTO ", objName, " (");

    boolean needComma = false;

    List<FieldDef> filtFields = arrayList();

    for (var fieldDef : d.fields()) {
      if (fieldDef.name().equals("id"))
        continue;
      filtFields.add(fieldDef);
      if (needComma) {
        s.a(", ");
      }
      needComma = true;
      s.a(fieldDef.name());
    }
    s.a(") VALUES(");
    for (int i = 0; i < filtFields.size(); i++) {
      if (i > 0)
        s.a(",");
      s.a("?");
    }
    s.a(")`))", CLOSE);

    s.a("var err error", CR, //
        "var createdObj ", objNameGo, CR, //

        "result, err1 := ", stName, ".Exec(");

    needComma = false;
    for (var f : filtFields) {
      if (needComma) {
        s.a(", ");
      }
      needComma = true;
      s.a("obj.");
      s.a(f.getterName(), "()");
      todo("we may need to convert getter output to something else, e.g. string or int");
    }
    s.a(")", CR);

    s.a("Pr(`execd, result:`,createdObj,`err:`,err1)", CR);

    s.a("err = err1", CR, "if err == nil", OPEN);
    {
      s.a("id, err2 := result.LastInsertId()", CR, //
          "err = err2", CR, "if err == nil", OPEN, //
          "createdObj = obj.ToBuilder().SetId(int(id)).Build()", CLOSE);
    }
    s.a(CLOSE);
    s.a("return createdObj, err", CLOSE);
    addChunk(s);
  }

  private void addChunk(SourceBuilder sb) {
    mCode.append(sb.getContent());
    mCode.append("\n\n");
  }

  public void addIndex(List<String> fields) {
    mIndexes.add(fields);
  }

  private SourceBuilder varCode() {
    return mMiscVar;
  }


  private SourceBuilder dbInitCode() {
    return mDbInitCode;
  }

  private SourceBuilder initCode() {
    return mInitCode;
  }

//  private SourceBuilder mCreateTable = sourceBuilder();
//  private SourceBuilder mCreateIndex = sourceBuilder();

  private SourceBuilder mMiscVar = sourceBuilder();
  private SourceBuilder mInitCode = sourceBuilder();
  private SourceBuilder mDbInitCode = sourceBuilder();

  
  //  private SourceBuilder mMiscVarSourceBuilder;
  //private SourceBuilder mMiscInitCodeSourceBuilder;

  private String uniqueVar(String prefix) {
    mUniqueVarCounter++;
    return prefix + mUniqueVarCounter;
  }

  private int mUniqueVarCounter;

  private String objName() {
    if (mObjName == null) {
      mObjName = DataUtil.convertCamelCaseToUnderscores(objNameGo());
    }
    return mObjName;
  }

  private String mObjName;

  private String objNameGo() {
    if (objNameGo == null) {
      objNameGo = mGeneratedTypeDef.qualifiedName().className();
    }
    return objNameGo;
  }

  private String objNameGo;

  private Set<String> mUniqueIndexNames = hashSet();

  private void createIndexes() {

    for (var fields : mIndexes) {

      var indexName = "index_" + objName() + "_" + String.join("_", fields);
      checkState(mUniqueIndexNames.add(indexName), "duplicate index:", indexName);

      var s = sourceBuilder();

      var sqlString = uniqueVar("createIndexStatement");
      s.a("var ", sqlString, " = `CREATE UNIQUE INDEX IF NOT EXISTS ", indexName, " ON ", tableNameSql(),
          " (");
      s.startComma();
      for (var fn : fields) {
        s.a(COMMA, fn);
      }
      s.endComma().a(")`");
      var goStatement = s.cr().getContent();
      varCode().addSafe(goStatement);
      
      s = dbInitCode();
      s.a("_,err = database.Exec(",sqlString,")",CR, //
          "CheckOk(err)",CR
          );
    }
  }

  private void includeVars() {
    append(mCode,varCode(),"Variables");
  }

  private String auxAppend(SourceBuilder source, Object... comments) {
//    pr("aux append, source empty:",source.isEmpty(),"comments:",BasePrinter.toString(comments));
    if (source.isEmpty())
      return "";
    StringBuilder sb = new StringBuilder();
    if (comments.length > 0) {
      var s = BasePrinter.toString(comments);
      var lines = split(s, '\n');
      for (var ln : lines) {
        sb.append("@@// ");
        sb.append(ln);
        sb.append('\n');
      }
    }
    sb.append(source.getContent());
    addLF(sb);
    sb.append('\n');
   
    return sb.toString();
  }

  private void append(StringBuilder target, SourceBuilder source, Object... comments) {
    var c = auxAppend(source, comments);
    if (c.isEmpty())
      return;
    target.append(c);
  }

  private void append(SourceBuilder target, SourceBuilder source, Object... comments) {
    var c = auxAppend(source, comments);
    if (c.isEmpty())
      return;
    target.addSafe(c);
  }

  private void includeMiscCode() {
//    append(initCode(), mCreateTable,"Table creation");
//    append(initCode(), mCreateIndex,"Index creation");
    append(mCode,initCode() );
    
    if (!mDbInitCode.isEmpty()) {
      var s = sourceBuilder();
      s.a("func PrepareDatabase(db *sql.DB)",OPEN, //
          "var err error",CR //
          );
      
      append(s,mDbInitCode);
      s.a(CLOSE);
      append(mCode,s);
    }
  }

  private Set<String> unique = hashSet();

  private int mState;

  private String mPackageExpr;
  private boolean mActive;
  private boolean mWasActive;
  private File mCachedDir;
  private StringBuilder mCode = new StringBuilder();
  private List<List<String>> mIndexes = arrayList();
  private GeneratedTypeDef mGeneratedTypeDef;

  /* private */ DatagenConfig mConfig;

}
