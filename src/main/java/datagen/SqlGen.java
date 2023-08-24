package datagen;

import java.io.File;

import js.base.BaseObject;
import js.file.Files;
import static js.base.Tools.*;

public class SqlGen extends BaseObject {

  public void generate() {

    mDir = Context.config.sqlDir();
    if (Files.empty(mDir))
      return;
    if (!Context.sql.Active)
      return;
    todo("if clean, delete sql product directory (but not here)");
    Files.S.mkdirs(mDir);

    if (TableFlag) {
      var g = new SqlCreateTable();
      g.generate();
    }

  }

  public File directory() {
    return mDir;
  }

  public boolean Active;
  public boolean TableFlag;
  private File mDir;
}
