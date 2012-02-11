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
package dk.hlyh.hudson.plugins.dependencyviewer.encoding;

import dk.hlyh.hudson.plugins.dependencyviewer.dependencies.Link;
import dk.hlyh.hudson.plugins.dependencyviewer.dependencies.Node;
import dk.hlyh.hudson.plugins.dependencyviewer.util.NodeByName;
import java.text.SimpleDateFormat;
import java.util.*;

class GraphvizFormatter {
  
  private final Locale locale;
  private final SimpleDateFormat dateFormatter;

  GraphvizFormatter(Locale locale) {
    this.locale = locale;
    this.dateFormatter  = new SimpleDateFormat("H:m:s", locale);    
  }
  
  public String generateDotText(Set<Node> nodes, Set<Link> links) {
    StringBuilder builder = new StringBuilder(1024);

    List<Node> sortedNodes = new ArrayList<Node>(nodes);
    Collections.sort(sortedNodes, new NodeByName());
    builder.append("digraph {\n"
            + "node [shape=box, style=rounded];\n"
            + "subgraph clusterdepgraph {\n");

    // format all nodes    
    for (Node node : sortedNodes) {

      switch (node.getType()) {
        case Project:
          formatProjectNode(builder, node);
          break;
        case Build:
          formatBuildNode(builder, node);
      }
    }

    // format all links
    for (Link link : links) {
      formatLink(builder, link);
    }


    builder.append("color=white;"
            + "}\n"
            + "}\n");
    return builder.toString();
  }

  private void formatProjectNode(StringBuilder builder, Node node) {
    Map<String, String> attr = new HashMap<String, String>();

    attr.put("URL", node.getUrl());
    attr.put("style", "filled");
    attr.put("fillcolor", "#ffffff");

    String label = (String) node.getName() + "\\n";
    if ( node.getStatus() != Node.Status.Planned) {
      label += node.getStatus() + "\\n";
    }
    if (node.getDuration() != null) {
      label += "Estimated: " + node.getDuration();
    }
    attr.put("label", label);
    attr.put("fontsize", "10");
    formatNode(builder, node.getName(), attr);
  }

  private void formatBuildNode(StringBuilder builder, Node node) {
    Map<String, String> attr = new HashMap<String, String>();
    attr.put("URL", node.getUrl());
    attr.put("style", "filled");
    attr.put("fillcolor", node.getResult().color.getHtmlBaseColor());
    String label = (String) node.getName() + "\\n";
    label += "Started: " + dateFormatter.format(node.getBuildStart())+"\\n";
    label += "Duration: "  + node.getDuration();
    
    attr.put("label", label);
    attr.put("fontsize", "10");
    formatNode(builder, node.getName(), attr);
  }

  private void formatNode(StringBuilder builder, String name, Map<String, String> attr) {
    escapeString(builder, name);
    builder.append(" [");
    for (Map.Entry<String, String> entry : attr.entrySet()) {
      builder.append(entry.getKey());
      builder.append("=");
      escapeString(builder, entry.getValue());
      builder.append(" ");
    }
    builder.append("];\n");
  }

  private void formatLink(StringBuilder builder, Link link) {

    Map<String, String> attr = new HashMap<String, String>();
    if (link.getCause() != null) {
      attr.put("label", link.getCause());
    }
    attr.put("fontsize", "9");
    formatLink(builder, link, attr);
  }

  private void formatLink(StringBuilder builder, Link link, Map<String, String> attr) {
    escapeString(builder, link.getUpstream());
    builder.append(" -> ");
    escapeString(builder, link.getDownstream());
    builder.append(" [");
    for (Map.Entry<String, String> entry : attr.entrySet()) {
      builder.append(entry.getKey());
      builder.append("=");
      escapeString(builder, entry.getValue());
      builder.append(" ");
    }
    builder.append("];\n");
  }

  private void escapeString(StringBuilder builder, String text) {
    builder.append('"');
    builder.append(text);
    builder.append('"');
  }
}
