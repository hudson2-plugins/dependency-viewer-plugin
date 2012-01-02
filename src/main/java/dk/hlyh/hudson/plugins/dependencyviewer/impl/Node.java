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
package dk.hlyh.hudson.plugins.dependencyviewer.impl;

import hudson.model.Result;
import java.util.Date;

public class Node {
    
  private String name;
  private Type type;
  private String projectName;
  private int buildNumber;
  private String url;
  private String duration;
  private long durationMillis;

  public long getDurationMillis() {
    return durationMillis;
  }

  public void setDurationMillis(long durationMillis) {
    this.durationMillis = durationMillis;
  }
  private Result result;
  private Status status;
  private Date buildStart;
  private Date buildEnd ;
        
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getBuildNumber() {
    return buildNumber;
  }

  public void setBuildNumber(int buildNumber) {
    this.buildNumber = buildNumber;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public Result getResult() {
    return result;
  }

  public void setResult(Result result) {
    this.result = result;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }



  public Date getBuildEnd() {
    return buildEnd;
  }

  public void setBuildEnd(Date buildEnd) {
    this.buildEnd = buildEnd;
  }

  public Date getBuildStart() {
    return buildStart;
  }

  public void setBuildStart(Date buildStart) {
    this.buildStart = buildStart;
  }
       
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Node other = (Node) obj;
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
    return hash;
  }

  public static enum Type {
    Project,
    Build
  }
  
  public static enum Status {
    Planned,
    Queue,
    Building,
    Completed
  }
  
}
