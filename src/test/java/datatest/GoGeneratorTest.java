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
import js.data.DataUtil;
import js.file.Files;

import static js.base.Tools.*;

import java.io.File;

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
  public void externalRepeated() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "class {", INDENT, //
        "*Beaver b;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void enums() {
    p().pr("enum {", INDENT, //
        "alpha, bravo, charlie;", OUTDENT, //
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
        "map string gen.webservgen.Session sessionMap;", CR, OUTDENT, "}");
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
}
