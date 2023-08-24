package datagen;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.data.DataUtil;
import js.file.Files;

public class SqlCreateTable extends BaseObject {

  public void generate() {

    GeneratedTypeDef d = Context.generatedTypeDef;

    ap("CREATE TABLE IF NOT EXISTS ");

    var tableName = DataUtil.convertCamelCaseToUnderscores(d.qualifiedName().className());

    ap(tableName);
    ap(" (\n");

    var i = INIT_INDEX;
    for (FieldDef f : d.fields()) {
      i++;
      if (i != 0) {
        ap(",");
        cr();
      }
      ap(spaces(4));
      var name = f.name();
      String sqlType = f.dataType().sqlType();
      boolean isId = name.equals("id");
      if (isId) {
        if (!sqlType.equals("INTEGER"))
          badState("id doesn't look like an integer: ", f.name(), f.dataType().qualifiedName().className(),
              sqlType);
        checkState(i == 0, "'id' should be first field");
      }
      ap(name);
      sp();
      checkArgument(!sqlType.startsWith("!!!"), "no sql type for", f.name(), ";", f.dataType().getClass());
      ap(sqlType);
      if (isId) {
        ap(" PRIMARY KEY");
      }

    }
    cr();
    ap(");");
    cr();
    
    var f = Files.join(Context.sql.directory(), tableName + "_gen.txt");
    Files.S.writeString(f, mSb.toString());
  }

  private void ap(Object value) {
    mSb.append(value);
  }

  private void cr() {
    addLF(mSb);
  }

  private void sp() {
    char last = ' ';
    StringBuilder sb = mSb;
    if (sb.length() > 0)
      last = sb.charAt(sb.length() - 1);
    if (last > ' ')
      sb.append(' ');
  }

  private StringBuilder mSb = new StringBuilder();
}
