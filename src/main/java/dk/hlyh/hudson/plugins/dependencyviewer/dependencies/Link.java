/*
 * Copyright (c) 2011 Henrik Lynggaard
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
package dk.hlyh.hudson.plugins.dependencyviewer.dependencies;

public class Link  {
  
  private String upstream;
  private String downstream;
  private String cause;

  public Link(String upstream, String downstream) {
    this.upstream = upstream;
    this.downstream = downstream;
  }

  public String getDownstream() {
    return downstream;
  }

  public String getUpstream() {
    return upstream;
  }

  public String getCause() {
    return cause;
  }

  public void setCause(String cause) {
    this.cause = cause;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Link other = (Link) obj;
    if ((this.upstream == null) ? (other.upstream != null) : !this.upstream.equals(other.upstream)) {
      return false;
    }
    if ((this.downstream == null) ? (other.downstream != null) : !this.downstream.equals(other.downstream)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 37 * hash + (this.upstream != null ? this.upstream.hashCode() : 0);
    hash = 37 * hash + (this.downstream != null ? this.downstream.hashCode() : 0);
    return hash;
  }

  
  
}
