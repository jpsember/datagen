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
package datagen;

import static js.base.Tools.*;

import java.util.List;

import static datagen.Utils.*;
import datagen.gen.Language;
import js.base.BasePrinter;

/**
 * Formats source code to a buffer
 */
public final class SourceBuilder {

  public static final Object IN = new Object();
  public static final Object OUT = new Object();
  public static final Object OPEN = new Object();
  public static final Object CLOSE = new Object();
  public static final Object QUOTE = new Object();
  public static final Object COMMA = new Object();

  public SourceBuilder(Language language) {
    mLanguage = language;
    mTabSize = 2;
  }

  public String toString() {
    throw die("unexpected! Did you mean to call content()?");
  }

  /**
   * Wrap the next object within quotes (")
   */
  private SourceBuilder quoteNextObject() {
    checkState(!mQuotePending);
    mQuotePending = true;
    return this;
  }

  public SourceBuilder comment(Object... messages) {
    alert("generating debug comments");
    cr();
    a("// ");
    a(BasePrinter.toString(messages));
    cr();
    return this;
  }

  /**
   * Append the string representations of a list of objects
   */
  public SourceBuilder a(Object... messages) {

    for (Object msg : messages) {
      if (msg == CR) {
        cr();
        continue;
      }
      if (msg == BR) {
        br();
        continue;
      }
      if (msg == IN) {
        in();
        continue;
      }
      if (msg == OUT) {
        out();
        continue;
      }
      if (msg == OPEN) {
        open();
        continue;
      }
      if (msg == CLOSE) {
        close();
        continue;
      }
      if (msg == COMMA) {
        comma();
        continue;
      }
      if (msg == QUOTE) {
        quoteNextObject();
        continue;
      }

      if (mColumn < mIndent) {
        addSafe(spaces(mIndent - mColumn));
      }
      String s = msg.toString();

      if (mQuotePending) {
        s = quote(s);
        mQuotePending = false;
      }

      add(s);
    }
    return this;
  }

  public SourceBuilder br() {
    if (mStringBuilder.length() > 0) {
      generateCr();
    }
    return this;
  }

  public SourceBuilder cr() {
    if (prevChar('\n') != '\n') {
      generateCr();
    }
    return this;
  }

  public SourceBuilder setIndent(int indent) {
    mIndent = indent;
    return this;
  }

  /**
   * Get content, and disable the SourceBuilder (it should not be used after
   * this is called)
   */
  public String getContent() {
    //pw("getContent, non null:",mStringBuilder != null);
    String content = mStringBuilder.toString();
    mStringBuilder = null;
    return content;
  }

  /**
   * Tab the margin to the right, and do a CR
   */
  public SourceBuilder in() {
    mIndent += mTabSize;
    cr();
    return this;
  }

  private boolean python() {
    return mLanguage == Language.PYTHON;
  }

  /**
   * Add: a space (if previous character wasn't whitespace), '{', IN
   */
  public SourceBuilder open() {
    if (python()) {
      return in();
    }

    if (prevChar(' ') > ' ')
      addSafe(" ");
    a("{");
    return in();
  }

  /**
   * Add: OUT, '}', CR
   */
  public SourceBuilder close() {
    out();
    if (!python())
      a("}");
    return cr();
  }

  /**
   * Do a CR, and tab the margin to the left
   */
  public SourceBuilder out() {
    cr();
    return outNoCr();
  }

  /**
   * Tab margin to left (without a cr)
   */
  public SourceBuilder outNoCr() {
    setIndent(mIndent - mTabSize);
    return this;
  }

  /**
   * If boolean flag is true, generate messages; must be balanced by call to
   * endIf(...)
   */
  public SourceBuilder doIf(boolean condition, Object... messages) {
    mConditionalStack.add(condition);
    if (condition)
      a(messages);
    return this;
  }

  /**
   * If boolean flag for previous doIf() was true, generate messages
   */
  public SourceBuilder endIf(Object... messages) {
    boolean condition = pop(mConditionalStack);
    if (condition)
      a(messages);
    return this;
  }

  private void add(String str) {
    if (str.indexOf('\n') >= 0)
      throw new IllegalArgumentException("newline in text: '" + str + "'");
    addSafe(str);
  }

  public void addSafe(String str) {
    mStringBuilder.append(str);
    mColumn += str.length();
  }

  private void generateCr() {
    addCr(mStringBuilder);
    mColumn = 0;
  }

  private char prevChar(char def) {
    if (mStringBuilder.length() == 0)
      return def;
    return mStringBuilder.charAt(mStringBuilder.length() - 1);
  }

  private final Language mLanguage;
  private final int mTabSize;
  private int mIndent;
  private int mColumn;
  private boolean mQuotePending;
  private StringBuilder mStringBuilder = new StringBuilder();
  private List<Boolean> mConditionalStack = arrayList();
  private int mCommaState;

  public SourceBuilder startComma() {
    checkState(mCommaState == 0, "comma nesting problem");
    mCommaState = 1;
    return this;
  }

  public SourceBuilder endComma() {
    checkState(mCommaState > 0, "endComma without startComma");
    mCommaState = 0;
    return this;
  }

  public SourceBuilder comma() {
    checkState(mCommaState != 0, "no startComma() was made");
    if (mCommaState > 1) {
      a(", ");
    } else {
      mCommaState = 1;
    }
    return this;
  }

  public void wtf() {
    checkNotNull(mStringBuilder);
  }

}
