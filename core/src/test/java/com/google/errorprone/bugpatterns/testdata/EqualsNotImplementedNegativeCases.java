package com.google.errorprone.bugpatterns.testdata;

import java.util.Objects;

/** @author dsharinov@gmail.com */
public class EqualsNotImplementedNegativeCases {

  static class A {
    final int id;

    A(int id) {
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      A a = (A) o;
      return id == a.id;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }
  }

  void checkSameIdEquals() {
    A a1 = new A(0);
    A a2 = new A(0);
    a1.equals(a2);
  }

}
