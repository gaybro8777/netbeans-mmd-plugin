/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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
package com.igormaznitsa.mindmap.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.regex.Pattern;

public class ExtraLinkTest {
  
  @Test
  public void testEquals() throws Exception {
    assertEquals(new ExtraLink("http://www.google.com"), new ExtraLink("http://www.google.com"));
    assertEquals(new ExtraLink("http://www.google.com?a=1&b=2"), new ExtraLink("http://www.google.com?a=1&b=2"));
    assertNotEquals(new ExtraLink("http://www.google.com?a=1&b=2"), new ExtraLink("http://www.googlee.com?a=1&b=2"));
    assertNotEquals(new ExtraLink("http://www.google.com?a=1&b=2"), new ExtraLink("http://www.google.com?b=2&a=1"));
    assertNotEquals(new ExtraLink("http://www.google.com?a=1&b=2"), new ExtraLink("http://www.google.com?b=1&a=2"));
  }
  
  @Test
  public void testContainsPattern() throws Exception {
    final ExtraLink link = new ExtraLink("http://www.1cpp.ru/forum/YaBB.pl?num=1341507344");
    assertTrue(link.containsPattern(null, Pattern.compile(Pattern.quote("http://www.1cpp.ru/forum/YaBB.pl?num=1341507344"))));
    assertTrue(link.containsPattern(new File(System.getProperty("user.home")), Pattern.compile(Pattern.quote("http://www.1cpp.ru/forum/YaBB.pl?num=1341507344"))));
    assertTrue(link.containsPattern(null, Pattern.compile(Pattern.quote("http://www.1cpp.ru/forum/YaBB.pl?num=1341507344"),Pattern.CASE_INSENSITIVE)));
    assertTrue(link.containsPattern(new File(System.getProperty("user.home")), Pattern.compile(Pattern.quote("http://www.1cpp.ru/forum/YaBB.pl?num=1341507344"),Pattern.CASE_INSENSITIVE)));
    assertTrue(link.containsPattern(null, Pattern.compile(Pattern.quote("num=1341507344"))));
    assertTrue(link.containsPattern(new File(System.getProperty("user.home")), Pattern.compile(Pattern.quote("num=1341507344"))));
    
    assertFalse(link.containsPattern(null, Pattern.compile(Pattern.quote("yab3"),Pattern.CASE_INSENSITIVE)));
  }
  
}
