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

import static js.base.Tools.*;

import org.junit.Test;

import datagen.Context;
import datagen.gen.DatagenConfig;
import datagen.gen.Language;
import js.testutil.MyTestCase;

public class ParseToolsTest extends MyTestCase {

  @Test
  public void adjustLineFeeds() {
    a("\n");
    a("\n");
    a("   xyz\n   uvw\n\n   abc\n\n\n   def\n\n\n\n  hij\n   \nklm\n  \n   \n     \nabc");
    perform();
  }

  private void a(Object... objects) {
    for (Object obj : objects)
      mBuffer.append(obj.toString());
  }

  private void perform() {
    loadTools();
    // We have to prepare a context before calling ParseTools
    Context.prepareApp(files(), DatagenConfig.newBuilder().language(Language.JAVA));
    assertMessage(Context.pt.adjustLinefeeds(mBuffer.toString()));
  }

  private StringBuilder mBuffer = new StringBuilder();
}
