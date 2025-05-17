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
 **/
package datagen;

import static js.base.Tools.*;

import datagen.gen.Language;
import js.base.BasePrinter;

import java.io.File;

public final class Utils {

  public static final boolean ISSUE_54 = false && alert("ISSUE 54 is in effect");
  public static final boolean DEBUG_RUST_FILES = false && alert("ISSUE 47 is in effect");
  public static final boolean DEBUG_RUST_IMPORTS = false;
  public static final boolean DEBUG_RUST_LISTS = false;
  public static final boolean DEBUG_RUST_MOD = true && alert("DEBUG_RUST_MOD is in effect");

  public static final boolean DEBUG_RUST = DEBUG_RUST_FILES || DEBUG_RUST_IMPORTS || DEBUG_RUST_LISTS || DEBUG_RUST_MOD;
  public static final boolean RUST_COMMENTS = DEBUG_RUST;
  public static final String GEN_SUBDIR_NAME = "gen";
  public static final String RUST_IMPORT_ALL_PREFIX = "!all_";

  public static void pmod(Object... messages) {
    if (DEBUG_RUST_MOD)
      pr(insertStringToFront("RUST_MOD --->", messages));
  }

  public static void p54(Object... messages) {
    if (ISSUE_54)
      pr(insertStringToFront("ISSUE_54 --->", messages));
  }

  public static void loadUtils() {
    todo("weird stuff with alert json map not being found in unit tests");
    todo("also not always clickable in IDEA");
  }

  /**
   * Throw UnsupportedOperationException due to an unsupported target language
   */
  public static UnsupportedOperationException languageNotSupported(Object... messages) {
    throw notSupported(insertStringToFront("Language not supported:", messages));
  }

  private static DebugCounter crCounter = new DebugCounter("crs", 0);

  public static void addCr(StringBuilder dest) {
    crCounter.event(dest);
    dest.append('\n');
  }

  public static String sourceFileExtension() {
    return sourceFileExtension(Context.pt.language());
  }

  public static String sourceFileExtension(Language language) {
    switch (language) {
      default:
        throw languageNotSupported();
      case JAVA:
        return "java";
      case PYTHON:
        return "py";
      case GO:
        return "go";
      case RUST:
        return "rs";
    }
  }

  /**
   * Get the language-specific expression for "null" (i.e., "None" if Python)
   */
  public static String nullExpr() {
    switch (Context.config.language()) {
      default:
        throw languageNotSupported();
      case PYTHON:
        return "None";
      case JAVA:
        return "null";
      case GO:
        return "nil";
      case RUST:
        return "<rust has no nulls though!>";
    }
  }

  public static final String verboseVariant(String succinctOption, String verboseOption) {
    return Context.config.verboseNames() ? verboseOption : succinctOption;
  }

  public static String notSupportedMessage(Object... messages) {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    StackTraceElement elem = stackTrace[2];

    StringBuilder sb = new StringBuilder();
    sb.append("!!!=== Not supported yet");
    if (messages.length > 0) {
      sb.append(": ");
      sb.append(BasePrinter.toString(messages));
    } else {
      sb.append("!");
    }
    sb.append(" (");
    sb.append(elem.getFileName());
    sb.append("(");
    sb.append(elem.getLineNumber());
    sb.append("): ");
    sb.append(elem.getMethodName());
    sb.append(") ===!!!");
    return sb.toString();
  }

  /**
   * Get stack trace element at a particular depth within the current thread,
   * converted to a string that allows clicking on within (an
   * intelligent-enough) IDE.
   */
  public static String getStackTraceElement(int stackDepth) {
    stackDepth += 2;
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    if (stackTrace.length <= stackDepth) {
      return "(no stack info avail)";
    } else {
      StackTraceElement element = stackTrace[stackDepth];

      var sb = new StringBuilder();
      //
      sb.append('(');
      sb.append(element.getFileName());
      sb.append(':');
      sb.append(element.getLineNumber());
      sb.append("; ");

      // The method name maybe just adds unnecessary clutter.
      sb.append(element.getMethodName());
      sb.append(')');
      return sb.toString();
    }
  }


  public static File determineGenDirectory(File sourceFile) {
    String path = sourceFile.toString();
    int cursor = path.lastIndexOf("/gen/");
    checkArgument(cursor >= 0, "Cannot find generated directory for source file:", sourceFile);
    return new File(path.substring(0, cursor) + "/gen");
  }

}

