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
package com.igormaznitsa.nbmindmap.nb.print;

import java.awt.Graphics;
import org.netbeans.spi.print.PrintPage;

public class PrintPageAdapter implements PrintPage {

  private final com.igormaznitsa.mindmap.print.PrintPage delegate;
  
  public PrintPageAdapter(final com.igormaznitsa.mindmap.print.PrintPage delegate){
    this.delegate = delegate;
  }
  
  @Override
  public void print (final Graphics g) {
    this.delegate.print(g);
  }
  
}
