/*
 * Copyright 2015 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.equalsMethodDeclaration;
import static com.google.errorprone.matchers.Matchers.instanceEqualsInvocation;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticEqualsInvocation;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.toType;

import com.google.auto.value.AutoValue;
import com.google.common.base.Predicate;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.Signatures;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;

/** @author dsharinov@versonix.com */
@BugPattern(
    name = "EqualsNotImplemented",
    summary = "Calling equal on a class not explicitly implementing it",
    severity = WARNING)
public class EqualsNotImplemented extends BugChecker implements MethodInvocationTreeMatcher {
  private static final Matcher<MethodInvocationTree> STATIC_EQUALS_MATCHER =
      staticEqualsInvocation();

  private static final Matcher<ExpressionTree> INSTANCE_EQUALS_MATCHER = instanceEqualsInvocation();

  @Override
  public Description matchMethodInvocation(
      MethodInvocationTree invocationTree, final VisitorState state) {
    if (!STATIC_EQUALS_MATCHER.matches(invocationTree, state)
        && !INSTANCE_EQUALS_MATCHER.matches(invocationTree, state)) {
      return Description.NO_MATCH;
    }

    // This is the type of the object on which the java.lang.Object.equals() method
    // is called, either directly or indirectly via a static utility method. In the latter,
    // it is the type of the first argument to the static method.
    Type receiverType = STATIC_EQUALS_MATCHER.matches(invocationTree, state)
        ? ASTHelpers.getType(invocationTree.getArguments().get(0))
        : ASTHelpers.getReceiverType(invocationTree);

    if (!(receiverType instanceof ClassType)) {
      return Description.NO_MATCH;
    }

    ClassType receiverClass = (ClassType) receiverType;
    ClassTree classTree = ASTHelpers.findClass((ClassSymbol) receiverClass.tsym, state);

    // abstract classes should be ignored
    if (classTree == null || classTree.getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
      return NO_MATCH;
    }

    MethodTree equals = classTree.getMembers().stream()
        .filter(m -> (m instanceof MethodTree))
        .map(m -> (MethodTree) m)
        .filter(m -> equalsMethodDeclaration().matches(m, state))
        .findFirst()
        .orElse(null);

    if (equals != null) { // explicitly declared
      return NO_MATCH;
    }

    return describeMatch(invocationTree);
  }

}
