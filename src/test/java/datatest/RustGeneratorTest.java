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
 * 
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
}
