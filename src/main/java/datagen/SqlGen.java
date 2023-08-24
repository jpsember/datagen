package datagen;

import java.io.File;

import js.base.BaseObject;
import js.file.Files;

public class SqlGen extends BaseObject {

  public void generate() {

    mDir = Context.config.sqlDir();
    if (Files.empty(mDir))
      return;
    Files.S.mkdirs(mDir);

    if (TableFlag) {
      var g = new SqlCreateTable();
      g.generate();
    }

  }

  public File directory() {
    return mDir;
  }

  public boolean TableFlag;
  private File mDir;
}
