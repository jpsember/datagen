package datagen;

import js.base.Pair;
import js.file.Files;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static datagen.Utils.DEBUG_RUST_IMPORTS;
import static datagen.Utils.pmod;
import static js.base.Tools.*;

public final class RustUtil {

  public static void generateModFiles(List<Pair<File, GeneratedTypeDef>> entries) {
    pmod("generateModFiles");

    //Map<File, Set<String>> mModFilesMap = hashMap();

    List<String> listForCurrentDir = arrayList();
    File currentDir = Files.DEFAULT;

    for (var ent : entries) {
      var file = ent.first;
      var genType = ent.second;

      var dir = Files.parent(file);
      if (!dir.equals(currentDir)) {
        flushDir(currentDir, listForCurrentDir);
        currentDir = dir;
        listForCurrentDir.clear();
      }

      var name = Files.basename(file);
      listForCurrentDir.add(
          "pub mod " + name + ";"
      );

    }
    flushDir(currentDir, listForCurrentDir);
  }

  private static void flushDir(File directory, List<String> entries) {
    if (entries.isEmpty()) return;
    var sb = new StringBuilder();
    entries.sort(null);
    for (var x : entries) {
      sb.append(x);
      sb.append('\n');
    }
    var content = sb.toString();
    var modFile = new File(directory, "mod.rs");
    pmod("updating", modFile, ":", INDENT, content, VERT_SP);
    Files.S.writeString(modFile, content);
  }


////      listForCurrentDir.add()
////      var set = mModFilesMap.get(file);
////      if (set == null) {
////        set = hashSet();
////        mModFilesMap.put(file,set);
//      }
//      var set = ent.getValue();
//      Set<String> sorted = treeSet();
//      pmod("file:", file, "set:", sorted);
//      sorted.addAll(set);
//
//      var modFile = new File(file, "mod.rs");
//      if (datagenConfig().clean())
//        Files.S.deletePeacefully(modFile);
//
//      // Add existing mod file entries, so we don't omit any ones
//      // corresponding to files that aren't being freshened
//      // (as they won't have entries in our mModFilesMap)
//      {
//        var c = Files.readString(modFile, "").strip();
//        if (!c.isEmpty()) {
//          var lines = split(c, '\n');
//          for (var x : lines) {
//            x = removePubModFromClass(x);
//            sorted.add(x);
//          }
//        }
//      }
//
//      var sb = new StringBuilder();
//      for (var x : sorted)
//        sb.append(addPubModToClass(x)).append('\n');
//      var content = sb.toString();
//      if (DEBUG_RUST_IMPORTS && verbose())
//        log(VERT_SP, "Updating", modFile, ":", INDENT, content, VERT_SP);
//      pmod("updating", modFile, ":", INDENT, content, VERT_SP);
//      Files.S.writeString(modFile, content);
//    }
}
