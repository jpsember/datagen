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

public class PythonGeneratorTest extends GenBaseTest {

  @Override
  public void setup() {
    super.setup();
    language(Language.PYTHON);
  }

  @Test
  public void simple() {
    p().pr("fields {", INDENT, //
        "int alpha = 42;", CR, //
        "? int beta;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void externDefaultPrefix() {
    p().pr("extern abc.xyz.Foo;", CR);
    p().pr("fields {", INDENT, //
        "Foo delta;", CR, //
        "?Foo optional;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void ints() {
    p().pr("fields {", INDENT, //
        "int alpha;", CR, //
        "int beta = 42;", CR, //
        "?int gamma;", CR, //
        "*int delta;", CR, //
        "*?int epsilon;", CR, //
        "long omega;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void bools() {
    p().pr("fields {", INDENT, //
        "bool alpha;", CR, //
        "bool beta = true;", CR, //
        "?bool gamma;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void doubles() {
    p().pr("fields {", INDENT, //
        "*double foo;", CR, //
        "?double golf;", CR, // 
        "float hotel;", CR, //
        "*float india;", CR, //
        "?*float juliet;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void strings() {
    p().pr("fields {", INDENT, //
        "string alpha;", CR, //
        "string beta = \"hello\";", CR, //
        "?string gamma;", CR, //
        "*string delta;", CR, //
        "*?string epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void enumDefn() {
    p().pr("enum {", INDENT, //
        "alpha, bravo, charlie;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void enumRef() {
    language(Language.PYTHON);
    p().pr("enum Language;", CR, //
        "fields {", INDENT, //
        "bool clean;", CR, //
        "Language language;", CR, //
        OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void enumMisc() {
    language(Language.PYTHON);
    p().pr("enum Zebra;", CR, //
        "fields {", INDENT, //
        "Zebra alpha;", CR, //
        "?Zebra gamma;", CR, //
        "*Zebra delta;", CR, //
        "*?Zebra epsilon;", CR, //
        OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void typeFile() {
    p().pr("fields {", INDENT, //
        "File alpha;", CR, //
        "?File beta;", CR, //
        "*File gamma;", CR, //
        "?*File delta;", CR, //
        "File epsilon = ", quote("abc/xyz.txt"), ";", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void ipoints() {
    p().pr("fields {", INDENT, //
        "IPoint a;", CR, //
        "?IPoint b;", CR, //
        "*IPoint c;", CR, //
        "?*IPoint d;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void alternateSourcePath() {
    addArg("python_source_path", sourceDir().toString() + "/alt");
    p().pr("fields {", INDENT, //
        "Foo delta;", CR, //
        "?Foo optional;", CR, //
        OUTDENT, "}");
    generateDummyDatFile("foo");
    compile();
  }

}
