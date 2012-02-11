/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.hlyh.hudson.plugins.dependencyviewer.encoding;

import dk.hlyh.hudson.plugins.dependencyviewer.dependencies.GraphBuilder;
import java.io.IOException;
import java.util.Locale;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author henrik
 */
class GraphvizSourceEncoder extends Encoder {
  
  GraphvizSourceEncoder(GraphBuilder calculator, Locale currentLocale) {
    super(calculator,currentLocale);
  }


  public void encode(StaplerRequest req, StaplerResponse rsp) throws IOException {
    GraphvizFormatter executor = new GraphvizFormatter(currentLocale);
    String graphDot = executor.generateDotText(calculator.getCollectedNodes(), calculator.getCollectedLinks());    
    rsp.setContentType("text/plain");
    rsp.getWriter().append(graphDot).close();
  }    
}
