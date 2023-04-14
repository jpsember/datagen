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

import static js.base.Tools.*;

public class Java2Test extends GenBaseTest {

  @Test
  public void big() {
    p().pr("extern abc.xyz.Beaver;");

    p().pr("fields {", INDENT, //
        "int alpha;", CR, //
        "int beta = " + Integer.MAX_VALUE + ";", CR, //
        "?int gamma;", CR, //
        "?*int epsilon;", CR, //

        "byte b;", CR, //
        "byte c = " + Byte.MAX_VALUE + ";", CR, //
        "?byte d;", CR, //
        "?*byte e;", CR, //

        "string sa;", CR, //
        "string sb = \"hello\";", CR, //
        "?string sc;", CR, //
        "*string sd;", CR, //
        "?*string se;", CR, //
        "*string sf = [\"abc\",\"123\"];", CR, //

        "bool ba;", CR, //
        "bool bb = true;", CR, //
        "?bool bc;", CR, //

        "Beaver bv;", CR, //
        "?Beaver bv2;", CR, //
        "*Beaver mult;", CR, //

        "File falpha;", CR, //
        "?File fbeta;", CR, //
        "*File fgamma;", CR, //
        "?*File fdelta;", CR, //
        "File fepsilon = ", quote("abc/xyz.txt"), ";", CR, //

        "IPoint location;", CR, //
        "IRect locx;", CR, //
        "FPoint floc;", CR, //
        "FRect frect;", CR, //
        "Matrix mat;", CR, //

        "JSMap jalpha;", CR, //
        "?JSMap jbeta;", CR, //
        "*JSMap jgamma;", CR, //
        "JSMap jepsilon = ", quote("{\\\"a\\\":15,\\\"b\\\":\\\"hello\\\"}"), ";", CR, //

        "JSList lalpha;", CR, //
        "?JSList lbeta;", CR, //
        "*JSList lgamma;", CR, //
        "JSList lepsilon = ", quote("[\\\"a\\\",15,\\\"b\\\",\\\"hello\\\"]"), ";", CR, //

        "foo.gen.Garp garp;", CR, //
        "enum foo.gen.GarpEnum gnum;", CR, //

        "IPoint za;", CR, //
        "?IPoint zb;", CR, //
        "*IPoint zc;", CR, //
        "?*IPoint zd;", CR, //
        "IPoint ze = [32,64];", CR, //

        "map string File mpalpha;", CR, //
        "?map string File mpbeta;", CR, //
        "map string Beaver mpgamma;", CR, //
        "map string string mpdelta;", CR, //

        "map string long name_map;", CR, //
        "set long ages_set;", CR, //

        "set File setalpha;", CR, //
        "?set File setbeta;", CR, //
        "set Beaver setgamma;", CR, //
        "set string setdelta;", CR, //
        OUTDENT, "}");
    compile();
  }

}
