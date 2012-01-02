/*
 * Copyright (c) 2010 Stefan Wolf
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
import hudson.model.AbstractModelObject;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TopLevelItem;
import hudson.model.View;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Basic action for creating a Dot-Image of the DependencyGraph
 *
 * @author Henrik Lynggaard
 */
public class ViewGraphAction implements Action {

  private static final Logger LOGGER = Logger.getLogger(Logger.class.getName());
  private View view;

  public ViewGraphAction(View view) {
    this.view = view;
  }
 
  /**
   * graph.{png,gv,...} is mapped to the corresponding output
   */
  public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException {
    GraphBuilder calculator = new GraphBuilder();
    GraphvizFormatter executor = new GraphvizFormatter(req.getLocale());
    Collection<? extends AbstractProject<?, ?>> projectsForDepgraph = getProjectsForDepgraph();
    String path = req.getRestOfPath();
    
    if (path.startsWith("/graph.")) {
      String extension = path.substring("/graph.".length());
      SupportedImageType imageType = SupportedImageType.supportedTypes.get(extension.toLowerCase());

      if (imageType != null) {
        calculator.getProjectDependencies(projectsForDepgraph);
        List<Node> nodes = new ArrayList<Node>(calculator.getCollectedNodes());
        String graphDot = executor.generateDotText(calculator.getCollectedNodes(),calculator.getCollectedLinks());
        rsp.setContentType(imageType.contentType);
        if ("gv".equalsIgnoreCase(extension)) {
          rsp.getWriter().append(graphDot).close();
        } else {
          executor.runDot(rsp.getOutputStream(), new ByteArrayInputStream(graphDot.getBytes()), imageType.dotType);
        }
      }
    } else {
      rsp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
  }

  /**
   * Execute the dot commando with given input and output stream
   *
   * @param type the parameter for the -T option of the graphviz tools
   */
  /**
   * @return projects for which the dependency graph should be calculated
   */
  protected Collection<? extends AbstractProject<?, ?>> getProjectsForDepgraph() {
    Collection<TopLevelItem> items = view.getItems();
    Collection<AbstractProject<?, ?>> projects = new ArrayList<AbstractProject<?, ?>>();
    for (TopLevelItem item : items) {
      if (item instanceof AbstractProject<?, ?>) {
        projects.add((AbstractProject<?, ?>) item);
      }
    }
    return projects;
  }

 
  /**
   * @return title of the dependency graph page
   */

  public String getTitle() {
    return Messages.AbstractDependencyGraphAction_DependencyGraphOf(view.getDisplayName());
  }

  /**
   * @return object for which the sidepanel.jelly will be shown
   */
  public AbstractModelObject getParentObject() {
    return view;
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
