/**
 * MIT License
 *
 * Copyright (c) 2021 Jeff Sember
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/
package datatest;

import org.junit.Test;

import datagen.gen.Language;

import static js.base.Tools.*;

public class RustGeneratorTest extends GenBaseTest {

  @Override
  public void setup() {
    super.setup();
    language(Language.RUST);
  }

  @Test
  public void basic() {
    p().pr("class {", INDENT, //
        "string profile;", CR, //
        "string database;", CR, //
        "string config_file;", CR, //
        "string hive;", CR, //
        "string key = \"sample_key\";", CR, //
        "string out;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void crow() {
    p().pr("class {", INDENT, //
        "string name;", CR, //
        "int rage_level = 16;", CR, //
        "long timestamp;", CR, //
        "bool live;", CR, //
        "string zebra = \"frank\";", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void bigBar() {
    p().pr("extern gen.abc.Contract;", CR, //
        "class {", INDENT, //
        "int foo;", CR, //
        "Contract contract;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void pluto() {
    p().pr("extern gen.abc.Contract;", CR, //
        "class {", INDENT, //
        "int foo;", CR, //
        "Contract contract;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void fox() {
    p().pr("class {", INDENT, //
        "*int foo;", CR, //
        "}");
    compile();
  }

  @Test
  public void neptune() {
    p().pr( //
        "class {", INDENT, //
        "int foo;", CR, //
        "gen.abc.Contract contract;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void lists() {
    p().pr( //
        "class {", INDENT, //
        "* byte b;", CR, //
        "* short s;", CR, //
        "* int i;", CR, //
        "* long g;", CR, //
        "* string strs;", CR, //
        "* gen.abc.Contract con;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void enum1() {
    p().pr("enum {alpha, beta, gamma}");
    compile();
  }

  @Test
  public void file1() {
    p().pr("class {", INDENT, //
        "File x;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void externMulti() {
    p().pr("class alpha {", INDENT, //
        "*foo.gen.Layer layers;", CR, //
        OUTDENT, "}");
    p().pr("enum bravo {", INDENT, //
        "a,b,c,", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void subdirs() {
    withDatFilename("classes");
    p().pr("class echo {int alpha;}");
    p().pr("class gamma {int b;}");
    pushDatSubdir("foo");
    withDatFilename("classes");
    p().pr("class fox {int bravo;}");
    p().pr("class echo {int charlie;}");
    compile();
  }

  @Test
  public void jtools() {
    withDatFilename("classes");
    addArg("special_handling", "rust_jtools");
    p().pr(
        "\n" +
            "class global_vars {\n" +
            "  string loc_prefix_old;\n" +
            "  string loc_prefix_new;\n" +
            "}\n" +
            "\n" +
            "class source_location_info {\n" +
            "    File source_file;\n" +
            "    int line_number;\n" +
            "    int column_number;\n" +
            "    string function_name;\n" +
            "\n" +
            "    // The string that was parsed to produce the above fields\n" +
            "    string origin;\n" +
            "}\n" +
            "\n" +
            "// Information about a particular error type\n" +
            "//\n" +
            "class error_info {\n" +
            "    int code;\n" +
            "    string name;\n" +
            "    string description;\n" +
            "}\n");
    compile();
  }

  @Test
  public void snakeCase() {
    rv();
    withDatFilename("classes");
    p().pr(
        "class node {\n" +
            "  string id;\n" +
            "  *FPoint vertices;\n" +
            "\n" +
            "  *string original_column_contents;\n" +
            "}\n" +
            "\n" +
            "// enum experiment { alpha, beta }\n" +
            "\n" +
            "class alg_result {\n" +
            "  Node candidate;\n" +
            "\n" +
            "  float score;\n" +
            "\n" +
            "  string note;\n" +
            "}\n" +
            "\n" +
            "class node_set {\n" +
            "  *Node nodes;\n" +
            "  FPoint origin;\n" +
            "  FPoint size;\n" +
            "}\n"
    );
    compile();
  }
}
