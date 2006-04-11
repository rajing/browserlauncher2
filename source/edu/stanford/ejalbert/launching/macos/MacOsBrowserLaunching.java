/************************************************
    Copyright 2004,2006 Markus Gebhard, Jeff Chapman

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
// $Id: MacOsBrowserLaunching.java,v 1.2 2006/04/11 13:36:48 jchapman0 Exp $
package edu.stanford.ejalbert.launching.macos;

import java.util.List;

import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import edu.stanford.ejalbert.launching.IBrowserLaunching;

/**
 * @author Markus Gebhard
 */
public abstract class MacOsBrowserLaunching
        implements IBrowserLaunching {

    /**
     * The creator code of the Finder on a Macintosh, which is needed to send AppleEvents to the
     * application.
     */
    protected static final String FINDER_CREATOR = "MACS";

    /* ---------------- from IBrowserLaunching ---------------- */

    /**
     * Falls through to non-targetted openUrl method. Browser
     * targetting has not been implemented for the Mac.
     *
     * @param browser String
     * @param urlString String
     * @throws UnsupportedOperatingSystemException
     * @throws BrowserLaunchingExecutionException
     * @throws BrowserLaunchingInitializingException
     */
    public void openUrl(String browser,
                        String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException {
        openUrl(urlString);
    }

    /**
     * Falls through to non-targetted openUrl method. Browser
     * targetting has not been implemented for the Mac.
     *
     * @param browsers List
     * @param urlString String
     * @throws UnsupportedOperatingSystemException
     * @throws BrowserLaunchingExecutionException
     * @throws BrowserLaunchingInitializingException
     */
    public void openUrl(List browsers,
                        String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException {
        openUrl(urlString);
    }
}
