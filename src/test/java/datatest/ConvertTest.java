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

import datagen.Datagen;
import datagen.gen.DatagenConfig;
import js.app.App;
import js.data.DataUtil;
import js.testutil.MyTestCase;

import static js.base.Tools.*;

import java.util.List;

public class ConvertTest extends MyTestCase {

  @Test
  public void convert() {
    compile();
  }

  private void compile() {
    addArg(DatagenConfig.PROTO_PATH, "unit_test/old_format/proto");
    addArg(DatagenConfig.DAT_PATH, generatedFile("dat_files"));
    if (verbose())
      addArg("--verbose");
    App app = new Datagen();
    addArg("--exceptions");
    app.startApplication(DataUtil.toStringArray(args()));
    if (app.returnCode() != 0)
      throw die(app.getError());
    assertGenerated();
  }

  private void addArg(Object... args) {
    for (Object a : args)
      mArgs.add(a.toString());
  }

  private List<String> args() {
    return mArgs;
  }

  private List<String> mArgs = arrayList();

}
