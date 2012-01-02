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

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.DependencyGraph;
import hudson.model.DependencyGraph.Dependency;
import hudson.model.Fingerprint;
import hudson.model.Fingerprint.RangeSet;
import hudson.model.Hudson;
import hudson.security.Permission;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import org.acegisecurity.providers.dao.UserCache;

/**
 *
 * @author Henrik Lynggaard
 */
public class GraphBuilder {

  private static final Logger LOGGER = Logger.getLogger("dependency-viewer");
  private AbstractBuild topBuild;
  private final Set<AbstractBuild> visitedBuilds = new HashSet<AbstractBuild>();
  private final Set<AbstractProject> visitedProjects = new HashSet<AbstractProject>();
  private final Set<Node> collectedNodes = new HashSet<Node>();
  private final Set<Link> collectedLinks = new HashSet<Link>();
  private final DependencyGraph dependencyGraph = Hudson.getInstance().getDependencyGraph();


  public void getProjectDependencies(AbstractProject<?, ?> project) {
    clearCaches();
    visitProject(project);
  }

  public void getProjectDependencies(Collection<? extends AbstractProject<?, ?>> projects) {
    clearCaches();
    for (AbstractProject<?, ?> project : projects) {
      if (!visitedProjects.contains(project)) {
        visitProject(project);
      }
    }
  }

  public void getBuildDependencies(AbstractBuild<?, ?> build) {
    clearCaches();
    topBuild = build;
    visitBuild(build);
  }

  public Set<Node> getCollectedNodes() {
    return collectedNodes;
  }

  public Set<Link> getCollectedLinks() {
    return collectedLinks;
  }

  /**
   * Clear the caches in preparation for a new calculation.
   */
  private void clearCaches() {
    visitedBuilds.clear();
    visitedProjects.clear();
    collectedNodes.clear();
    collectedLinks.clear();
    topBuild = null;
  }

  private Node visitBuild(AbstractBuild<?, ?> build) {
    Set<AbstractProject> builtProjects = new HashSet<AbstractProject>();

    // create basic node
    Node buildNode = createBuildNode(build);
    collectedNodes.add(buildNode);
    visitedBuilds.add(build);
    visitedProjects.add(build.getProject());
    
    // find downstream builds
    for (Entry<AbstractProject, Fingerprint.RangeSet> entry : build.getDownstreamBuilds().entrySet()) {

      AbstractProject<?, ?> childProject = entry.getKey();
      Fingerprint.RangeSet rangeSet = entry.getValue();

      // make sure user is allowed to read the project
      if (childProject.hasPermission(Permission.READ) && !rangeSet.isEmpty()) {
        builtProjects.add(childProject);        
        visitRangeSet(rangeSet, buildNode, childProject);
      }
    }
    // find indirect downstream builds
    for (Dependency dependency : dependencyGraph.getDownstreamDependencies(build.getProject())) {
      AbstractProject downstreamProject = dependency.getDownstreamProject();

      if (downstreamProject.hasPermission(Permission.READ)) {
        // get ranges of any builds that match the topBuild
        RangeSet downstreamRangeSet = topBuild.getDownstreamRelationship(downstreamProject);
        if (!downstreamRangeSet.isEmpty()) {
          builtProjects.add(downstreamProject);
          visitRangeSet(downstreamRangeSet, buildNode, downstreamProject);
        }

        // get planned projects
        if (!builtProjects.contains(downstreamProject) || downstreamProject.isBuilding() || downstreamProject.isInQueue()) {
          Node downstreamNode = visitProject(downstreamProject);
          createLink(buildNode.getName(), downstreamNode.getName());
        } else {
          // blank
        }
      }
    }
    return buildNode;
  }

  private void visitRangeSet(RangeSet rangeSet, Node parentNode, AbstractProject downstreamProject) {
    // lookup parent
    AbstractProject<?, ?> upstreamProject = (AbstractProject<?, ?>) Hudson.getInstance().getItem(parentNode.getProjectName());
    AbstractBuild<?, ?> upstreamBuild = upstreamProject.getBuildByNumber(parentNode.getBuildNumber());

    for (Integer buildNumber : rangeSet.listNumbers()) {
      AbstractBuild<?, ?> downstreamBuild = (AbstractBuild<?, ?>) downstreamProject.getBuildByNumber(buildNumber);
      if (downstreamBuild != null) {
        Node childNode = visitBuild(downstreamBuild);
        Link createLink = null;
        AbstractBuild<?, ?> nextUpstreamBuild = upstreamBuild.getNextBuild();
        if (upstreamBuild.getTimeInMillis() < downstreamBuild.getTimeInMillis() && nextUpstreamBuild != null
                && downstreamBuild.getTimeInMillis() < nextUpstreamBuild.getTimeInMillis()) {
          createLink = createLink(parentNode.getName(), childNode.getName());
        }
        if (upstreamBuild.getTimeInMillis() < downstreamBuild.getTimeInMillis() && nextUpstreamBuild == null) {
          createLink = createLink(parentNode.getName(), childNode.getName());          
        }
        if (createLink != null) {
          for (Cause cause : downstreamBuild.getCauses()) {
            if (cause instanceof Cause.UpstreamCause) {
              createLink.setCause("upstream");
            }
            if (cause instanceof Cause.UserCause) {
              Cause.UserCause userCause = (Cause.UserCause) cause;
              createLink.setCause("Manual ("+userCause.getUserName()+")");
            }
          }
        }
      }
    }

  }

  private Node visitProject(AbstractProject<?, ?> project) {
    Node projectNode = createProjectNode(project);
    collectedNodes.add(projectNode);
    visitedProjects.add(project);


    for (Dependency dependency : dependencyGraph.getUpstreamDependencies(project)) {
      AbstractProject upstreamProject = dependency.getUpstreamProject();
      if (!visitedProjects.contains(upstreamProject) && upstreamProject.hasPermission(Permission.READ)) {
        Node upstreamNode = visitProject(upstreamProject);
        createLink(upstreamProject.getName(), project.getName());
      }

    }
    for (Dependency dependency : dependencyGraph.getDownstreamDependencies(project)) {
      AbstractProject downstreamProject = dependency.getDownstreamProject();
      if (!visitedProjects.contains(downstreamProject) && downstreamProject.hasPermission(Permission.READ)) {
        Node downstreamNode = visitProject(downstreamProject);
      }
      createLink(project.getName(), downstreamProject.getName());
    }
    return projectNode;
  }

  private Link createLink(String upstream, String downstream) {
    Link link = new Link(upstream, downstream);
    collectedLinks.add(link);
    return link;
  }

  private Node createProjectNode(AbstractProject project) {
    Node node = new Node();
    node.setName(project.getName());
    node.setType(Node.Type.Project);
    node.setUrl(Hudson.getInstance().getRootUrlFromRequest() + project.getUrl());
    node.setStatus(Node.Status.Planned);
    // status
    if (project.isInQueue()) {
      node.setStatus(Node.Status.Queue);
    }
    if (project.isBuilding()) {
      node.setStatus(Node.Status.Building);
    }

    if (project.getLastSuccessfulBuild() != null) {
      node.setDuration(project.getLastSuccessfulBuild().getDurationString());
    }
    return node;
  }

  private Node createBuildNode(AbstractBuild<?, ?> build) {
    Node node = new Node();
    node.setName(build.getFullName());
    node.setType(Node.Type.Build);
    node.setUrl(Hudson.getInstance().getRootUrlFromRequest() + build.getUrl());
    node.setProjectName(build.getProject().getName());
    node.setBuildNumber(build.getNumber());
    node.setResult(build.getResult());
    node.setDuration(build.getDurationString());
    node.setBuildStart(new Date(build.getTimeInMillis()));
    node.setBuildEnd(new Date(build.getTimeInMillis()+build.getDuration()));
    node.setDurationMillis(build.getDuration());

    
    return node;
  }
}
