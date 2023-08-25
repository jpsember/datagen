package datagen;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.data.DataUtil;

public class SqlCreateTable extends BaseObject {

  public String generate(GeneratedTypeDef d)  {

    mSb = new StringBuilder();
    
    
    
    
    
    var tableNameGo =  d.qualifiedName().className() ;

    
    
ap("func CreateTable"+tableNameGo+"(db *sql.DB) {")    .cr();
ap("  _, err := db.Exec(`");
    
    
    
    
    
    //GeneratedTypeDef d = Context.generatedTypeDef;

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

    ap("  );");
    ap("`)").cr();
    ap("  CheckOk(err, \"failed to create table\")").cr();
    ap("}").cr();
   
    return mSb.toString();
  }

  private SqlCreateTable ap(Object value)  {
    mSb.append(value);
    return this;
  }

  private SqlCreateTable cr()  {
    addLF(mSb);
    return this;
  }

  private SqlCreateTable sp() {
    char last = ' ';
    StringBuilder sb = mSb;
    if (sb.length() > 0)
      last = sb.charAt(sb.length() - 1);
    if (last > ' ')
      sb.append(' ');
    return this;}

  private StringBuilder mSb; // = new StringBuilder();
}
