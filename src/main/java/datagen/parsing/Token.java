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
package datagen.parsing;

import static js.base.Tools.*;

import js.base.BasePrinter;

public final class Token {

  public Token(String source, int id, String text, int lineNumber, int column) {
    mSource = source;
    mId = id;
    mText = text;
    mRow = lineNumber;
    mColumn = column;
  }

  public boolean isUnknown() {
    return mId == DFA.UNKNOWN_TOKEN;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    if (mSource != null) {
      sb.append(mSource);
      sb.append(' ');
    }
    sb.append("row " + mRow + ", column " + mColumn);
    sb.append(", id " + id());
    sb.append(")");
    tab(sb, 25);
    sb.append(" : ");
    sb.append(mText);
    return sb.toString();
  }

  public String locInfo() {
    StringBuilder sb = new StringBuilder();
    if (mSource != null) {
      sb.append(mSource);
    }
    sb.append("(");
    sb.append(mRow);
    sb.append(", ");
    sb.append(mColumn);
    sb.append(")");
    return sb.toString();
  }

  public int id() {
    return mId;
  }

  public boolean id(int value) {
    return mId == value;
  }

  public String text() {
    return mText;
  }

  public int row() {
    return mRow;
  }

  public int column() {
    return mColumn;
  }

  public ScanException fail(Object... messages) {
    String reason;
    if (messages == null)
      reason = "Unspecified problem";
    else {
      BasePrinter p = new BasePrinter();
      p.pr(messages);
      reason = p.toString();
    }
    return new ScanException(this, reason);
  }

  private final String mSource;
  private final int mId;
  private final String mText;
  private final int mRow;
  private final int mColumn;

}
