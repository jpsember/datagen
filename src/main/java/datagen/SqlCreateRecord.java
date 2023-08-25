package datagen;

import static js.base.Tools.*;

import java.util.List;

import datagen.gen.Language;
import js.base.BaseObject;
import js.data.DataUtil;
import static datagen.SourceBuilder.*;

public class SqlCreateRecord extends BaseObject {

  public static String generate(GeneratedTypeDef d) {
    var s = new SourceBuilder(Language.GO);

    var objNameGo = d.qualifiedName().className();
    var objName = DataUtil.convertCamelCaseToUnderscores(objNameGo);
    var stName = "stmtCreate" + objNameGo;
    
    s.a("var ", stName, " *sql.Stmt",CR);
   
    s.a("func Create", objNameGo, "(db *sql.DB, obj ", objNameGo, ") (", objNameGo, ", error)", OPEN);
    
    s.a("Pr(`Create:`,obj)",CR)
    ;
    
    s.a("if ",stName," == nil ",OPEN, //
        stName," = CheckOkWith(db.Prepare(`INSERT INTO ", objName, " (");

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

    s.a(    "var err error", CR, //
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
    
    s.a("Pr(`execd, result:`,createdObj,`err:`,err1)",CR)
    ;
    
    
    s.a("err = err1", CR, "if err == nil", OPEN);
    {
      s.a("id, err2 := result.LastInsertId()", CR, //
          "err = err2", CR, "if err == nil", OPEN, //
          "createdObj = obj.ToBuilder().SetId(int(id)).Build()", CLOSE);
    }
    s.a(CLOSE);
    s.a("return createdObj, err", CLOSE);

    return s.getContent() + "\n";
  }
}
