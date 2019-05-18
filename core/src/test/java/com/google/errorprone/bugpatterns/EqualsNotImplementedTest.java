package com.google.errorprone.bugpatterns;

import static org.junit.Assert.*;

import com.google.errorprone.CompilationTestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** @author dsharinov@gmail.com */
@RunWith(JUnit4.class)
public class EqualsNotImplementedTest {
  private CompilationTestHelper compilationHelper;

  @Before
  public void setUp() {
    compilationHelper = CompilationTestHelper.newInstance(EqualsNotImplemented.class, getClass());
  }

  @Test
  public void testPositiveCase() {
    compilationHelper.addSourceFile("EqualsNotImplementedPositiveCases.java").doTest();
  }

  @Test
  public void testNegativeCase() {
    compilationHelper.addSourceFile("EqualsNotImplementedNegativeCases.java").doTest();
  }

}