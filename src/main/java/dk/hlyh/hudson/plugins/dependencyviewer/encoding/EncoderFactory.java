/*
 * Copyright (c) 2012 Henrik Lynggaard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.hlyh.hudson.plugins.dependencyviewer.encoding;

import dk.hlyh.hudson.plugins.dependencyviewer.dependencies.GraphBuilder;
import java.util.Locale;

public final class EncoderFactory {

  private EncoderFactory() {
  }

  public static Encoder create(GraphBuilder calculator,Locale currentLocale,String filename) {
    
    // timeline
    if (filename.equalsIgnoreCase("events.xml")) {
      return new EventEncoder(calculator, currentLocale);
    }
    
    // graphvizSource
    if (filename.equalsIgnoreCase("source.gv")) {
      return new GraphvizSourceEncoder(calculator, currentLocale);
    }
    
    // graphviz
    if (filename.equalsIgnoreCase("graph.png")) {
      return new GraphvizEncoder(calculator, currentLocale, "image/png", "png");
    }
    if (filename.equalsIgnoreCase("graph.svg")) {
      return new GraphvizEncoder(calculator, currentLocale, "image/svg", "svg");
    }
    if (filename.equalsIgnoreCase("graph.map")) {
      return new GraphvizEncoder(calculator, currentLocale,"image/cmapx", "cmapx");
    }           
    return null;    
  }
}
