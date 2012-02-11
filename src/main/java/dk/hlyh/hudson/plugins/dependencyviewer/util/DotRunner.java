/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.hlyh.hudson.plugins.dependencyviewer.util;

import dk.hlyh.hudson.plugins.dependencyviewer.Configuration;
import hudson.Launcher;
import hudson.model.Hudson;
import hudson.util.LogTaskListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author henrik
 */
public class DotRunner {

  private static final Logger LOGGER = Logger.getLogger("dependencygraph");

  private DotRunner() {
  }

  
  /**
   * Execute the dot commando with given input and output stream
   *
   * @param type the parameter for the -T option of the graphviz tools
   */
  public static void runDot(OutputStream output, InputStream input, String type)
          throws IOException {
    Configuration.DescriptorImpl descriptor = Hudson.getInstance().getDescriptorByType(Configuration.DescriptorImpl.class);
    String dotPath = descriptor.getDotExeOrDefault();
    Launcher launcher = Hudson.getInstance().createLauncher(new LogTaskListener(LOGGER, Level.CONFIG));
    try {
      launcher.launch().cmds(dotPath, "-T" + type).stdin(input).stdout(output).start().join();
    } catch (InterruptedException e) {
      LOGGER.severe("Interrupted while waiting for dot-file to be created:" + e);
      e.printStackTrace();
    } finally {
      if (output != null) {
        output.close();
      }
    }
  }
}
