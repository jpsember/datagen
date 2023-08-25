package datagen;

import static js.base.Tools.*;

import datagen.gen.Language;
import js.base.BaseObject;
import js.data.DataUtil;

import static datagen.SourceBuilder.*;

public class SqlCreateTable extends BaseObject {

  public static String generate(GeneratedTypeDef d) {

    var s = new SourceBuilder(Language.GO);

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
    return s.getContent() + "\n";
  }

}
