/************************************************
    Copyright 2004,2005 Markus Gebhard, Jeff Chapman

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
// $Id: DefaultWindowsBrowserLaunching.java,v 1.6 2005/12/14 16:08:54 jchapman0 Exp $
package edu.stanford.ejalbert.launching.windows;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import edu.stanford.ejalbert.launching.IBrowserLaunching;
import net.sf.wraplog.AbstractLogger;

/**
 * @author Markus Gebhard
 */
abstract class DefaultWindowsBrowserLaunching
        extends WindowsBrowserLaunching {
    private final List browserList = new ArrayList();

    protected DefaultWindowsBrowserLaunching(AbstractLogger logger) {
        super(logger);
        // creating a static list here until we can find a method
        // to dynamically obtain a browser list
        browserList.add(IBrowserLaunching.BROWSER_DEFAULT);
        browserList.add("firefox");
        browserList.add("iexplore");
        browserList.add("mozilla");
    }

    /**
     * The first parameter that needs to be passed into Runtime.exec() to open the default web
     * browser on Windows.
     */
    protected static final String FIRST_WINDOWS_PARAMETER = "/c";

    /** The second parameter for Runtime.exec() on Windows. */
    protected static final String SECOND_WINDOWS_PARAMETER = "start";

    /**
     * The third parameter for Runtime.exec() on Windows.  This is a "title"
     * parameter that the command line expects.  Setting this parameter allows
     * URLs containing spaces to work.
     */
    protected static final String THIRD_WINDOWS_PARAMETER = "\"\"";

    public void openUrl(String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException {
        try {
            logger.info(urlString);
            String protocol = getProtocol(urlString);
            logger.info(protocol);
            String[] args = getCommandArgs(protocol,
                                           urlString);
            if (logger.isDebugEnabled()) {
                logger.debug(getArrayAsString(args));
            }
            Process process = Runtime.getRuntime().exec(args);
            // This avoids a memory leak on some versions of Java on Windows.
            // That's hinted at in <http://developer.java.sun.com/developer/qow/archive/68/>.
            process.waitFor();
            process.exitValue();
        }
        catch (Exception e) {
            logger.error("fatal exception", e);
            throw new BrowserLaunchingExecutionException(e);
        }
    }

    public void openUrl(String browser, String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException {
        if (IBrowserLaunching.BROWSER_DEFAULT.equals(browser) ||
            browser == null) {
            logger.info("default or null browser target");
            openUrl(urlString);
        }
        else {
            boolean successfullLaunch = false;
            try {
                logger.info(urlString);
                String protocol = getProtocol(urlString);
                logger.info(protocol);
                String[] args = getCommandArgs(protocol,
                                               browser,
                                               urlString);
                if (logger.isDebugEnabled()) {
                    logger.debug(getArrayAsString(args));
                }
                Process process = Runtime.getRuntime().exec(args);
                // This avoids a memory leak on some versions of Java on Windows.
                // That's hinted at in <http://developer.java.sun.com/developer/qow/archive/68/>.
                process.waitFor();
                successfullLaunch = process.exitValue() == 0;
            }
            catch (Exception e) {
                logger.error("fatal exception", e);
                successfullLaunch = false;
            }
            if (!successfullLaunch) {
                logger.debug("falling through to non-targetted openUrl");
                openUrl(urlString);
            }
        }
    }

    /**
     * Returns a list of browsers to be used for browser targetting.
     * This list will always contain at least one item--the BROWSER_DEFAULT.
     *
     * @return List
     */
    public List getBrowserList() {
        return browserList;
    }

    /**
     * Returns the windows arguments for launching a default browser.
     *
     * @param protocol String
     * @param urlString String
     * @return String[]
     */
    protected abstract String[] getCommandArgs(String protocol,
                                               String urlString);

    /**
     * Returns the windows arguments for launching a specified browser.
     *
     * @param protocol String
     * @param browserName String
     * @param urlString String
     * @return String[]
     */
    protected abstract String[] getCommandArgs(String protocol,
                                               String browserName,
                                               String urlString);
}
