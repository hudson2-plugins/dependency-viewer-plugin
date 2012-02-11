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

import dk.hlyh.hudson.plugins.dependencyviewer.dependencies.GraphBuilder;
import dk.hlyh.hudson.plugins.dependencyviewer.dependencies.Node;
import dk.hlyh.hudson.plugins.dependencyviewer.encoding.Encoder;
import dk.hlyh.hudson.plugins.dependencyviewer.encoding.EncoderFactory;
import dk.hlyh.hudson.plugins.dependencyviewer.util.NodeByStartTime;
import hudson.Extension;
import hudson.model.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
public class DependencyAction implements ProminentProjectAction {

  private static final Logger LOGGER = Logger.getLogger(DependencyAction.class.toString());
  
  private Integer buildNumber;
  private String projectName;
  private View view;
  
  public DependencyAction() {
    this(null,null,null);
  }

  public DependencyAction(View view) {
    this(null,null,view);
  }

  public DependencyAction(AbstractProject project, AbstractBuild build) {
    this(project,build,null);
  }
  
  private DependencyAction(AbstractProject project, AbstractBuild build,View view) {
    this.projectName = project != null ? project.getName() : null;
    this.buildNumber = build != null ? build.getNumber() : null; 
    this.view = view;
    LOGGER.log(Level.FINE,"DependencyAction created");
  }

  @Override
  public String getIconFileName() {
    return "graph.gif";
  }

  @Override
  public String getDisplayName() {
    return Messages.Menu_Title();
  }

  @Override
  public String getUrlName() {
    return "dependency-viewer";
  }

  public AbstractModelObject getParentObject() {
    return view == null ? (AbstractModelObject) Hudson.getInstance().getItem(projectName) : view;
  }
  
  public boolean isBuildDisplay() {
    return buildNumber != null;
  }

  public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException {
    Locale currentLocale = req.getLocale();
    String path = req.getRestOfPath().substring(1);
    LOGGER.log(Level.FINE,"Path '{0}' requested",path);
    
    Encoder encoder = EncoderFactory.create(calculateNodes(), currentLocale, path);
    if (encoder != null) {
      LOGGER.log(Level.FINE,"Found encoder '{0}'",encoder);
      encoder.encode(req, rsp);
    } else {
      LOGGER.log(Level.FINE,"No encoder found, returning error");
      rsp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
    return;
  }

  public List<Node> getNodes() {
    List<Node> nodes = new ArrayList<Node>(calculateNodes().getCollectedNodes());
    Collections.sort(nodes, new NodeByStartTime());
    return nodes;
  }
  
  private GraphBuilder calculateNodes() {
    GraphBuilder calculator = new GraphBuilder();

    if (view != null) {
      Collection<AbstractProject<?, ?>> projects = new ArrayList<AbstractProject<?, ?>>();
      for (TopLevelItem item : view.getItems()) {
        if (item instanceof AbstractProject<?, ?>) {
          projects.add((AbstractProject<?, ?>) item);
        }
      }
      calculator.getProjectDependencies(projects);
      return calculator;
    }
    AbstractProject project = (AbstractProject) Hudson.getInstance().getItem(projectName);
    if (buildNumber != null) {
      AbstractBuild build = (AbstractBuild) project.getBuildByNumber(buildNumber);
      calculator.getBuildDependencies(build);
    } else {
      calculator.getProjectDependencies(project);
    }
    return calculator;
  }
}