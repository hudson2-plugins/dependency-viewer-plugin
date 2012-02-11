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
public abstract class Encoder {

  protected final GraphBuilder calculator;
  protected final Locale currentLocale;

  public Encoder(GraphBuilder calculator,Locale currentLocale) {    
    this.calculator = calculator;
    this.currentLocale = currentLocale;            
  }
    
  public abstract void encode(StaplerRequest req, StaplerResponse rsp) throws IOException;
}
