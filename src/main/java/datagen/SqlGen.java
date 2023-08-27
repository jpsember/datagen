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

  public SqlGen(DatagenConfig config) {
    mConfig = config;
    //alertVerbose();
  }

  public void prepare() {
    log("prepare");
    incState(1);
    mGlobalDbVar = uniqueVar("db");
    mGlobalLockVar = uniqueVar("dbLock");
    varCode() .a("var ", mGlobalDbVar, " *sql.DB", CR);
 varCode() .a("var ", mGlobalLockVar, " *sync.Mutex", CR);

  }

  private String mGlobalDbVar;
  private String mGlobalLockVar;

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

  public void setTypeDef(GeneratedTypeDef typeDef) {
    clearItemsForNewClass();
    todo("clean up lifecycle of SqlGen");
    mGeneratedTypeDef = typeDef;
    var pkg = determinePackage();
    if (mPackageExpr == null) {
      mPackageExpr = pkg;
      log("package expr:", pkg);
    }
    checkState(pkg.equals(mPackageExpr));
  }

  public void generate() {
    log("generate, active:", mActive);
    assertState(1);
    if (!mActive)
      return;

    //setTypeDef(generatedTypeDef);

    createTable();
    createRecord();
    updateRecord();
    readRecord();

    indexFunc();

    pr("...clearing generatedTypeDef to null");
    mGeneratedTypeDef = null;
  }

  private void clearItemsForNewClass() {
    todo("this is error prone; have a separate container class that gets rebuilt");
    mCachedObjName = null;
    mCachedObjNameGo = null;
    mIndexes = arrayList();
  }

  private void indexFunc() {
    pr("indexFunc, index len:", mIndexes.size());
    for (var info : mIndexes) {
      checkState(info.mFieldNames.size() == 1, "unexpected number of fields");
      var fieldName = info.mFieldNames.get(0);
      readByField(fieldName);
    }
  }

  public void readByField(String fieldName) {
    var d = mGeneratedTypeDef;
    var s = sourceBuilder();

    todo("probably a bunch of duplicated variables here, make them fields");

    var objNameGo = d.qualifiedName().className();
    var objName = DataUtil.convertCamelCaseToUnderscores(objNameGo);
    var stName = "stmtReadByField" + objNameGo;

    varCode().a("var ", stName, " *sql.Stmt", CR);

    {
      initCode2().a(stName, " = CheckOkWith(db.Prepare(`SELECT id FROM ", objName, " WHERE ", fieldName,
          " = ?`))", CR);
    }

    var scanFuncName = "scanByField" + fieldName + objNameGo;
    var addScanFunc = firstTimeInSet(scanFuncName);
    if (addScanFunc) {
      generateScanByFieldFunc(d, s, scanFuncName);
    }

    FieldDef our = null;
    for (var fd : d.fields()) {
      if (fd.name().equals(fieldName)) {
        our = fd;
      }
    }
    checkState(our != null, "can't find field with name:", fieldName);
    var fieldTypeStr = our.dataType().qualifiedName().className();

    s.a("func Read", objNameGo, "With", DataUtil.capitalizeFirst(fieldName),
        "(objValue ", fieldTypeStr, ") (int, error)", OPEN);

    s.a("rows := ", stName, ".QueryRow(objValue)", CR, //
        "result, err := ", scanFuncName, "(rows)", CR, //
        "return result, err", CLOSE);
    todo("why is there no space between previous code?");
    todo("we should be formatting this generated function as well");
    addChunk(s);
  }

  private void generateScanByFieldFunc(GeneratedTypeDef d, SourceBuilder s, String funcName) {
    s.a("func ", funcName, "(rows *sql.Row) (int, error)", OPEN);
    s.a("var id int", CR);
    s.a("err := rows.Scan(&id)", CR);
    s.a("if err ==  sql.ErrNoRows", OPEN, "err = ObjectNotFoundError", CLOSE);
    s.a("return id, err", CLOSE);
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

    constructTables();
    createIndexes();

    includeVars();
    includeMiscCode();

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
  }

  private void generateLockAndDeferUnlock(SourceBuilder s) {
    s.a(mGlobalLockVar,".Lock()", CR, //
        "defer ",mGlobalLockVar,".Unlock()", CR);
    }
    
  private void updateRecord() {
    var s = sourceBuilder();
    var d = mGeneratedTypeDef;

    createConstantOnce(s, "var ObjectNotFoundError = errors.New(`object with requested id not found`)");

    var objNameGo = d.qualifiedName().className();
    var objName = DataUtil.convertCamelCaseToUnderscores(objNameGo);
    var stName = uniqueVar("stmtUpdate");

    varCode().a("var ", stName, " *sql.Stmt", CR);

    s.a("func Update", objNameGo, "(obj ", objNameGo, ") error", OPEN);

    generateLockAndDeferUnlock(s);
 
    List<FieldDef> filtFields = arrayList();

    {
      var t = initCode2();
      t.a(stName, " = CheckOkWith(db.Prepare(`UPDATE ", objName, " SET ");

      t.startComma();
      for (var fieldDef : d.fields()) {
        if (fieldDef.name().equals("id"))
          continue;
        t.comma();
        filtFields.add(fieldDef);
        t.a(fieldDef.name(), " = ?");
      }
      t.endComma();
      t.a(" WHERE id = ?`))", CR);
    }

    s.a("var err error", CR);
    s.a("for", OPEN, //
        "result, err1 := ", stName, ".Exec(");

    s.startComma();
    for (var fieldDef : filtFields) {
      s.comma();
      s.a("obj.", fieldDef.getterName(), "()");
    }
    s.comma();
    s.a("obj.Id()").endComma().a(")", CR);
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

    varCode().a("var ", stName, " *sql.Stmt", CR);

    {
      initCode2().a(stName, " = CheckOkWith(db.Prepare(`SELECT * FROM ", objName, " WHERE id = ?`))", CR);
    }

    var scanFuncName = "scan" + objNameGo;
    var addScanFunc = firstTimeInSet(scanFuncName);
    if (addScanFunc) {
      generateScanFunc(d, s, objNameGo, objName, scanFuncName);
    }

    s.a("func Read", objNameGo, "(objId int) (", objNameGo, ", error)", OPEN);

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

    var fnName = "createTable" + tableNameGo;
    mCreateTableFnNames.add(fnName);
    mCreateTableCalls.a(fnName, "()", CR);

    s.a("func ", fnName, "()", OPEN, //
        " _, err := ",mGlobalDbVar,".Exec(`CREATE TABLE IF NOT EXISTS ", tableName, " (", CR);

    var i = INIT_INDEX;
    s.startComma();
    for (FieldDef f : d.fields()) {
      i++;
      s.comma();
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
    s.endComma();
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

    varCode().a("var ", stName, " *sql.Stmt", CR);

    s.a("func Create", objNameGo, "(obj ", objNameGo, ") (", objNameGo, ", error)",
        OPEN);

    List<FieldDef> filtFields = arrayList();

    {
      var t = initCode2();
      t.a(stName, " = CheckOkWith(db.Prepare(`INSERT INTO ", objName, " (");

      t.startComma();
      for (var fieldDef : d.fields()) {
        if (fieldDef.name().equals("id"))
          continue;
        t.comma();
        filtFields.add(fieldDef);
        t.a(fieldDef.name());
      }
      t.endComma();
      t.a(") VALUES(");
      t.startComma();
      for (int i = 0; i < filtFields.size(); i++) {
        t.comma();
        t.a("?");
      }
      t.endComma();
      t.a(")`))", CR);
    }

    s.a("var err error", CR, //
        "var createdObj ", objNameGo, CR, //
        "result, err1 := ", stName, ".Exec(");

    s.startComma();
    for (var f : filtFields) {
      s.a(COMMA, "obj.");
      s.a(f.getterName(), "()");
      todo("we may need to convert getter output to something else, e.g. string or int");
    }
    s.endComma();
    s.a(")", CR);

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
    IndexInfo info = new IndexInfo();
    info.typeName2 = objName();
    info.tableName2 = tableNameGo();
    info.mFieldNames.addAll(fields);
    mIndexes.add(info);
  }

  private SourceBuilder varCode() {
    return mMiscVar;
  }

  private SourceBuilder initCode1() {
    return mInitFunctionCode1;
  }

  private SourceBuilder initCode2() {
    return mInitFunctionCode2;
  }

  private SourceBuilder mMiscVar = sourceBuilder();
  private SourceBuilder mInitFunctionCode1 = sourceBuilder();
  private SourceBuilder mInitFunctionCode2 = sourceBuilder();
  private SourceBuilder mCreateTableCalls = sourceBuilder();
  private List<String> mCreateTableFnNames = arrayList();

  private String uniqueVar(String prefix) {
    mUniqueVarCounter++;
    return prefix + mUniqueVarCounter;
  }

  private int mUniqueVarCounter;

  private String objName() {
    if (mCachedObjName == null) {
      mCachedObjName = DataUtil.convertCamelCaseToUnderscores(objNameGo());
    }
    return mCachedObjName;
  }

  private String mCachedObjName;

  private String objNameGo() {
    if (mCachedObjNameGo == null) {
      mCachedObjNameGo = mGeneratedTypeDef.qualifiedName().className();
    }
    return mCachedObjNameGo;
  }

  private String mCachedObjNameGo;

  private Set<String> mUniqueIndexNames = hashSet();

  private void constructTables() {
    for (var x : mCreateTableFnNames) {
      initCode1().a(x, "()", CR);
    }
  }

  private void createIndexes() {
    for (var fields : mIndexes) {
      var indexName = fields.typeName2 + "_" + String.join("_", fields.mFieldNames);
      checkState(mUniqueIndexNames.add(indexName), "duplicate index:", indexName);
      var s = initCode1();
      s.a("CheckOkWith(db.Exec(`CREATE UNIQUE INDEX IF NOT EXISTS ", indexName, " ON ", fields.tableName2,
          " (");
      s.startComma();
      for (var fn : fields.mFieldNames) {
        s.a(COMMA, fn);
      }
      s.endComma().a(")`))", CR);
    }
  }

  private void includeVars() {
    append(mCode, varCode(), "Variables");
  }

  private String auxAppend(SourceBuilder source, Object... comments) {
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
    target.addParagraph(c);

  }

  private void includeMiscCode() {
    var t = initCode1();
    if (!t.isEmpty()) {
      var s = sourceBuilder();
      s.a("func PrepareDatabase(db *sql.DB, lock *sync.Mutex)", OPEN);
      s.a(mGlobalDbVar, " = db", CR, //
          mGlobalLockVar, " = lock", CR);

      append(s, mInitFunctionCode1);
      append(s, mInitFunctionCode2);
      s.a(CLOSE);
      append(mCode, s);
    }
  }

  private Set<String> unique = hashSet();

  private int mState;

  private String mPackageExpr;
  private boolean mActive;
  private boolean mWasActive;
  private File mCachedDir;
  private StringBuilder mCode = new StringBuilder();
  private List<IndexInfo> mIndexes;
  private GeneratedTypeDef mGeneratedTypeDef;

  private static class IndexInfo {
    String typeName2;
    String tableName2;
    List<String> mFieldNames = arrayList();
  }

  /* private */ DatagenConfig mConfig;

}
