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
package dk.hlyh.hudson.plugins.dependencyviewer;

import dk.hlyh.hudson.plugins.dependencyviewer.impl.GraphBuilder;
import dk.hlyh.hudson.plugins.dependencyviewer.impl.GraphvizFormatter;
import dk.hlyh.hudson.plugins.dependencyviewer.impl.Node;
import dk.hlyh.hudson.plugins.dependencyviewer.impl.SupportedImageType;
import dk.hlyh.hudson.plugins.dependencyviewer.impl.sorter.NodeByStartTime;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractModelObject;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.ProminentProjectAction;
import hudson.util.TimeUnit2;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
public class ProjectGraphAction implements ProminentProjectAction {

  private Integer buildNumber;
  private String projectName;

  public ProjectGraphAction() {
  }

  public ProjectGraphAction(AbstractProject project, AbstractBuild build) {
    this.projectName = project != null ? project.getName() : null;
    this.buildNumber = build != null ? build.getNumber() : null;
  }

  public AbstractModelObject getParentObject() {
    return (AbstractModelObject) Hudson.getInstance().getItem(projectName);
  }

  public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException {
    GraphvizFormatter executor = new GraphvizFormatter(req.getLocale());
    String path = req.getRestOfPath();
    if (path.startsWith("/graph.")) {
      String extension = path.substring("/graph.".length());
      SupportedImageType imageType = SupportedImageType.supportedTypes.get(extension.toLowerCase());

      if (imageType != null) {
        GraphBuilder calculator = calculateNodes();
        String graphDot = executor.generateDotText(calculator.getCollectedNodes(), calculator.getCollectedLinks());
        rsp.setContentType(imageType.contentType);
        if ("gv".equalsIgnoreCase(extension)) {
          rsp.getWriter().append(graphDot).close();
        } else {
          executor.runDot(rsp.getOutputStream(), new ByteArrayInputStream(graphDot.getBytes()), imageType.dotType);
        }
      }
      return;
    }
    if (path.endsWith("events.xml")) {
      StringBuilder builder = new StringBuilder(1024);
      builder.append("<data>");
      SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy HH:mm:ss ZZ");
      long minute = TimeUnit.MINUTES.toMillis(1);
      for (Node node : getNodes()) {
        if (node.getBuildStart() != null) {                    
          boolean duration = node.getDurationMillis() > minute;
          builder.append("<event ");
          builder.append(" start=\"").append(formatter.format(node.getBuildStart())).append("\" ");
          builder.append(" latestStart=\"").append(formatter.format(node.getBuildStart())).append("\" ");
          builder.append(" end=\"").append(formatter.format(node.getBuildEnd())).append("\" ");
          builder.append(" earliestEnd=\"").append(formatter.format(node.getBuildEnd())).append("\" ");
          builder.append(" title=\"").append(node.getName()).append("\" ");
          builder.append(" durationEvent=\"").append(duration).append("\" ");
          builder.append(" />\n");
        }
      }
      builder.append("</data>");
      rsp.setContentType("application/xml");
      rsp.getWriter().append(builder.toString()).close();
      return;
    }

    rsp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
  }

  public List<Node> getNodes() {
    List<Node> nodes = new ArrayList<Node>(calculateNodes().getCollectedNodes());
    Collections.sort(nodes, new NodeByStartTime());
    return nodes;
  }

  private GraphBuilder calculateNodes() {

    GraphBuilder calculator = new GraphBuilder();
    AbstractProject project = (AbstractProject) Hudson.getInstance().getItem(projectName);
    if (buildNumber != null) {
      AbstractBuild build = (AbstractBuild) project.getBuildByNumber(buildNumber);
      calculator.getBuildDependencies(build);
    } else {
      calculator.getProjectDependencies(project);
    }
    return calculator;
  }

  public String getIconFileName() {
    return "graph.gif";
  }

  public String getDisplayName() {
    return Messages.Menu_Title();
  }

  public String getUrlName() {
    return "dependency-viewer";
  }
}
