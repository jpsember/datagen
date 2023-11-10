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

public class GoGeneratorTest extends GenBaseTest {

  @Override
  public void setup() {
    super.setup();
    language(Language.GO);
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
  public void cat() {
    p().pr("class {", INDENT, //
        "string name = \"kirby\";", CR, //
        "*string children = [\"winken\",\"blinken\",\"nod\"];", CR, //
        "*int lucky_numbers = [9, 14 ];", CR, OUTDENT, "}");
    compile();
  }

  @Test
  public void contract() {
    p().pr("extern gen.abc.Crow;", CR, //
        "class {", INDENT, //
        "Crow boss;", CR, //
        "*Crow henchmen;", CR //
        , OUTDENT, "}");
    compile();
  }

  @Test
  public void filesMisc() {
    p().pr("class {", INDENT, //
        "File a;", CR, //
        "*File bs;", CR, //
        "File c = \"hello.txt\";", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void externalRepeated() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "class {", INDENT, //
        "*Beaver beaver_list;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void enums() {
    p().pr("enum {", INDENT, //
        "alpha, bravo, charlie, ddelta_epsilon;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void enumField() {
    p().pr("class {", INDENT, //
        "enum Level grad;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void session() {
    setDatSubdir("gen/webservgen");
    p().pr("class {", INDENT, //
        "string id;", CR, OUTDENT, "}");
    compile();
  }

  @Test
  public void persistSessionMap() {
    setDatSubdir("gen/webservgen");
    p().pr("class {", INDENT, //
        "map string gen.webservgen.Session session_map;", CR, OUTDENT, "}");
    compile();
  }

  @Test
  public void depr1() {
    setDatSubdir("gen/webservgen");
    p().pr("class {", INDENT, //
        "- string id;", CR, //
        " int count;", OUTDENT, "}");
    compile();
  }

  @Test
  public void depr2() {
    setDatSubdir("gen/webservgen");
    p().pr("- class {", INDENT, //
        " string id;", CR, //
        " int count;", OUTDENT, "}");
    compile();
  }

  @Test
  public void ints() {
    p().pr("class {", INDENT, //
        "int x;", CR, //
        "int y = 42;", CR, //
        "*int sizes;", CR, //
        "short z;", CR, //
        "short z1 = 19;", CR, //
        "long u;", CR, //
        "long u1 = 19000000000;", CR, //
        "byte b;", CR, //
        "byte b1 = 42;", CR, //
        "*byte b2;", CR, //
        "}");
    compile();
  }

  @Test
  public void floats() {
    p().pr("class {", INDENT, //
        "float x;", CR, //
        "float y = 42.12;", CR, //
        "*float sizes;", CR, //
        "}");
    compile();
  }
  
  @Test
  public void maps() {
    p().pr("class {", INDENT, //
         "map string string s;", CR, //
      "map string string q = \"{\\\"bar\\\":\\\"foo\\\"}\";", CR, //
        "map string long h;",CR,//
        "}");
    alert("map string long fails; the long must be extracted from the JSEntity");
    compile();
  }

  @Test
  public void alpha() {
    p().pr("class{", INDENT, //
        "int id;", CR, //
        "string name;", CR, //
        "enum Level grad;", CR, //
        "bool alive;", CR, //
        "} sql{ }");
    compile();
  }

  /**
   * Generating SQL code for a data type that includes an index
   */
  @Test
  public void bravo() {
    //rv();
    addArg("comments");
    p().pr("class {", INDENT, //
        "int id;", CR, //
        "string name;", CR, //
        "enum Level grad;", CR, //
        "bool alive;", CR, //
        "} sql{index name}");
    compile();
  }

 
  @Test
  public void bravo2() {
    //rv();
    addArg("comments");
    addArg("dbsim");
    p().pr("class {", INDENT, //
        "int id;", CR, //
        "string name;", CR, //
        "enum Level grad;", CR, //
        "bool alive;", CR, //
        "} sql{index name}");
    compile();
  }
  /**
   * Generating sql code, with simulated database
   */
  @Test
  public void charlie() {
    addArg("dbsim");
    addArg("comments");
    p().pr("class {", INDENT, //
        "int id;", CR, //
        "string name;", CR, //
        "enum Level grad;", CR, //
        "bool alive;", CR, //
        "} sql{index name}");
    compile();
  }

}
