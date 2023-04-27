package datagen;

import static js.base.Tools.*;

import js.base.BasePrinter;

public class DebugCounter {

  public DebugCounter(String name, int target) {
    loadTools();
    if (target != 0)
      alert("DebugCounter is active:", name);
    mTarget = target;
  }

  public void event(StringBuilder target, Object... messages) {
    if (mTarget == 0)
      return;
    mCounter++;
    checkState(mCounter != mTarget, "DebugCounter reached target");
    target.append('[');
    target.append(mCounter);
    if (messages.length > 0) {
      target.append(":");
      target.append(BasePrinter.toString(messages));
      target.append(']');
    }

  }

  private final int mTarget;
  private int mCounter;
}
