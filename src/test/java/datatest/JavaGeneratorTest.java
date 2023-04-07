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

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.junit.Test;

import datagen.gen.SampleDataType;
import js.data.ByteArray;
import js.data.DataUtil;
import js.data.FloatArray;
import js.data.IntArray;
import js.data.LongArray;
import js.data.ShortArray;
import js.file.Files;
import js.json.JSMap;

import static js.base.Tools.*;

public class JavaGeneratorTest extends GenBaseTest {

  @Test
  public void singleLineCommentOld() {
    p().pr("// A single-line comment", CR);
    p().pr("fields {}");
    compile();
  }

  @Test
  public void multiLineCommentOld() {
    p().pr("/* A multiline", CR);
    p().pr("     comment", CR);
    p().pr("  */   ");
    p().pr("fields {}");
    compile();
  }

  @Test
  public void typeIntVariousOld() {
    p().pr("fields {", INDENT, //
        "int alpha;", CR, //
        "int beta = " + Integer.MAX_VALUE + ";", CR, //
        "?int gamma;", CR, //
        "?*int epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeIntListSerializationOld() {
    SampleDataType.Builder b = SampleDataType.newBuilder();
    b.i3(SAMPLE_INTS);
    SampleDataType x = b.build();
    String s = DataUtil.toString(x);
    JSMap m = new JSMap(s);
    SampleDataType u2 = SampleDataType.DEFAULT_INSTANCE.parse(m);
    log("Original:", INDENT, x);
    log("Parsed:", INDENT, u2);
    assertEquals(x, u2);
  }

  @Test
  public void typeByteVariousOld() {
    p().pr("fields {", INDENT, //
        "byte alpha;", CR, //
        "byte beta = " + Byte.MAX_VALUE + ";", CR, //
        "?byte gamma;", CR, //
        "?*byte epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

  private static byte[] SAMPLE_BYTES = { //
      -128, -127, -126, -125, //
      -4, -3, -2, -1, //
      0, 1, 2, 3, //
      4, 5, 6, 7, 124, 125, 126, 127,//
  };

  private static short[] SAMPLE_SHORTS = { //
      Short.MIN_VALUE, Short.MIN_VALUE + 1, Short.MIN_VALUE + 2, Short.MIN_VALUE + 3, //
      -4, -3, -2, -1, //
      0, 1, 2, 3, //
      Short.MAX_VALUE - 3, Short.MAX_VALUE - 2, Short.MAX_VALUE - 1, Short.MAX_VALUE, };

  private static int[] SAMPLE_INTS = { //
      Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 3, //
      -4, -3, -2, -1, //
      0, 1, 2, 3, //
      Integer.MAX_VALUE - 3, Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE, };

  private static long[] SAMPLE_LONGS = { //
      Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MIN_VALUE + 2, Long.MIN_VALUE + 3, //
      -4, -3, -2, -1, //
      0, 1, 2, 3, //
      Long.MAX_VALUE - 3, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE, };

  @Test
  public void typeByteListSerialization() {
    SampleDataType.Builder b = SampleDataType.newBuilder();
    b.b3(SAMPLE_BYTES);
    SampleDataType x = b.build();
    String s = DataUtil.toString(x);
    JSMap m = new JSMap(s);
    SampleDataType u2 = SampleDataType.DEFAULT_INSTANCE.parse(m);
    log("Original:", INDENT, x);
    log("Parsed:", INDENT, u2);
    assertEquals(x, u2);
  }

  @Test
  public void typeShortVariousOld() {
    p().pr("fields {", INDENT, //
        "short alpha;", CR, //
        "short beta = " + Byte.MAX_VALUE + ";", CR, //
        "?short gamma;", CR, //
        "?*short epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeShortListSerialization() {
    SampleDataType.Builder b = SampleDataType.newBuilder();
    b.s3(SAMPLE_SHORTS);
    SampleDataType x = b.build();
    String s = DataUtil.toString(x);
    JSMap m = new JSMap(s);
    SampleDataType u2 = SampleDataType.DEFAULT_INSTANCE.parse(m);
    log("Original:", INDENT, x);
    log("Parsed:", INDENT, u2);
    assertEquals(x, u2);
  }

  @Test
  public void typeLongVariousOld() {
    p().pr("fields {", INDENT, //
        "long alpha;", CR, //
        "long beta = " + Long.MAX_VALUE + ";", CR, //
        "?long gamma;", CR, //
        "?*long epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

  private void dump(String heading, long[] array) {
    if (!verbose())
      return;
    pr("long array:", heading);
    pr(LongArray.with(array).toJson());
  }

  @Test
  public void typeLongListSerialization() {
    SampleDataType.Builder b = SampleDataType.newBuilder();
    b.l3(SAMPLE_LONGS);
    SampleDataType x = b.build();
    String s = DataUtil.toString(x);
    JSMap m = new JSMap(s);
    SampleDataType u2 = SampleDataType.DEFAULT_INSTANCE.parse(m);
    log("Original:", INDENT, x);
    log("Parsed:", INDENT, u2);
    dump("original:", x.l3());
    dump("parsed:", u2.l3());
    assertEquals(x, u2);
  }

  @Test
  public void typeFloatVariousOld() {
    p().pr("fields {", INDENT, //
        "float alpha;", CR, //
        "float beta = " + Float.MAX_VALUE + ";", CR, //
        "?float gamma;", CR, //
        "?*float epsilon;", CR, //
        "*float hotel;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeFloatListSerialization() {
    SampleDataType.Builder u = SampleDataType.newBuilder();
    float[] SAMPLE_FLOATS = { 3.1415f, 1000f };

    u.f3(SAMPLE_FLOATS);
    SampleDataType v = u.build();
    String s = DataUtil.toString(v);

    JSMap m = new JSMap(s);
    SampleDataType w = SampleDataType.DEFAULT_INSTANCE.parse(m);
    log("Original:", INDENT, v);
    log("Parsed:", INDENT, w);
    assertEquals(v, w);
  }

  @Test
  public void typeFloatListSerialization2() {
    SampleDataType.Builder u = SampleDataType.newBuilder();
    FloatArray.Builder f = FloatArray.newBuilder();
    for (int i = 0; i < 20; i++)
      f.add(i * 37f - 8.2f);
    u.f3(f.array());
    SampleDataType v = u.build();
    String s = DataUtil.toString(v);
    log(s);

    JSMap m = new JSMap(s);
    SampleDataType w = SampleDataType.DEFAULT_INSTANCE.parse(m);
    log("Original:", INDENT, v);
    log("Parsed:", INDENT, w);
    assertEquals(v, w);
  }

  @Test
  public void typeDoubleListSerialization() {
    SampleDataType.Builder u = SampleDataType.newBuilder();
    double[] SAMPLE_DOUBLES = { 3.1415, 1000.0 };

    u.d3(SAMPLE_DOUBLES);
    SampleDataType v = u.build();
    String s = DataUtil.toString(v);

    JSMap m = new JSMap(s);
    SampleDataType w = SampleDataType.DEFAULT_INSTANCE.parse(m);
    log("Original:", INDENT, v);
    log("Parsed:", INDENT, w);
    assertEquals(v, w);
  }

  @Test
  public void typeDoubleVariousOld() {
    p().pr("fields {", INDENT, //
        "double alpha;", CR, //
        "double beta = " + Double.MAX_VALUE + ";", CR, //
        "?double gamma;", CR, //
        "?*double epsilon;", CR, //
        "*double hotel = [3, 1e-3];", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeStringOld() {
    verboseNames();
    p().pr("fields {", INDENT, //
        "string alpha;", CR, //
        "string beta = \"hello\";", CR, //
        "?string gamma;", CR, //
        "*string delta;", CR, //
        "?*string epsilon;", CR, //
        "*string hotel = [\"abc\",\"123\"];", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeBoolOld() {
    p().pr("fields {", INDENT, //
        "bool alpha;", CR, //
        "bool beta = true;", CR, //
        "?bool gamma;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void externalOld() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "fields {", INDENT, //
        "Beaver busy;", CR, //
        "?Beaver opt;", CR, //
        OUTDENT, "}");
    compile();
  }

  /**
   * Fields of type 'string' must not be declared as 'String'
   */
  @Test(expected = RuntimeException.class)
  public void misspelledString() {
    p().pr(//
        "fields {", INDENT, //
        "String not_camel_case;", CR, //
        OUTDENT, "}");
    compile();
  }

  /**
   * Fields of type 'bool' must not be declared as 'boolean'
   */
  @Test(expected = RuntimeException.class)
  public void misspelledBool() {
    p().pr(//
        "fields {", INDENT, //
        "boolean bad_type;", CR, //
        OUTDENT, "}");
    compile();
  }

  /**
   * Fields of type 'bool' must not be declared as 'Bool'
   */
  @Test(expected = RuntimeException.class)
  public void misspelledBool2() {
    p().pr(//
        "fields {", INDENT, //
        "Bool bad_type;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void externalListOld() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "fields {", INDENT, //
        "*Beaver mult;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void external2Old() {
    p().pr( // This one omits the 'extern' line, so it assumes the same package
        "fields {", INDENT, //
        "Beaver busy;", CR, //
        "?Beaver opt;", CR, //
        OUTDENT, "}");
    generateDummyDatFile("Beaver");
    compile();
  }

  @Test
  public void externalRepeatedOld() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "fields {", INDENT, //
        "*Beaver busy;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void listOptOld() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "fields {", INDENT, //
        "?*Beaver busy;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void enumExample() {
    p().pr("enum {", INDENT, //
        "alpha, bravo, charlie;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void typeFileVariousOld() {
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
  public void optCommentsOld() {
    addArg("comments");
    p().pr("fields {", INDENT, //
        "File alpha;", CR, //
        "int beta;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void builtInOld() {
    p().pr("fields {", INDENT, //
        "IPoint location;", CR, //
        "IRect x;", CR, //
        "FPoint floc;", CR, //
        "FRect frect;", CR, //
        "Matrix mat;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeJsonMapVariousOld() {
    p().pr("fields {", INDENT, //
        "JSMap alpha;", CR, //
        "?JSMap beta;", CR, //
        "*JSMap gamma;", CR, //
        "JSMap epsilon = ", quote("{\\\"a\\\":15,\\\"b\\\":\\\"hello\\\"}"), ";", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeJsonListVariousOld() {
    p().pr("fields {", INDENT, //
        "JSList alpha;", CR, //
        "?JSList beta;", CR, //
        "*JSList gamma;", CR, //
        "JSList epsilon = ", quote("[\\\"a\\\",15,\\\"b\\\",\\\"hello\\\"]"), ";", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void problem() {
    p().pr("extern js.scredit.gen.ProjectState;", CR, //
        "fields {", INDENT, //
        "int version = 14;", CR, //
        "JSMap recent_projects;", CR, //
        "?ProjectState default_project_state;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test(expected = RuntimeException.class)
  public void missingFields() {
    p().pr("extern abc.xyz.Beaver;");
    compile();
  }

  @Test(expected = RuntimeException.class)
  public void unusedReference() {
    addArg("treat_warnings_as_errors");
    p().pr("extern abc.xyz.Beaver;", CR, //
        "fields {", INDENT, //
        "int value;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void qualifiedReferenceOld() {
    p().pr("fields {", INDENT, //
        "foo.gen.MongoParams mongo_params;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void implicitEnumReferenceOld() {
    p().pr("fields {", INDENT, //
        "enum foo.gen.MongoEnum val;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test(expected = RuntimeException.class)
  public void conflictEnumReference() {
    p().pr("fields {", INDENT, //
        "foo.gen.MongoEnum first;", CR, //
        "enum foo.gen.MongoEnum second;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void secondEnumReference() {
    p().pr("fields {", INDENT, //
        "enum foo.gen.MongoEnum first;", CR, //
        "MongoEnum second;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void ipointsOld() {
    p().pr("fields {", INDENT, //
        "IPoint a;", CR, //
        "?IPoint b;", CR, //
        "*IPoint c;", CR, //
        "?*IPoint d;", CR, //
        "IPoint e = [32,64];", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeMapVariousOld() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "fields {", INDENT, //
        "map string File alpha;", CR, //
        "?map string File beta;", CR, //
        "map string Beaver gamma;", CR, //
        "map string string delta;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeBoxedValuesOld() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "fields {", INDENT, //
        "map string long name_map;", CR, //
        "set long ages_set;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeSetVariousOld() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "fields {", INDENT, //
        "set File alpha;", CR, //
        "?set File beta;", CR, //
        "set Beaver gamma;", CR, //
        "set string delta;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void base64Encoding() {
    redirectSystemOut();

    byte[] bytes = { 1, 2, -1, -2, 127, -128 };
    double[] doubles = { 1.234, 1.234e4, 1.234e8 };
    float[] floats = { 1.234f, 1.234e4f, 1.234e8f };
    short[] shorts = { 1, 2, 3, 4 };
    int[] ints = { 1, 2, 3, 4 };
    long[] longs = { 1, 2, 3, 4 };

    SampleDataType.Builder b = SampleDataType.newBuilder();
    b.b3(bytes);
    b.d3(doubles);
    b.f3(floats);
    b.s3(shorts);
    b.i3(ints);
    b.l3(longs);

    SampleDataType ba = b.build();
    JSMap json = ba.toJson();

    pr(DASHES);

    pr("Serialized data type:");
    pr(json);
    pr(DASHES);

    pr("Printing primitive arrays:");

    pr(IntArray.with(ba.i3()));
    pr(ShortArray.with(ba.s3()));
    pr(ByteArray.with(ba.b3()));
    pr(DASHES);

    SampleDataType bb = Files.parseAbstractDataOpt(SampleDataType.DEFAULT_INSTANCE, json);
    checkState(ba.equals(bb));
    pr("Verifying objects are equal after serialization/deserialization:");
    pr(bb);
    pr(DASHES);
    assertSystemOut();
  }

  @Test
  public void base64Encoding2() {
    redirectSystemOut();

    byte[] bytes = { 1, 2, 3, 4, -1, -2, -3, -4, 127, -128, 55, 66, 77, 88 };
    double[] doubles = { 1.234, 1.234e4, 1.234e8, 2., 3., 4., 5.2, 6.2, 8., 10. };
    float[] floats = { 1.234f, 1.234e4f, 1.234e8f, 2f, 3f, 4f, 5.2f, 6.2f, 8f, 10f };
    short[] shorts = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    int[] ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    long[] longs = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

    SampleDataType.Builder b = SampleDataType.newBuilder();
    b.b3(bytes);
    b.d3(doubles);
    b.f3(floats);
    b.s3(shorts);
    b.i3(ints);
    b.l3(longs);

    SampleDataType ba = b.build();
    JSMap json = ba.toJson();

    pr(DASHES);

    pr("Serialized data type:");
    pr(json);
    pr(DASHES);

    pr("Printing primitive arrays:");

    pr(IntArray.with(ba.i3()));
    pr(ShortArray.with(ba.s3()));
    pr(ByteArray.with(ba.b3()));
    pr(DASHES);

    SampleDataType bb = Files.parseAbstractDataOpt(SampleDataType.DEFAULT_INSTANCE, json);
    checkState(ba.equals(bb));
    pr("Verifying objects are equal after serialization/deserialization:");
    pr(bb);
    pr(DASHES);
    assertSystemOut();
  }

  @Test
  public void deprecationsOld() {
    p().pr("fields {", INDENT, //
        "-int alpha;", CR, //
        "int beta = " + Integer.MAX_VALUE + ";", CR, //
        "-?int gamma;", CR, //
        "-?*int epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void setStuff() {
    SampleDataType.Builder t = SampleDataType.newBuilder();
    checkState(t.s().isEmpty());

    Set<File> st = treeSet();
    st.add(new File("alpha.txt"));
    st.add(new File("beta.txt"));
    t.s(st);

    SampleDataType u = t.build();
    JSMap m = u.toJson();
    SampleDataType v = SampleDataType.DEFAULT_INSTANCE.parse(m);
    checkState(u.equals(v));

    // Note: though we are dealing with an immutable data object,
    // its fields (the set) are mutable
    //
    v.s().add(new File("gamma.txt"));
    checkState(!u.equals(v));
  }

  @Test
  public void singleLineComment() {
    p().pr("// A single-line comment", CR);
    p().pr("class {}");
    compile();
  }

  @Test
  public void multiLineComment() {
    p().pr("/* A multiline", CR);
    p().pr("     comment", CR);
    p().pr("  */   ");
    p().pr("class {}");
    compile();
  }

  @Test
  public void typeIntVarious() {
    p().pr("class {", INDENT, //
        "int alpha;", CR, //
        "int beta = " + Integer.MAX_VALUE + ";", CR, //
        "?int gamma;", CR, //
        "?*int epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeIntListSerialization() {
    SampleDataType.Builder b = SampleDataType.newBuilder();
    b.i3(SAMPLE_INTS);
    SampleDataType x = b.build();
    String s = DataUtil.toString(x);
    JSMap m = new JSMap(s);
    SampleDataType u2 = SampleDataType.DEFAULT_INSTANCE.parse(m);
    log("Original:", INDENT, x);
    log("Parsed:", INDENT, u2);
    assertEquals(x, u2);
  }

  @Test
  public void typeByteVarious() {
    p().pr("class {", INDENT, //
        "byte alpha;", CR, //
        "byte beta = " + Byte.MAX_VALUE + ";", CR, //
        "?byte gamma;", CR, //
        "?*byte epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeShortVarious() {
    p().pr("class {", INDENT, //
        "short alpha;", CR, //
        "short beta = " + Byte.MAX_VALUE + ";", CR, //
        "?short gamma;", CR, //
        "?*short epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeLongVarious() {
    p().pr("class {", INDENT, //
        "long alpha;", CR, //
        "long beta = " + Long.MAX_VALUE + ";", CR, //
        "?long gamma;", CR, //
        "?*long epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeFloatVarious() {
    p().pr("class {", INDENT, //
        "float alpha;", CR, //
        "float beta = " + Float.MAX_VALUE + ";", CR, //
        "?float gamma;", CR, //
        "?*float epsilon;", CR, //
        "*float hotel;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeDoubleVarious() {
    p().pr("class {", INDENT, //
        "double alpha;", CR, //
        "double beta = " + Double.MAX_VALUE + ";", CR, //
        "?double gamma;", CR, //
        "?*double epsilon;", CR, //
        "*double hotel = [3, 1e-3];", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeString() {
    verboseNames();
    p().pr("class {", INDENT, //
        "string alpha;", CR, //
        "string beta = \"hello\";", CR, //
        "?string gamma;", CR, //
        "*string delta;", CR, //
        "?*string epsilon;", CR, //
        "*string hotel = [\"abc\",\"123\"];", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeBool() {
    p().pr("class {", INDENT, //
        "bool alpha;", CR, //
        "bool beta = true;", CR, //
        "?bool gamma;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void external() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "class {", INDENT, //
        "Beaver busy;", CR, //
        "?Beaver opt;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void externalList() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "class {", INDENT, //
        "*Beaver mult;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void external2() {
    p().pr( // This one omits the 'extern' line, so it assumes the same package
        "class {", INDENT, //
        "Beaver busy;", CR, //
        "?Beaver opt;", CR, //
        OUTDENT, "}");
    generateDummyDatFile("Beaver");
    compile();
  }

  @Test
  public void externalRepeated() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "class {", INDENT, //
        "*Beaver busy;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void listOpt() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "class {", INDENT, //
        "?*Beaver busy;", OUTDENT, //
        "}");
    compile();
  }

  @Test
  public void typeFileVarious() {
    p().pr("class {", INDENT, //
        "File alpha;", CR, //
        "?File beta;", CR, //
        "*File gamma;", CR, //
        "?*File delta;", CR, //
        "File epsilon = ", quote("abc/xyz.txt"), ";", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void optComments() {
    addArg("comments");
    p().pr("class {", INDENT, //
        "File alpha;", CR, //
        "int beta;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void builtIn() {
    p().pr("class {", INDENT, //
        "IPoint location;", CR, //
        "IRect x;", CR, //
        "FPoint floc;", CR, //
        "FRect frect;", CR, //
        "Matrix mat;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeJsonMapVarious() {
    p().pr("class {", INDENT, //
        "JSMap alpha;", CR, //
        "?JSMap beta;", CR, //
        "*JSMap gamma;", CR, //
        "JSMap epsilon = ", quote("{\\\"a\\\":15,\\\"b\\\":\\\"hello\\\"}"), ";", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeJsonListVarious() {
    p().pr("class {", INDENT, //
        "JSList alpha;", CR, //
        "?JSList beta;", CR, //
        "*JSList gamma;", CR, //
        "JSList epsilon = ", quote("[\\\"a\\\",15,\\\"b\\\",\\\"hello\\\"]"), ";", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void qualifiedReference() {
    p().pr("class {", INDENT, //
        "foo.gen.MongoParams mongo_params;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void implicitEnumReference() {
    p().pr("class {", INDENT, //
        "enum foo.gen.MongoEnum val;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void ipoints() {
    p().pr("class {", INDENT, //
        "IPoint a;", CR, //
        "?IPoint b;", CR, //
        "*IPoint c;", CR, //
        "?*IPoint d;", CR, //
        "IPoint e = [32,64];", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeMapVarious() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "class {", INDENT, //
        "map string File alpha;", CR, //
        "?map string File beta;", CR, //
        "map string Beaver gamma;", CR, //
        "map string string delta;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeBoxedValues() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "class {", INDENT, //
        "map string long name_map;", CR, //
        "set long ages_set;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void typeSetVarious() {
    p().pr("extern abc.xyz.Beaver;", CR, //
        "class {", INDENT, //
        "set File alpha;", CR, //
        "?set File beta;", CR, //
        "set Beaver gamma;", CR, //
        "set string delta;", CR, //
        OUTDENT, "}");
    compile();
  }

  @Test
  public void deprecations() {
    p().pr("class {", INDENT, //
        "-int alpha;", CR, //
        "int beta = " + Integer.MAX_VALUE + ";", CR, //
        "-?int gamma;", CR, //
        "-?*int epsilon;", CR, //
        OUTDENT, "}");
    compile();
  }

}
