package org.resolvetosavelives.red.router.screen;

import android.view.View;

import flow.Flow;

public class Poop {

  void oncreate(final View context) {
    Supplier<Flow> p = new Supplier<Flow>() {
      @Override
      public Flow get() {
        return Flow.get(context);
      }
    };

    Supplier<Flow> p2 = () -> Flow.get(context);
  }
}
