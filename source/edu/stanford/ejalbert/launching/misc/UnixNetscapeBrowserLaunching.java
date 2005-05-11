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
// $Id: UnixNetscapeBrowserLaunching.java,v 1.4 2005/05/11 13:38:11 jchapman0 Exp $
package edu.stanford.ejalbert.launching.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.launching.IBrowserLaunching;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import net.sf.wraplog.AbstractLogger;

/**
 * Try several browsers. Most users will have at least one of these installed.
 * @author Markus Gebhard, Jeff Chapman
 */
public class UnixNetscapeBrowserLaunching
        implements IBrowserLaunching {
    /**
     * list of commonly supported unix/linux browsers
     */
    private List unixBrowsers = new ArrayList(3);

    private final AbstractLogger logger; // in ctor

    public UnixNetscapeBrowserLaunching(AbstractLogger logger) {
        this.logger = logger;
    }

    /* ---------------------- from IBrowserLaunching ----------------------- */

    /**
     * Use the which command to find out which browsers are available.
     *
     * @todo what do we do if there are no browsers available?
     * @throws BrowserLaunchingInitializingException
     */
    public void initialize()
            throws BrowserLaunchingInitializingException {
        List potentialBrowsers = new ArrayList(3);
        potentialBrowsers.add(StandardUnixBrowser.FIREFOX);
        potentialBrowsers.add(StandardUnixBrowser.MOZILLA);
        potentialBrowsers.add(StandardUnixBrowser.NETSCAPE);
        potentialBrowsers.add(StandardUnixBrowser.KONQUEROR);
        // iterate potential browsers to see which are available
        Iterator iter = potentialBrowsers.iterator();
        // will store all names of potential browsers in case the error message should list the browsers to install
        String potentialBrowserNames = "";
        UnixBrowser browser;
        while (iter.hasNext()) {
            browser = (UnixBrowser) iter.next();
            potentialBrowserNames += browser.getBrowserName();
            if (iter.hasNext()) {
                potentialBrowserNames += ", ";
            }
            if (browser.isBrowserAvailable()) {
                unixBrowsers.add(browser);
            }
        }
        if (unixBrowsers.size() == 0) {
            // no browser installed
            throw new BrowserLaunchingInitializingException(
                    "one of the supported browsers must be installed: "
                    + potentialBrowserNames);
        }
        logger.info(unixBrowsers.toString());
        unixBrowsers = Collections.unmodifiableList(unixBrowsers);
    }

    /**
     * This implementation will cause the calling thread to block until the
     * browser exits. Calling methods MUST wrap the call in a separate thread.
     *
     * @param urlString String
     * @throws BrowserLaunchingExecutionException
     */
    public void openUrl(String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException {
        try {
            logger.info(urlString);
            boolean success = false;
            Iterator iter = unixBrowsers.iterator();
            UnixBrowser browser;
            Process process;
            while (iter.hasNext() && !success) {
                browser = (UnixBrowser) iter.next();
                logger.info(browser.getBrowserName());
                process = Runtime.getRuntime().exec(browser.
                        getArgsForOpenBrowser(urlString));
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    process = Runtime.getRuntime().exec(browser.
                            getArgsForStartingBrowser(urlString));
                    exitCode = process.waitFor();
                }
                success = exitCode == 0;
            }
        }
        catch (Exception e) {
            throw new BrowserLaunchingExecutionException(e);
        }
    }
}
