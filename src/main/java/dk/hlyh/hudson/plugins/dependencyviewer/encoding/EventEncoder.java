/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.hlyh.hudson.plugins.dependencyviewer.encoding;

import dk.hlyh.hudson.plugins.dependencyviewer.dependencies.GraphBuilder;
import dk.hlyh.hudson.plugins.dependencyviewer.dependencies.Node;
import dk.hlyh.hudson.plugins.dependencyviewer.util.NodeByStartTime;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author henrik
 */
class EventEncoder extends Encoder{

  EventEncoder(GraphBuilder calculator, Locale currentLocale) {
    super(calculator,currentLocale);
  }

  public void encode(StaplerRequest req, StaplerResponse rsp) throws IOException {
      StringBuilder builder = new StringBuilder(1024);
      builder.append("<data>");
      SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy HH:mm:ss ZZ");
      long minute = TimeUnit.MINUTES.toMillis(1);
      List<Node> nodes = new ArrayList<Node>(calculator.getCollectedNodes());
      Collections.sort(nodes, new NodeByStartTime());      
      for (Node node : nodes) {
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
  }
    
}
