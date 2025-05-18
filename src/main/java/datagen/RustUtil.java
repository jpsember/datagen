package datagen;

import js.file.Files;

import java.io.File;
import java.util.List;

import static datagen.Utils.*;
import static js.base.Tools.*;

public final class RustUtil {

  public static void generateModFiles(List<GeneratedTypeDef> entries) {
    pmod("generateModFiles");

    List<GeneratedTypeDef> listForCurrentDir = arrayList();
    File currentDir = Files.DEFAULT;

    for (var genType : entries) {
      var file = genType.generatedSourceFile();
      var dir = Files.parent(file);
      if (!dir.equals(currentDir)) {
        flushDir(currentDir, listForCurrentDir);
        currentDir = dir;
        listForCurrentDir.clear();
      }
      listForCurrentDir.add(genType);
    }
    flushDir(currentDir, listForCurrentDir);
  }

  private static void flushDir(File directory, List<GeneratedTypeDef> entries) {
    if (entries.isEmpty()) return;

    List<String> lines = arrayList();
    for (var x : entries)
      lines.add("pub mod " + Files.basename(x.generatedSourceFile()) + ";");
    lines.sort(null);

    var content = String.join("\n", lines) + "\n";
    var modFile = new File(directory, "mod.rs");
    pmod("updating", modFile, ":", INDENT, content, VERT_SP);
    Files.S.writeString(modFile, content);
  }

}
