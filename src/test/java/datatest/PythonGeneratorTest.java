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

  private static final String sampleStringList = list().add("a").add("b").toString();

  @Override
  public void setup() {
    super.setup();
    language(Language.PYTHON);
  }

  @Test
  public void simple() {
    p().pr("class {", INDENT, //
        "int alpha = 42;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void externDefaultPrefix() {
    p().pr("extern abc.xyz.Foo;", CR);
    p().pr("class {", INDENT, //
        "Foo delta;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void ints() {
    p().pr("class {", INDENT, //
        "int alpha;", CR, //
        "int beta = 42;", CR, //
        "*int delta;", CR, //
        "long omega;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void bools() {
    p().pr("class {", INDENT, //
        "bool alpha;", CR, //
        "bool beta = true;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void doubles() {
    p().pr("class {", INDENT, //
        "*double foo;", CR, //
        "float hotel;", CR, //
        "*float india;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void strings() {
    cryptic();
    p().pr("class {", INDENT, //
        "string alpha;", CR, //
        "string beta = \"hello\";", CR, //
        "*string delta;", CR, //
        "*string fox = ", sampleStringList, ";", CR, //
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
        "class {", INDENT, //
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
        "class {", INDENT, //
        "Zebra alpha;", CR, //
        "*Zebra delta;", CR, //
        OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void typeFile() {
    p().pr("class {", INDENT, //
        "File alpha;", CR, //
        "*File gamma;", CR, //
        "File epsilon = ", quote("abc/xyz.txt"), ";", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void ipoints() {
    p().pr("class {", INDENT, //
        "IPoint a;", CR, //
        "*IPoint c;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void alternateSourcePath() {
    addArg("python_source_path", sourceDir().toString() + "/alt");
    p().pr("class {", INDENT, //
        "Foo delta;", CR, //
        OUTDENT, "}");
    generateDummyDatFile("foo");
    compile();
  }

  @Test
  public void jsmap() {
    p().pr("class {", INDENT, //
        "JSMap m;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void matrix() {
    p().pr("class {", INDENT, //
        "float a;", CR, //
        "float b;", CR, //
        "float c;", CR, //
        "float d;", CR, //
        "float tx;", CR, //
        "float ty;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void externType() {
    p().pr("class {", INDENT, //
        "*foo.gen.Layer layers;", CR, //
        OUTDENT, "}");
    compile();
  }
}
