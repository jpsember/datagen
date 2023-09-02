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
  }

  public void prepare() {
    log("prepare");
    incState(1);
  }

  public void setTypeDef(GeneratedTypeDef typeDef) {
    ci = new ClassInfo();
    mGeneratedTypeDef = typeDef;
    var pkg = determinePackage();
    if (mPackageExpr == null) {
      mPackageExpr = pkg;
      log("package expr:", pkg);
    }
    checkState(pkg.equals(mPackageExpr));
    setActive(true);
  }

  public void addIndex(List<String> fields) {
    IndexInfo info = new IndexInfo();
    info.typeName = ci.objName;
    info.tableName = mGeneratedTypeDef.qualifiedName().className();
    info.mFieldNames.addAll(fields);
    ci.ind.add(info);
  }

  public void generate() {
    log("generate, active:", mActive);
    assertState(1);
    if (!mActive)
      return;

    createTable();
    createRecord();
    updateRecord();
    readRecord();
    createIndexSpecific();
    indexFunc();

    mGeneratedTypeDef = null;
    setActive(false);
  }

  public void complete() {
    log("complete, was active:", mWasActive);
    incState(2);
    if (!mWasActive)
      return;

    constructTables();
    createIndexes();

    includeVars();

    JSMap m = map();
    m.put("package_decl", mPackageExpr);

    {
      var x = sourceBuilder();
      if (simulated()) {
      } else {
        x.a(" _ \"github.com/mattn/go-sqlite3\"", CR);
        x.a(" \"database/sql\"", CR);
      }
      m.put("additional_imports", x.getContent());
    }

    {
      var tn = simulated() ? "db_support_go_sim.txt" : "db_support_go_sqlite.txt";
      m.put("support", Files.readString(SourceGen.class, tn));
    }

    m.put("code", mCode.toString());

    m.put("init_code1", initCode1().getContent());
    m.put("init_code2", initCode2().getContent());

    // Keep performing macro substitution until the output doesn't change.
    // This is so we can embed macro keys within other macros' values.
    // 

    String template = Files.readString(SourceGen.class, "db_template_go.txt");
    String content = null;
    while (true) {
      MacroParser parser = new MacroParser();
      parser.withTemplate(template).withMapper(m);
      content = parser.content();
      if (content.equals(template))
        break;
      template = content;
    }

    //  Strip (or retain) optional comments.  Such comments are denoted by a line with the prefix "@@"
    //  
    content = ParseTools.processOptionalComments(content, Context.config.comments());

    content = Context.pt.adjustLinefeeds(content);
    File target = new File(directory(), "db.go");

    Context.files.mkdirs(Files.parent(target));
    boolean wrote = Context.files.writeIfChanged(target, content);
    Context.generatedFilesSet.add(target);
    if (wrote)
      log(".....updated:", target);
    else {
      target.setLastModified(System.currentTimeMillis());
      log("...freshened:", target);
    }
  }

  private void setActive(boolean state) {
    log("setActive:", state);
    assertState(1);
    mActive = state;
    if (state)
      mWasActive = true;
  }

  private String determinePackage() {
    String pkgName = mGeneratedTypeDef.qualifiedName().packagePath();
    checkArgument(!pkgName.isEmpty(), "Package name is empty");
    pkgName = QualifiedName.lastComponent(pkgName);
    return "package " + pkgName;
  }

  private static class ClassInfo {

    String objName;
    String objNameGo;
    String simTableName;
    List<IndexInfo> ind = arrayList();

    public ClassInfo() {
      var t = Context.generatedTypeDef;
      objNameGo = t.qualifiedName().className();
      objName = DataUtil.convertCamelCaseToUnderscores(objNameGo);
      simTableName = quote(objName);
    }

  }

  private ClassInfo ci;

  private void indexFunc() {
    for (var info : ci.ind) {
      checkState(info.mFieldNames.size() == 1, "unexpected number of fields");
      var fieldName = info.mFieldNames.get(0);
      readByField(fieldName);
    }
  }

  private void readByField(String fieldNameSnake) {

    if (simulated()) {
      var s = sourceBuilder();
      var fieldNameCamel = snakeToCamel(fieldNameSnake);

      s.a("// Read ", ci.objNameGo, " whose ", fieldNameCamel, " matches a value.", CR, //
          "// Returns the object if successful.  If not found, returns nil.", CR, //
          "// If some other database error, returns an error.", CR);
      var d = mGeneratedTypeDef;
      FieldDef our = null;
      for (var fd : d.fields()) {
        if (fd.name().equals(fieldNameSnake)) {
          our = fd;
        }
      }
      checkState(our != null, "can't find field with name:", fieldNameSnake);
      var fieldTypeStr = our.dataType().qualifiedName().className();

      s.a("func Read", ci.objNameGo, "With", fieldNameCamel, "(objValue ", fieldTypeStr, ") (", ci.objNameGo,
          ", error)", OPEN);
      generateLockAndDeferUnlock(s);
      s.a("mp := ", GLOBAL_DB, ".getTable(", ci.simTableName, ")", CR, //
          "for _, val := range mp.table.WrappedMap()", OPEN, //
          "valMap := val.AsJSMap()", CR, "fieldVal := valMap.OptAny(", quote(fieldNameSnake), ")", CR, //
          "// convert fieldVal to apppropriate type (int, string, etc)", CR //
      );
      var convExpr = "";
      if (fieldTypeStr.equals("string")) {
        convExpr = ".AsString()";
      }
      if (convExpr == null)
        badArg("don't know how to convert field of type:", fieldTypeStr);
      s.a("if fieldVal", convExpr, " == objValue", OPEN, //
          "return Default", ci.objNameGo, ".Parse(valMap).(", ci.objNameGo, "), nil", CLOSE, //
          CLOSE, //
          "return nil, NoSuchObjectErr", CLOSE);

      addChunk(s);
      return;
    }

    var d = mGeneratedTypeDef;
    var s = sourceBuilder();
    var fieldNameCamel = snakeToCamel(fieldNameSnake);

    var objNameGo = ci.objNameGo;
    var objName = ci.objName;
    var stName = "stmtReadByField" + objNameGo;

    varCode().a("var ", stName, " *sql.Stmt", CR);

    {
      initCode2().a(stName, " = CheckOkWith(db.Prepare(`SELECT id FROM ", objName, " WHERE ", fieldNameSnake,
          " = ?`))", CR);
    }

    var scanFuncName = "scanByField" + fieldNameCamel + objNameGo;
    var addScanFunc = firstTimeInSet(scanFuncName);
    if (addScanFunc) {
      generateScanByFieldFunc(d, s, scanFuncName);
    }

    FieldDef our = null;
    for (var fd : d.fields()) {
      if (fd.name().equals(fieldNameSnake)) {
        our = fd;
      }
    }
    checkState(our != null, "can't find field with name:", fieldNameSnake);
    var fieldTypeStr = our.dataType().qualifiedName().className();

    s.a("// Read ", objNameGo, " whose ", fieldNameCamel, " matches a value.", CR, //
        "// Returns the object if successful.  If not found, returns nil.", CR, //
        "// If some other database error, returns an error.", CR);

    s.a("func Read", objNameGo, "With", fieldNameCamel, "(objValue ", fieldTypeStr, ") (int, error)", OPEN);
    generateLockAndDeferUnlock(s);

    s.a("rows := ", stName, ".QueryRow(objValue)", CR, //
        "result, err := ", scanFuncName, "(rows)", CR, //
        "return result, err", CLOSE);
    addChunk(s);
  }

  private void generateScanByFieldFunc(GeneratedTypeDef d, SourceBuilder s, String funcName) {
    s.a("func ", funcName, "(rows *sql.Row) (int, error)", OPEN);
    s.a("var id int", CR);
    s.a("err := rows.Scan(&id)", CR);
    s.a("if err ==  sql.ErrNoRows", OPEN, "err = NoSuchObjectErr", CLOSE);
    s.a("return id, err", CLOSE);
  }

  private File directory() {
    if (mCachedDir == null) {
      // Determine where the go source files were written, in order to place
      // the sql source file there as well
      File sample = Context.generatedFilesSet.iterator().next();
      mCachedDir = Files.assertDirectoryExists(sample.getParentFile());
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

  private void generateLockAndDeferUnlock(SourceBuilder s) {
    s.a(GLOBAL_LOCK, ".Lock()", CR, //
        "defer ", GLOBAL_LOCK, ".Unlock()", CR);
  }

  private void updateRecord() {

    var s = sourceBuilder();
    var d = mGeneratedTypeDef;

    var objNameGo = ci.objNameGo;
    var objName = ci.objName;
    s.a("func Update", objNameGo, "(obj ", objNameGo, ") error", OPEN);

    if (simulated()) {
      generateLockAndDeferUnlock(s);
      s.a("tbl := ", GLOBAL_DB, ".getTable(", ci.simTableName, ")", CR, //
          "if !tbl.HasKey(obj.Id())", OPEN, //
          "return NoSuchObjectErr", CLOSE, //
          "tbl.Put(obj.Id(),obj.Build())", CR, //
          "tbl.modified = true", CR, "return nil", CLOSE);

    } else {
      var stName = uniqueVar("stmtUpdate");

      varCode().a("var ", stName, " *sql.Stmt", CR);

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
          "if count != 1 { err = NoSuchObjectErr }", CR, //
          "break", CLOSE);
      s.a("return err", CLOSE);
    }
    addChunk(s);
  }

  private SourceBuilder sourceBuilder() {
    return new SourceBuilder(Language.GO);
  }

  private void readRecord() {

    var d = mGeneratedTypeDef;
    var s = sourceBuilder();

    var objNameGo = ci.objNameGo;
    var objName = ci.objName;

    if (simulated()) {
      s.a("func Read", objNameGo, "(objId int) (", objNameGo, ", error)", OPEN);
      generateLockAndDeferUnlock(s);
      s.a("mp := ", GLOBAL_DB, ".getTable(", ci.simTableName, ")", CR, //
          "if !mp.HasKey(objId)", OPEN, //
          "return nil, NoSuchObjectErr", CLOSE, //
          "return mp.GetData(objId, Default", objNameGo, ").(", ci.objNameGo, "), nil", CLOSE);
    } else {

      var stName = "stmtRead" + objNameGo;
      varCode().a("var ", stName, " *sql.Stmt", CR);

      initCode2().a(stName, " = CheckOkWith(db.Prepare(`SELECT * FROM ", objName, " WHERE id = ?`))", CR);

      var scanFuncName = "scan" + objNameGo;
      var addScanFunc = firstTimeInSet(scanFuncName);
      if (addScanFunc) {
        generateScanFunc(d, s, objNameGo, objName, scanFuncName);
      }

      s.a("func Read", objNameGo, "(objId int) (", objNameGo, ", error)", OPEN);
      generateLockAndDeferUnlock(s);

      s.a("rows := ", stName, ".QueryRow(objId)", CR, //
          "result, err := ", scanFuncName, "(rows)", CR, //
          "return result, err", CLOSE);
    }
    addChunk(s);
  }

  private void generateScanFunc(GeneratedTypeDef d, SourceBuilder s, String objNameGo, String objName,
      String funcName) {
    s.a("// Return a non-nil error only if an error other than 'not found'.", CR);
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

    s.a("if err ==  sql.ErrNoRows", OPEN, "err = nil } else {", CR, //
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
    addCr(s);
  }

  private static void addCr(SourceBuilder s) {
    s.addSafe("\n");
  }

  private boolean firstTimeInSet(String object) {
    return mUniqueStringSet.add(object);
  }

  private void createTable() {
    if (simulated())
      return;

    var s = sourceBuilder();

    var d = mGeneratedTypeDef;

    var tableNameGo = d.qualifiedName().className();
    var tableName = DataUtil.convertCamelCaseToUnderscores(tableNameGo);

    var fnName = "createTable" + tableNameGo;
    mCreateTableFnNames.add(fnName);
    mCreateTableCalls.a(fnName, "()", CR);

    s.a("func ", fnName, "()", OPEN, //
        " _, err := ", GLOBAL_SQL_DB, ".Exec(`CREATE TABLE IF NOT EXISTS ", tableName, " (", CR);

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

    if (simulated()) {
      var objNameGo = ci.objNameGo;
      s.a("func Create", objNameGo, "(obj ", objNameGo, ") (", objNameGo, ", error)", OPEN);
      generateLockAndDeferUnlock(s);
      s.a("mp := ", GLOBAL_DB, ".getTable(", ci.simTableName, ")", CR, //
          "id := mp.nextUniqueKey()", CR, //
          "obj = obj.ToBuilder().SetId(id).Build()", CR, //
          "mp.Put(id, obj)", CR, //
          "mp.modified = true", CR, //
          "return obj, nil", CLOSE);
      addChunk(s);
      return;
    }

    var objNameGo = ci.objNameGo;
    var objName = ci.objName;
    var stName = "stmtCreate" + objNameGo;

    varCode().a("var ", stName, " *sql.Stmt", CR);

    s.a("func Create", objNameGo, "(obj ", objNameGo, ") (", objNameGo, ", error)", OPEN);
    generateLockAndDeferUnlock(s);

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

  private void createIndexSpecific() {
    // If there's an index on a specific (non key) field, add Create<Type>With<Field> methods

    for (var info : ci.ind) {
      // We only do this if there is a single field in the index
      if (info.mFieldNames.size() != 1)
        continue;
      var fieldName = info.mFieldNames.get(0);
      createWithField(fieldName);
    }
  }

  private static String snakeToCamel(String snake) {
    return DataUtil.convertUnderscoresToCamelCase(snake);
  }

  private void createWithField(String fieldNameSnake) {

    var fieldNameCamel = snakeToCamel(fieldNameSnake);

    var s = sourceBuilder();

    var nm = ci.objNameGo;
    s.a("// Create ", nm, " with the given (unique) ", fieldNameSnake,
        "; return nil if already exists; non-nil err if some other problem.", CR);
    s.a("func Create", nm, "With", fieldNameCamel, "(obj ", nm, ") (", nm, ", error)", OPEN);

    if (simulated()) {
      s.a("mp := ", GLOBAL_DB, ".getTable(", ci.simTableName, ")", CR, //
          "for _, val := range mp.table.WrappedMap()", OPEN, //
          "valMap := val.AsJSMap()", CR, "fieldVal := valMap.OptAny(", quote(fieldNameSnake), ")", CR, //
          "// convert fieldVal to appropriate type (int, string, etc)", CR);

      var d = mGeneratedTypeDef;
      FieldDef our = null;
      for (var fd : d.fields()) {
        if (fd.name().equals(fieldNameSnake)) {
          our = fd;
        }
      }
      checkState(our != null, "can't find field with name:", fieldNameSnake);
      var fieldTypeStr = our.dataType().qualifiedName().className();

      var convExpr = "";
      if (fieldTypeStr.equals("string")) {
        convExpr = ".AsString()";
      }
      if (convExpr == null)
        badArg("don't know how to convert field of type:", fieldTypeStr);

      s.a("if fieldVal", convExpr, " == obj.", fieldNameCamel, "()", OPEN, //
          "return nil, nil", CLOSE, //
          CLOSE);

      //    obj  = obj.ToBuilder().SetId(mp.nextUniqueKey()).Build()
      //    mp.Put(obj.Id(),obj)
      //    mp.modified = true
      //    return obj, nil
      //  }
      s.a("obj = obj.ToBuilder().SetId(mp.nextUniqueKey()).Build()", CR, //
          "mp.Put(obj.Id(), obj)", CR, //
          "mp.modified = true", CR, //
          "return obj, nil", CLOSE);

    } else {
      var objNameGo = ci.objNameGo;
      var objName = ci.objName;

      var stName = "stmtCreate" + objName + "With" + fieldNameSnake;

      varCode().a("var ", stName, " *sql.Stmt", CR);

      s.a("// Use our own lock here; the functions we call will use the usual lock.", CR, //
          "// Our own lock prevents other threads from calling this specific function.", CR, //
          "// So, if this function is the only one called to create objects, the uniqueness", CR, //
          "// property will hold.", CR);

      var ourLockVar = uniqueVar("lockCreateWith");
      varCode().a("var ", ourLockVar, " sync.Mutex", CR);

      s.a(ourLockVar, ".Lock()", CR, //
          "defer ", ourLockVar, ".Unlock()", CR, //
          //
          "var err error", CR, //
          "var created ", objNameGo, CR, //

          "existingId, err1 := Read", objNameGo, "With", fieldNameCamel, "(obj.", fieldNameCamel, "())", CR, //
          "Pr(`existing id:`,existingId)", CR, //
          "err = err1", CR, //
          "if err == NoSuchObjectErr", OPEN, //
          "c, err2 := Create", objNameGo, "(obj)", CR, //
          "err = err2", CR, "created = c", CLOSE, "return created,err", CR, CLOSE);
    }
    addChunk(s);

  }

  private void addChunk(SourceBuilder sb) {
    mCode.append(sb.getContent());
    mCode.append("\n\n");
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

  //  private String objName() {
  //    if (mCachedObjName == null) {
  //      mCachedObjName = DataUtil.convertCamelCaseToUnderscores(objNameGo());
  //    }
  //    return mCachedObjName;
  //  }
  //
  //  private String objNameGo() {
  //    if (mCachedObjNameGo == null) {
  //      mCachedObjNameGo = mGeneratedTypeDef.qualifiedName().className();
  //    }
  //    return mCachedObjNameGo;
  //  }
  //
  //  private String simTableNameGo() {
  //    if (mCachedSimTableNameGo == null) {
  //      mCachedSimTableNameGo = quote(objName());
  //    }
  //    return mCachedSimTableNameGo;
  //  }

  private Set<String> mUniqueIndexNames = hashSet();

  private void constructTables() {
    for (var x : mCreateTableFnNames) {
      initCode1().a(x, "()", CR);
    }
  }

  private void createIndexes() {
    for (var fields : ci.ind) {
      var indexName = fields.typeName + "_" + String.join("_", fields.mFieldNames);
      checkState(mUniqueIndexNames.add(indexName), "duplicate index:", indexName);
      var s = initCode1();
      s.a("CheckOkWith(db.Exec(`CREATE UNIQUE INDEX IF NOT EXISTS ", indexName, " ON ", fields.tableName,
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

  private static class IndexInfo {
    String typeName;
    String tableName;
    List<String> mFieldNames = arrayList();
  }

  private boolean simulated() {
    return mConfig.dbsim();
  }

  private static final String GLOBAL_DB = "singletonDatabase";
  private static final String GLOBAL_LOCK = GLOBAL_DB + ".Lock";
  private static final String GLOBAL_SQL_DB = GLOBAL_DB + ".SqlDatabase";

  private DatagenConfig mConfig;
  private Set<String> mUniqueStringSet = hashSet();
  private int mState;
  private String mPackageExpr;
  private boolean mActive;
  private boolean mWasActive;
  private File mCachedDir;
  private StringBuilder mCode = new StringBuilder();
  private GeneratedTypeDef mGeneratedTypeDef;
}
