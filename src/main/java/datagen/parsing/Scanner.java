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

import static datagen.ParseTools.*;
import static js.base.Tools.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public final class Scanner {

  private static final int SKIP_ID_NONE = -10000;

  public Scanner(DFA dfa, Reader reader, int skipId) {
    mDfa = dfa;
    mSkipId = (skipId < 0 ? SKIP_ID_NONE : skipId);
    mReader = reader;
  }

  public Scanner(DFA dfa, Reader reader) {
    this(dfa, reader, 0);
  }

  public Scanner(DFA dfa, String string, int skipId) {
    this(dfa, new StringReader(string), skipId);
  }

  public Scanner(DFA dfa, String string) {
    this(dfa, new StringReader(string), 0);
  }

  public void setAcceptUnknownTokens() {
    mAcceptUnknownTokens = true;
  }

  public void setSourceDescription(String description) {
    mSourceDescription = description;
  }

  private String mSourceDescription;

  /**
   * Determine next token, without reading it
   *
   * @return token, or null if end of input
   */
  public Token peek() {
    if (mHistoryCursor == mHistory.size()) {
      // Repeat until we find a non-skipped token, or end of input
      while (true) {
        Token token = peekAux();
        if (token == null)
          break;

        // Advance the column, row numbers
        String tokenText = token.text();
        for (int i = 0; i < tokenText.length(); i++) {
          char c = tokenText.charAt(i);
          mColumn++;
          if (c == '\n') {
            mLineNumber++;
            mColumn = 0;
          }
        }
        if (!token.id(mSkipId)) {
          mHistory.add(token);
          break;
        }
      }
    }
    Token ret = null;
    if (mHistoryCursor < mHistory.size()) {
      ret = mHistory.get(mHistoryCursor);
    }
    return ret;
  }

  private Token peekAux() {
    if (peekChar(0) < 0)
      return null;
    int bestLength = 1;
    int bestId = DFA.UNKNOWN_TOKEN;
    int charOffset = 0;
    State state = mDfa.getStartState();
    while (true) {

      int ch = peekChar(charOffset);
      State nextState = null;
      for (Edge edge : state.edges()) {
        if (mDfa.getFinalStateIndex() == edge.destinationStateId()) {
          int newTokenId = edgeLabelToTokenId(edge.codeRange()[0]);
          if (newTokenId >= bestId || charOffset > bestLength) {
            bestLength = charOffset;
            bestId = newTokenId;
          }
        } else {
          int[] range = edge.codeRange();
          if (rangeContainsValue(range, ch)) {
            nextState = mDfa.getState(edge.destinationStateId());
            break;
          }
        }
      }
      if (nextState == null)
        break;
      state = nextState;
      charOffset++;
    }
    String tokenText = skipChars(bestLength);
    Token peekToken = new Token(mSourceDescription, bestId, tokenText, 1 + mLineNumber, 1 + mColumn);
    if (bestLength == 0)
      throw new ScanException(peekToken, "scanned zero-length token");
    return peekToken;
  }

  public Token read() {
    return read(-1);
  }

  public Token read(int tokenId) {
    Token token = peek();
    if (token == null)
      throw new ScanException(null, "no more tokens");
    if (!mAcceptUnknownTokens && token.isUnknown())
      throw new ScanException(token, "unknown token");
    if (tokenId >= 0) {
      if (token.id() != tokenId)
        throw new ScanException(token, "unexpected token");
    }
    mHistoryCursor++;
    return token;
  }

  public Token read(String tokenName) {
    return read(mDfa.tokenId(tokenName));
  }

  public Token readIf(String tokenName) {
    return readIf(mDfa.tokenId(tokenName));
  }

  public Token readIf(int tokenId) {
    Token token = peek();
    boolean readIt = (token != null && tokenId == token.id());
    if (readIt) {
      read();
      return token;
    }
    return null;
  }

  public boolean hasNext() {
    return peek() != null;
  }

  public String nameOf(Token token) {
    return mDfa.tokenName(token.id());
  }

  public void unread() {
    unread(1);
  }

  public void unread(int count) {
    if (mHistoryCursor < count)
      throw new ScanException(null, "Token unavailable");
    mHistoryCursor -= count;
  }

  public int readInt() {
    return (int) ensureIntegerValue(read(NUMBER).text(), Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  private static final int EPSILON = -1;

  private static final int edgeLabelToTokenId(int edgeLabel) {
    return EPSILON - 1 - edgeLabel;
  }

  private int peekChar(int index) {
    try {
      if (index < mCharacterBuffer.length()) {
        return mCharacterBuffer.charAt(index);
      }
      int nRead = mReader.read(mReaderBuffer);
      if (nRead < 0)
        return -1;
      mCharacterBuffer.append(mReaderBuffer, 0, nRead);
      return mCharacterBuffer.charAt(index);
    } catch (IOException e) {
      throw asRuntimeException(e);
    }
  }

  private String skipChars(int count) {
    String s = mCharacterBuffer.substring(0, count);
    mCharacterBuffer.delete(0, count);
    return s;
  }

  private static boolean rangeContainsValue(int[] range, int value) {
    int i = 0;
    while (i < range.length) {
      if (value < range[i])
        return false;
      if (value < range[i + 1])
        return true;
      i += 2;
    }
    return false;
  }

  private Reader mReader;
  private DFA mDfa;
  private int mSkipId;
  private int mLineNumber;
  private int mColumn;
  private boolean mAcceptUnknownTokens;
  private List<Token> mHistory = arrayList();
  private int mHistoryCursor;
  private StringBuilder mCharacterBuffer = new StringBuilder();
  private char[] mReaderBuffer = new char[256];
}
