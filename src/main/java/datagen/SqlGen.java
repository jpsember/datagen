package datagen;

import java.io.File;
import java.util.List;
import java.util.Map;
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
    info.tableName = camelToSnake(mGeneratedTypeDef.qualifiedName().className());
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
    iterators();

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

    m.put("support_common", Files.readString(SourceGen.class, "db_support_go_common.txt"));
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
    String simTableNameStr;
    List<IndexInfo> ind = arrayList();
    boolean idVerified;

    public ClassInfo() {
      var t = Context.generatedTypeDef;
      objNameGo = t.qualifiedName().className();
      objName = camelToSnake(objNameGo);
      simTableNameStr = goQuote(objName);
    }

  }

  private static String goQuote(Object obj) {
    return "`" + obj + "`";
  }

  private ClassInfo ci;

  private void indexFunc() {
    for (var info : ci.ind) {
      checkState(info.mFieldNames.size() == 1, "unexpected number of fields");
      var fieldName = info.mFieldNames.get(0);
      readByField(fieldName);
    }
  }

  private FieldDef findFieldWithName(String fieldNameSnake) {
    var d = mGeneratedTypeDef;
    for (var fd : d.fields()) {
      if (fd.name().equals(fieldNameSnake)) {
        return fd;
      }
    }
    throw badArg("can't find field with name:", fieldNameSnake);
  }

  private void readByField(String fieldNameSnake) {

    var s = sourceBuilder();
    FieldDef our = findFieldWithName(fieldNameSnake);

    var fieldNameCamel = snakeToCamel(fieldNameSnake);
    var fieldTypeStr = our.dataType().qualifiedName().className();

    s.a("// Read ", ci.objNameGo, " whose ", fieldNameCamel, " matches a value.", CR, //
        "// Returns the object if successful.  If not found, returns the default object.", CR, //
        "// If some other database error, returns an error.", CR);
    s.a("func Read", ci.objNameGo, "With", fieldNameCamel, "(objValue ", fieldTypeStr, ") (", ci.objNameGo,
        ", error)", OPEN);

    if (simulated()) {
      lockAndDeferUnlock(s);
      s.a("mp := ", GLOBAL_DB, ".getTable(", ci.simTableNameStr, ")", CR, //
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
          "return Default", ci.objNameGo, ", nil", CLOSE);

      addChunk(s);
      return;
    }

    newLockAndDeferUnlock(s);

    var objNameGo = ci.objNameGo;
    var objName = ci.objName;
    var stName = "stmtReadByField" + objNameGo;
    declareStatement(stName);

    genPrepareStatement(stName, "`SELECT id FROM ", objName, " WHERE ", fieldNameSnake, " = ?`");

    var scanFuncName = "scanIdFromRows";

    s.a("var err error", CR, //
        "object := Default", objNameGo, CR, //
        "rows := ", stName, ".QueryRow(objValue)", CR, //
        "objId, err1 := ", scanFuncName, "(rows)", CR, //
        "err = err1", CR, //
        "if objId != 0", OPEN, //
        "object, err =  Read", objNameGo, "(objId)", CLOSE, //
        "return object, err", CLOSE);

    addChunk(s);
  }

  private void genPrepareStatement(String stName, Object... args) {
    initCode2().a(stName, " = CheckOkWith(", LOCAL_SQLDB, ".Prepare(");
    for (var arg : args)
      initCode2().a(arg);
    initCode2().a("))", CR);
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

  /**
   * Generate the code to lock the shared lock, and defer unlocking it
   */
  private void lockAndDeferUnlock(SourceBuilder s) {
    auxLockAndDeferUnlock(s, GLOBAL_LOCK);
  }

  /**
   * Create a new lock, and generate the code to lock it and defer unlocking it
   */
  private void newLockAndDeferUnlock(SourceBuilder s) {
    var ourLockVar = uniqueVar("privateLock");
    varCode().a("var ", ourLockVar, " sync.Mutex", CR);
    auxLockAndDeferUnlock(s, ourLockVar);
  }

  private void auxLockAndDeferUnlock(SourceBuilder s, String lockName) {
    s.a(lockName, ".Lock()", CR, //
        "defer ", lockName, ".Unlock()", CR);
  }

  private void updateRecord() {

    var s = sourceBuilder();
    var d = mGeneratedTypeDef;

    var objNameGo = ci.objNameGo;
    var objName = ci.objName;

    s.a("// Update ", objNameGo, " based on its id.", CR, //
        "// Returns a non-nil error if a problem occurs.", CR);

    s.a("func Update", objNameGo, "(obj ", objNameGo, ") error", OPEN);

    if (simulated()) {
      lockAndDeferUnlock(s);
      s.a("tbl := ", GLOBAL_DB, ".getTable(", ci.simTableNameStr, ")", CR, //
          "if !tbl.HasKey(obj.Id())", OPEN, //
          "return NoSuchObjectErr", CLOSE, //
          "tbl.Put(obj.Id(),obj.Build())", CR, //
          "tbl.modified = true", CR, "return nil", CLOSE);

    } else {
      var stName = uniqueVar("stmtUpdate");
      declareStatement(stName);

      lockAndDeferUnlock(s);

      List<FieldDef> filtFields = arrayList();

      {
        var t = initCode2();
        t.a(stName, " = CheckOkWith(", LOCAL_SQLDB, ".Prepare(`UPDATE ", objName, " SET ");

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

    var s = sourceBuilder();

    var objNameGo = ci.objNameGo;

    s.a("// Read ", objNameGo, " with a particular id.  If not found, returns default object.", CR, //
        "// Returns a non-nil error if some other problem occurs.", CR);

    if (simulated()) {
      s.a("func Read", objNameGo, "(objId int) (", objNameGo, ", error)", OPEN);
      lockAndDeferUnlock(s);
      s.a("mp := ", GLOBAL_DB, ".getTable(", ci.simTableNameStr, ")", CR, //
          "if !mp.HasKey(objId)", OPEN, //
          "return nil, nil", CLOSE, //
          "return mp.GetData(objId, Default", objNameGo, ").(", objNameGo, "), nil", CLOSE);
    } else {

      var stName = "stmtRead" + objNameGo;
      declareStatement(stName);
      // This LIMIT 1 is probably not necessary?
      genPrepareStatement(stName, "`SELECT * FROM ", ci.objName, " WHERE id = ? LIMIT 1`");

      var scanFuncName = "scan" + objNameGo;
      if (firstTimeInSet(scanFuncName))
        generateScanFunc(scanFuncName);

      s.a("func Read", objNameGo, "(objId int) (", objNameGo, ", error)", OPEN);
      lockAndDeferUnlock(s);
      s.a("// Now using the 'multiple rows' statement with a limit of 1, so we can reuse the scan code", CR);

      s.a("rows, err := ", stName, ".Query(objId)", CR, //
          "defer rows.Close()", CR, //
          "result := Default", objNameGo, CR, //
          "if err == nil", OPEN, //
          "var obj any", CR, //
          "obj, err = ", scanFuncName, "(rows)", CR, //
          "if err == nil", OPEN, //
          "result = obj.(", objNameGo, ")", CLOSE, //
          CLOSE, //
          "return result, err", CLOSE);

    }
    addChunk(s);
  }

  private void iterators() {

    // If the first field is 'id', create an iterator from it (as this is the 'implicit' index).
    verifyFirstFieldIsId();
    createIteratorWithField("id");

    for (var info : ci.ind) {
      // For now, we only do this if there is a single field in the index
      if (info.mFieldNames.size() != 1)
        continue;
      var fieldName = info.mFieldNames.get(0);
      createIteratorWithField(fieldName);
    }
  }

  private String indexTypeAsGo(String fieldName) {
    var g = mGeneratedTypeDef;
    for (var f : g.fields()) {
      var nm = f.name();
      if (nm.equals(fieldName))
        return f.dataType().qualifiedName().className();
    }
    throw badArg("Can't find type name for:", fieldName);
  }

  private void createIteratorWithField(String fieldNameSnake) {
    var s = sourceBuilder();
    var argName = fieldNameSnake + "Min";
    var fieldNameCamel = snakeToCamel(fieldNameSnake);

    String iteratorName;
    if (fieldNameSnake.equals("id")) {
      iteratorName = ci.objNameGo + "Iterator";
    } else {
      iteratorName = ci.objNameGo + fieldNameCamel + "Iterator";
    }

    s.a("// Get an iterator over ", ci.objNameGo, " objects, indexed by their ", fieldNameSnake, " fields.",
        CR, //
        "func ", iteratorName, "(", argName, " ", indexTypeAsGo(fieldNameSnake), ") DbIter", OPEN);

    // We need a function that extracts the field from an instance of this type of object
    var extractFieldFunc = uniqueVar("read" + snakeToCamel(fieldNameSnake) + "From" + ci.objNameGo);
    generateExtractFieldFunc(extractFieldFunc, snakeToCamel(fieldNameSnake));

    if (simulated()) {

      var fd = findFieldWithName(fieldNameSnake);
      var compareFuncName = determineCompareFuncForField(fd);

      s.a("x := newDbIter()", CR, //
          "x.tableName = ", ci.simTableNameStr, CR, //
          "x.fieldName = `", fieldNameSnake, "`", CR, //
          "x.fieldValMin = ", argName, CR, //
          "x.defaultObject = Default", ci.objNameGo, CR, //
          "x.readScanFieldValueFunc = ", extractFieldFunc, CR, //
          "x.compareFieldValueFunc = ", compareFuncName, CR, //
          "return x", CLOSE);

    } else {

      // We need an SQL SELECT statement, e.g., "SELECT * FROM user WHERE id >= ? ..."
      var selectStatementName = uniqueVar("stmtRead" + ci.objNameGo + "Chunk");
      declareStatement(selectStatementName);
      genPrepareStatement(selectStatementName, "`SELECT * FROM ", ci.objNameGo, " WHERE ", fieldNameSnake,
          " > ? ORDER BY ", fieldNameSnake, " LIMIT 20`");

      // We need a function to scan this type of object from a 'SELECT *' result
      var scanFuncName = "scan" + ci.objNameGo;
      var addScanFunc = firstTimeInSet(scanFuncName);

      s.a("x := newDbIter()", CR, //
          "x.fieldValMin = ", argName, CR, //
          "x.readChunkStmt = ", selectStatementName, CR, //
          "x.scanFunc = scan", ci.objNameGo, CR, //
          "x.readScanFieldValueFunc = ", extractFieldFunc, CR, //
          "x.defaultObject = Default", ci.objNameGo, CR, //
          "return x", CLOSE);

      if (addScanFunc)
        generateScanFunc(scanFuncName);
    }
    addChunk(s);
  }

  private static Map<String, String> sCompareFuncMap;
  static {
    sCompareFuncMap = hashMap();
    reg("int", "func compareInts(x, y any) int { return x.(int) - y.(int) }");
    reg("string", "func compareStrings(x, y any) int { return strings.Compare(x.(string), y.(string)) }");
  }

  private static void reg(String key, String val) {
    sCompareFuncMap.put(key, val);
  }

  private String determineCompareFuncForField(FieldDef fd) {
    var dt = fd.dataType();
    var name = dt.qualifiedName().className();

    String funcText = sCompareFuncMap.get(name);
    if (funcText == null) {
      badArg("no compare func registered for type name:", quote(name));
    }
    
    var s = chompPrefix(funcText, "func ");
    var i = s.indexOf('(');
    var funcName = s.substring(0,i);
    
    if (mCompareFuncsGenerated.add(funcName)) {
      addChunk(sourceBuilder().a(funcText));
    }
    return funcName;
  }
  private Set<String> mCompareFuncsGenerated = hashSet();

  
  private void generateExtractFieldFunc(String fnName, String fieldName) {
    var s = sourceBuilder();
    var varName = "as" + ci.objNameGo;
    s.a("func ", fnName, "(obj DataClass) any", OPEN, //
        varName, " := obj.(", ci.objNameGo, ")", CR, //
        "return ", varName, ".", fieldName, "()", CLOSE);
    addChunk(s);
  }

  private void generateScanFunc(String funcName) {
    var s = sourceBuilder();

    var oname = ci.objNameGo;
    s.a("// Scan next row as ", oname, ".  If no more rows exist, returns default object.", CR, //
        "// Returns a non-nil error if some other problem occurs.", CR);

    s.a("func ", funcName, "(rows *sql.Rows) (any, error)", OPEN);
    s.a("obj := Default", oname, CR, //
        "if !rows.Next()", OPEN, //
        "return obj, rows.Err()", CLOSE);

    List<String> fieldNames = arrayList();
    List<FieldDef> filtFields = mGeneratedTypeDef.fields();
    for (FieldDef fieldDef : filtFields) {

      var fn = camelToSnake(fieldDef.name());
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

    s.a("if err == nil", OPEN, //
        "b := New", oname, "()", CR);
    var i = INIT_INDEX;
    for (var v : filtFields) {
      i++;
      s.a("b.", v.setterName(), "(", fieldNames.get(i), ")", CR);
    }
    s.a("obj = b.Build()");
    s.a(CLOSE);

    s.a("return obj, err", CLOSE);
    addChunk(s);
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
    var tableName = camelToSnake(tableNameGo);

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
      verifyFirstFieldIsId();
      s.a(name, " ");
      checkArgument(!sqlType.startsWith("!!!"), "no sql type for", f.name(), ";", f.dataType().getClass());
      s.a(sqlType);
      if (i == 0) {
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

  private void verifyFirstFieldIsId() {
    if (ci.idVerified)
      return;

    var i = INIT_INDEX;
    for (FieldDef f : mGeneratedTypeDef.fields()) {
      i++;
      var name = f.name();
      String sqlType = f.dataType().sqlType();
      boolean isId = name.equals("id");
      if (i == 0 != isId) {
        badArg("The first field in type", mGeneratedTypeDef.name(), "should be 'id'");
      }
      if (isId) {
        if (!sqlType.equals("INTEGER"))
          badState("id doesn't look like an integer: ", f.name(), f.dataType().qualifiedName().className(),
              sqlType);
      }
    }
    ci.idVerified = true;
  }

  private void createRecord() {
    var d = mGeneratedTypeDef;
    var s = sourceBuilder();

    s.a("// Create new ", ci.objNameGo, ", and return it.", CR, //
        "// Returns a non-nil error if some other problem occurs.", CR);

    if (simulated()) {
      var objNameGo = ci.objNameGo;
      s.a("func Create", objNameGo, "(obj ", objNameGo, ") (", objNameGo, ", error)", OPEN);
      lockAndDeferUnlock(s);
      s.a("mp := ", GLOBAL_DB, ".getTable(", ci.simTableNameStr, ")", CR, //
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
    declareStatement(stName);

    s.a("func Create", objNameGo, "(obj ", objNameGo, ") (", objNameGo, ", error)", OPEN);
    lockAndDeferUnlock(s);

    List<FieldDef> filtFields = arrayList();

    {
      var t = initCode2();
      t.a(stName, " = CheckOkWith(", LOCAL_SQLDB, ".Prepare(`INSERT INTO ", objName, " (");

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
        "createdObj := Default", objNameGo, CR, //
        "result, err1 := ", stName, ".Exec(");

    s.startComma();
    for (var f : filtFields) {
      s.a(COMMA, "obj.");
      s.a(f.getterName(), "()");
      // todo("we may need to convert getter output to something else, e.g. string or int");
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
    // For each explicit index (i.e., not on "id"), Create<Type>With<Field> methods.
    // (The implicit "id" index is used by the Create<Type> method.)
    //
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

  private static String camelToSnake(String camel) {
    return DataUtil.convertCamelCaseToUnderscores(camel);
  }

  private void createWithField(String fieldNameSnake) {

    var fieldNameCamel = snakeToCamel(fieldNameSnake);

    var s = sourceBuilder();

    var nm = ci.objNameGo;
    s.a("// Create ", nm, " with the given (unique) ", fieldNameSnake, ".", CR, //
        "// Returns default object if such an object already exists.", CR, //
        "// Returns a non-nil error if some other problem occurs.", CR);

    s.a("func Create", nm, "With", fieldNameCamel, "(obj ", nm, ") (", nm, ", error)", OPEN);

    if (simulated()) {
      s.a("mp := ", GLOBAL_DB, ".getTable(", ci.simTableNameStr, ")", CR, //
          "for _, val := range mp.table.WrappedMap()", OPEN, //
          "valMap := val.AsJSMap()", CR, "fieldVal := valMap.OptAny(", quote(fieldNameSnake), ")", CR, //
          "// convert fieldVal to appropriate type (int, string, etc)", CR);

      FieldDef our = findFieldWithName(fieldNameSnake);
      var fieldTypeStr = our.dataType().qualifiedName().className();

      var convExpr = "";
      if (fieldTypeStr.equals("string")) {
        convExpr = ".AsString()";
      }
      if (convExpr == null)
        badArg("don't know how to convert field of type:", fieldTypeStr);

      s.a("if fieldVal", convExpr, " == obj.", fieldNameCamel, "()", OPEN, //
          "return Default", ci.objNameGo, ", nil", CLOSE, //
          CLOSE);

      s.a("obj = obj.ToBuilder().SetId(mp.nextUniqueKey()).Build()", CR, //
          "mp.Put(obj.Id(), obj)", CR, //
          "mp.modified = true", CR, //
          "return obj, nil", CLOSE);

    } else {
      var objNameGo = ci.objNameGo;

      s.a("// Use our own lock here; the functions we call will use the usual lock.", CR, //
          "// Our own lock prevents other threads from calling this specific function.", CR, //
          "// So, if this function is the only one called to create these objects, the uniqueness", CR, //
          "// property will hold.", CR);

      newLockAndDeferUnlock(s);

      s.a("var err error", CR, //
          "created := Default", objNameGo, CR, //

          "existingObj, err1 := Read", objNameGo, "With", fieldNameCamel, "(obj.", fieldNameCamel, "())", CR, //
          "err = err1", CR, //
          "if err == nil && existingObj.Id() == 0", OPEN, //
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

  private void declareStatement(String varName) {
    varCode().a("var ", varName, " *sql.Stmt", CR);
  }

  private int mUniqueVarCounter;

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
      s.a("CheckOkWith(", LOCAL_SQLDB, ".Exec(`CREATE UNIQUE INDEX IF NOT EXISTS ", indexName, " ON ",
          fields.tableName, " (");
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
  private static final String LOCAL_SQLDB = "sqldb";

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
