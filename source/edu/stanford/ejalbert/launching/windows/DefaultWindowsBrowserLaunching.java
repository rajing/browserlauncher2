/************************************************
    Copyright 2004 Markus Gebhard, Jeff Chapman

    This file is part of BrowserLauncher2.

    BrowserLauncher2 is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    BrowserLauncher2 is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with BrowserLauncher2; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ************************************************/
// $Id: DefaultWindowsBrowserLaunching.java,v 1.1 2005/01/06 17:07:06 jchapman0 Exp $
package edu.stanford.ejalbert.launching.windows;

import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * @author Markus Gebhard
 */
public abstract class DefaultWindowsBrowserLaunching
        extends WindowsBrowserLaunching {

    private String browser;

    protected DefaultWindowsBrowserLaunching(String browser) {
        this.browser = browser;
    }

    /**
     * The first parameter that needs to be passed into Runtime.exec() to open the default web
     * browser on Windows.
     */
    private static final String FIRST_WINDOWS_PARAMETER = "/c";

    /** The second parameter for Runtime.exec() on Windows. */
    private static final String SECOND_WINDOWS_PARAMETER = "start";

    /**
     * The third parameter for Runtime.exec() on Windows.  This is a "title"
     * parameter that the command line expects.  Setting this parameter allows
     * URLs containing spaces to work.
     */
    private static final String THIRD_WINDOWS_PARAMETER = ""; //"\"\"";

    public void openUrl(String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException {
        try {
            Process process =
                    Runtime.getRuntime().exec(
                            new String[] {
                            browser,
                            FIRST_WINDOWS_PARAMETER,
                            SECOND_WINDOWS_PARAMETER,
                            THIRD_WINDOWS_PARAMETER,
                            '"' + urlString + '"'});
            // This avoids a memory leak on some versions of Java on Windows.
            // That's hinted at in <http://developer.java.sun.com/developer/qow/archive/68/>.
            process.waitFor();
            process.exitValue();
        }
        catch (Exception e) {
            throw new BrowserLaunchingExecutionException(e);
        }
    }
}
