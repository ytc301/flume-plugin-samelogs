/**
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.flume.conf;

import static org.junit.Assert.assertEquals;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.junit.Test;

/**
 * This tests flume spec code generator -- the tests parse values to generate an
 * AST and then code gen the AST them back to their original form.
 */
public class TestFlumeSpecGen {

  @Test
  public void testHex() throws RecognitionException, FlumeSpecException {
    String hex = "0x1234";
    CommonTree thex = FlumeBuilder.parseLiteral(hex);
    String outHex = FlumeSpecGen.genArg(thex);
    assertEquals(hex, outHex);
  }

  @Test
  public void testDec() throws RecognitionException, FlumeSpecException {
    String orig = "12234";
    CommonTree tree = FlumeBuilder.parseLiteral(orig);
    String out = FlumeSpecGen.genArg(tree);
    assertEquals(orig, out);
  }

  @Test
  public void testString() throws RecognitionException, FlumeSpecException {
    String orig = "\"This is a a string\"";
    CommonTree tree = FlumeBuilder.parseLiteral(orig);
    String out = FlumeSpecGen.genArg(tree);
    assertEquals(orig, out);
  }

  @Test
  public void testFunc() throws RecognitionException, FlumeSpecException {
    String orig = "foo(\"bar\", 42)";
    CommonTree tree = FlumeBuilder.parseArg(orig);
    String out = FlumeSpecGen.genArg(tree);
    assertEquals(orig, out);
  }

  /**
   * Spaces are important for the next test cases.
   */
  @Test
  public void testSource() throws RecognitionException, FlumeSpecException {
    String orig = "twitter";
    CommonTree tree = FlumeBuilder.parseSource(orig);
    String out = FlumeSpecGen.genEventSource(tree);
    assertEquals(orig, out);

    orig = "text( \"/tmp/file\" )";
    tree = FlumeBuilder.parseSource(orig);
    out = FlumeSpecGen.genEventSource(tree);
    assertEquals(orig, out);
  }

  /**
   * Spaces are important for the next test cases.
   */
  @Test
  public void testSink() throws RecognitionException, FlumeSpecException {
    String orig = "null";
    CommonTree tree = FlumeBuilder.parseSink(orig);
    String out = FlumeSpecGen.genEventSink(tree);
    assertEquals(orig, out);

    orig = "thriftSink( 1234 )";
    tree = FlumeBuilder.parseSink(orig);
    out = FlumeSpecGen.genEventSink(tree);
    assertEquals(orig, out);

    orig = "text( \"/tmp/file\" )";
    tree = FlumeBuilder.parseSink(orig);
    out = FlumeSpecGen.genEventSink(tree);
    assertEquals(orig, out);
  }

  /**
   * Spaces are important for this test case.
   */
  @Test
  public void testDecoratedSink() throws RecognitionException,
      FlumeSpecException {
    String orig = "{ flakey( .90 ) => null }";
    CommonTree tree = FlumeBuilder.parseSink(orig);
    String out = FlumeSpecGen.genEventSink(tree);
    assertEquals(orig, out);

    orig = "{ intervalSampler( 20 ) => thriftSink( 1234 ) }";
    tree = FlumeBuilder.parseSink(orig);
    out = FlumeSpecGen.genEventSink(tree);
    assertEquals(orig, out);

    orig = "{ stubbornAppend => { flakey( .90 ) => text( \"/tmp/file\" ) } }";
    tree = FlumeBuilder.parseSink(orig);
    out = FlumeSpecGen.genEventSink(tree);
    assertEquals(orig, out);
  }

  /**
   * This tests to make sure that a multi sink is parsed properly and then
   * regenerated by the spec generator.
   */
  @Test
  public void testMulti() throws RecognitionException, FlumeSpecException {
    String orig = "[ counter( \"foo\" ), counter( \"bar\" ), counter( \"baz\" ) ]";
    CommonTree tree = FlumeBuilder.parseSink(orig);
    String out = FlumeSpecGen.genEventSink(tree);
    assertEquals(orig, out);
  }

  /**
   * This tests to make sure that a backup sink is parsed properly and then
   * regenerated by the spec generator.
   */
  @Test
  public void testBackup() throws RecognitionException, FlumeSpecException {
    String orig = "< counter( \"foo\" ) ? counter( \"bar\" ) >";
    CommonTree tree = FlumeBuilder.parseSink(orig);
    String out = FlumeSpecGen.genEventSink(tree);
    assertEquals(orig, out);
  }

  /**
   * This tests to make sure that a roll sink is parsed properly and then
   * regenerated by the spec generator.
   */
  @Test
  public void testRoll() throws RecognitionException, FlumeSpecException {
    String orig = "roll( 12345 ) { counter( \"foo\" ) }";
    CommonTree tree = FlumeBuilder.parseSink(orig);
    String out = FlumeSpecGen.genEventSink(tree);
    assertEquals(orig, out);
  }

  /**
   * This tests to make sure that a generator sink is parsed properly and then
   * regenerated by the spec generator.
   */
  @Test
  public void testGen() throws RecognitionException, FlumeSpecException {
    String orig = "collector( 12345 ) { counter( \"foo\" ) }";
    CommonTree tree = FlumeBuilder.parseSink(orig);
    String out = FlumeSpecGen.genEventSink(tree);
    assertEquals(orig, out);
  }

  @Test
  public void testKeywordArgGen() throws RecognitionException,
      FlumeSpecException {

    // kwargs only
    String s = "text( bogus=\"bogusdata\", foo=\"bar\" )";
    CommonTree o = FlumeBuilder.parseSink(s);
    String out = FlumeSpecGen.genEventSink(o);
    assertEquals(s, out);

    // normal arg then kwargs
    String s2 = "text( \"bogusdata\", foo=\"bar\" )";
    CommonTree o2 = FlumeBuilder.parseSink(s2);
    out = FlumeSpecGen.genEventSink(o2);
    assertEquals(s2, out);

    // normal arg then kwargs
    s2 = "text( \"bogusdata\", foo=\"bar\", boo=1.5 )";
    o2 = FlumeBuilder.parseSink(s2);
    out = FlumeSpecGen.genEventSink(o2);
    assertEquals(s2, out);
  }

  @Test
  public void testFuncs() throws RecognitionException, FlumeSpecException {
    // func arg
    String s = "text( baz(42), foo=\"bar\" )";
    CommonTree o = FlumeBuilder.parseSink(s);
    String out = FlumeSpecGen.genEventSink(o);
    assertEquals(s, out);

    // func with func arg
    String s2 = "text( baz(bog(\"boo\")), foo=\"bar\" )";
    CommonTree o2 = FlumeBuilder.parseSink(s2);
    out = FlumeSpecGen.genEventSink(o2);
    assertEquals(s2, out);

    // func as kwarg
    String s3 = "text( bogus=baz(42), foo=\"bar\" )";
    CommonTree o3 = FlumeBuilder.parseSink(s3);
    out = FlumeSpecGen.genEventSink(o3);
    assertEquals(s3, out);
  }

}
