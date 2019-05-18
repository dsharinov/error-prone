package com.google.errorprone.bugpatterns.testdata;

/** @author dsharinov@gmail.com */
public class EqualsNotImplementedPositiveCases {

  static class A {
    final int id;

    A(int id) {
      this.id = id;
    }
  }

  void checkSameIdEquals() {
    A a1 = new A(0);
    A a2 = new A(0);
    // BUG: Diagnostic contains: Calling equal on a class not explicitly implementing it
    a1.equals(a2);
  }

}
