package datagen;

import datagen.gen.DatagenConfig;
import js.base.BaseObject;
import js.file.Files;
import js.parsing.RegExp;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static datagen.Utils.*;
import static js.base.Tools.*;

public class RustModuleMgr extends BaseObject {

  // ------------------------------------------------------------------
  // Generating Rust module files
  // ------------------------------------------------------------------

  private DatagenConfig datagenConfig() {
    return Context.config;
  }

  public void prepareRustModules() {
    pmod("prepareRustModules");
    mModFilesMap = hashMap();
  }

  public void updateRustModules(File sourceRelPath) {
    pmod("updateRustModules, sourceRelPath:", sourceRelPath);
//    var db = DEBUG_RUST;
//    if (db)
//      log("updateRustModules, source:",  sourceRelPath );
    var srcPath = new File(datagenConfig().sourcePath(), sourceRelPath.toString());
    while (true) {

      pmod("...", srcPath);
      var parent = Files.parent(srcPath);
      if (parent.equals(datagenConfig().sourcePath()))
        break;
      var st = mModFilesMap.get(parent);
      if (st == null) {
        st = hashSet();
        mModFilesMap.put(parent, st);
      }
      var name = Files.removeExtension(srcPath.getName());
      st.add(name);
      srcPath = parent;
    }
  }

  public void flushRustModules() {
    pmod("flushRustModules");
    for (var ent : mModFilesMap.entrySet()) {
      var file = ent.getKey();
      var set = ent.getValue();
      Set<String> sorted = treeSet();
      pmod("file:", file, "set:", sorted);
      sorted.addAll(set);

      var modFile = new File(file, "mod.rs");
      if (datagenConfig().clean())
        Files.S.deletePeacefully(modFile);

      // Add existing mod file entries, so we don't omit any ones
      // corresponding to files that aren't being freshened
      // (as they won't have entries in our mModFilesMap)
      {
        var c = Files.readString(modFile, "").strip();
        if (!c.isEmpty()) {
          var lines = split(c, '\n');
          for (var x : lines) {
            x = removePubModFromClass(x);
            sorted.add(x);
          }
        }
      }

      var sb = new StringBuilder();
      for (var x : sorted)
        sb.append(addPubModToClass(x)).append('\n');
      var content = sb.toString();
      if (DEBUG_RUST_IMPORTS && verbose())
        log(VERT_SP, "Updating", modFile, ":", INDENT, content, VERT_SP);
      pmod("updating", modFile, ":", INDENT, content, VERT_SP);
      Files.S.writeString(modFile, content);
    }
  }

  private static String addPubModToClass(String className) {
    return "pub mod " + className + ";";
  }

  private static String removePubModFromClass(String pubModEntry) {
    var m = RegExp.matcher("pub mod ([a-zA-Z_][a-zA-Z_0-9]*);", pubModEntry.strip());
    checkArgument(m.matches(), "unexpected mod.rs entry:", pubModEntry);
    var stripped = m.group(1);
    return stripped;
  }

  private Map<File, Set<String>> mModFilesMap;

}
