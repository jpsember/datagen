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
import js.base.Tools;

/**
 * Formats source code to a buffer
 */
public final class SourceBuilder {

  public static final Object IN = new Object();
  public static final Object OUT = new Object();
  public static final Object OPEN = new Object();
  public static final Object CLOSE = new Object();
  public static final Object QUOTE = new Object();

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
  public SourceBuilder quoteNextObject() {
    checkState(!mQuotePending);
    mQuotePending = true;
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

      if (msg == QUOTE) {
        quoteNextObject();
        continue;
      }

      doIndent();
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
    String content = mStringBuilder.toString();
    mStringBuilder = null;
    return content;
  }

  private static DebugCounter sIndentCounter = new DebugCounter("indenting", 0);

  /**
   * Move margin to right by specific amount; do a CR
   */
  public SourceBuilder in(int adjustment) {
    if (adjustment == 0)
      alertWithSkip(1, "in with zero");
    mOldIndentStack.add(mIndent);
    mIndent += adjustment;
    sIndentCounter.event(mStringBuilder, adjustment, "=", mIndent);
    cr();
    return this;
  }

  /**
   * Tab the margin to the right, and do a CR
   */
  public SourceBuilder in() {
    return in(mTabSize);
  }

  /**
   * Do a CR, and adjust indent by a particular value; to adjust for some
   * special circumstances
   */
  @Deprecated
  public SourceBuilder specialIndentAdj(int adjAmount) {
    int newIndent = mIndent + adjAmount;
    cr();
    setIndent(newIndent);
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
    setIndent(pop(mOldIndentStack));
    sIndentCounter.event(mStringBuilder, "<", mIndent);
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

  private void validate() {
    alert("performing validation");
    String problem = null;

    int len = mStringBuilder.length();
    do {

      if (mColumn > len) {
        problem = "column > buffer length";
        break;
      }

    } while (false);
    if (problem != null) {
      pr("*** Validation problem; column:", mColumn, "indent:", mIndent, "buffer length:", len);
      pr("buffer:");
      pr(Tools.insertLeftRightMargins(mStringBuilder));
      pr("Problem:", problem);
      pr("Column:", mColumn, "indent:", mIndent, "buffer length:", len);
      badState("validation problem");
    }

  }

  public void doIndent() {
    validate();
    if (mColumn < mIndent) {
      addSafe(spaces(mIndent - mColumn));
    }
  }

  private DebugCounter dAddMisc = new DebugCounter("m", 0);

  private void add(String str) {
    dAddMisc.event(mStringBuilder);
    if (str.indexOf('\n') >= 0)
      throw new IllegalArgumentException("newline in text: '" + str + "'");
    addSafe(str);
  }

  private DebugCounter dc = new DebugCounter("a", 0);

  private void addSafe(String str) {
    dc.event(mStringBuilder, "addSafe");
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

  @Deprecated
  public StringBuilder debugStringBuilder() {
    return mStringBuilder;
  }

  private final Language mLanguage;
  private boolean mQuotePending;
  private int mIndent;
  private final int mTabSize;
  private int mColumn;
  private StringBuilder mStringBuilder = new StringBuilder();
  private List<Boolean> mConditionalStack = arrayList();
  private List<Integer> mOldIndentStack = arrayList();

}
