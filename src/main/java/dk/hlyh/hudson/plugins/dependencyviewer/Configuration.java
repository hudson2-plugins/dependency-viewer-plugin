/*
 * Copyright (c) 2010 Stefan Wolf
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

import hudson.Extension;
import hudson.Functions;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Class which keeps the configuration for the graphviz
 * executable.
 *
 * @author wolfs
 */
public class Configuration extends AbstractDescribableImpl<Configuration> {

    @DataBoundConstructor
    public Configuration() {
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Configuration> {

        private String dotExe;

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean configure( StaplerRequest req, JSONObject o ) {
            dotExe = Util.fixEmptyAndTrim(o.getString("dotExe"));
            save();

            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.Plugin_Title();
        }

        public String getDotExe() {
            return dotExe;
        }

        /**
         * @return configured dot executable or a default
         */
        public String getDotExeOrDefault() {
            if (Util.fixEmptyAndTrim(dotExe) == null) {
                return Functions.isWindows() ? "dot.exe" : "dot";
            } else {
                return dotExe;
            }
        }

        public synchronized void setDotExe(String dotPath) {
            this.dotExe = dotPath;
            save();
        }

        public FormValidation doCheckDotExe(@QueryParameter final String value) {
            return FormValidation.validateExecutable(value);
        }
    }
}
