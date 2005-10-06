/************************************************
    Copyright 2004,2005 Jeff Chapman

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
// $Id: StandardUnixBrowser.java,v 1.3 2005/10/06 15:14:48 jchapman0 Exp $
package edu.stanford.ejalbert.launching.misc;

import java.io.IOException;

class StandardUnixBrowser implements UnixBrowser {
    /**
     * name of browser for user display
     */
    private String browserName; // in ctor
    /**
     * name of browser used to invoke it
     */
    private String browserArgName; // in ctor
    /**
     * The shell parameters for Netscape that opens a given URL in an already-open copy of Netscape
     * on many command-line systems.
     */
    private static final String REMOTE_PARAMETER = "-remote";
    private static final String OPEN_PARAMETER_START = "openURL(";
    private static final String OPEN_PARAMETER_END = ")";

    static StandardUnixBrowser NETSCAPE = new StandardUnixBrowser("Netscape",
            "netscape");
    static StandardUnixBrowser MOZILLA = new StandardUnixBrowser("Mozilla",
            "mozilla");
    static StandardUnixBrowser FIREFOX = new StandardUnixBrowser("FireFox",
            "firefox");
    // on some systems, firefox is referenced as mozilla-firefox
    static StandardUnixBrowser MOZILLA_FIREFOX = new StandardUnixBrowser("FireFox",
            "mozilla-firefox");
    static StandardUnixBrowser KONQUEROR = new StandardUnixBrowser("Konqueror",
            "konqueror");
    private StandardUnixBrowser(String browserName, String browserArgName) {
        this.browserArgName = browserArgName;
        this.browserName = browserName;
    }

    public String toString() {
        return browserName;
    }

    /* ------------------------- from UnixBrowser ------------------------ */

    public String getBrowserName() {
        return browserName;
    }

    public String[] getArgsForOpenBrowser(String urlString) {
        return new String[] {
                browserArgName,
                REMOTE_PARAMETER,
                OPEN_PARAMETER_START + urlString + OPEN_PARAMETER_END};
    }

    public String[] getArgsForStartingBrowser(String urlString) {
        return new String[] {browserArgName, urlString};
    }

    /**
     * Returns true if the browser is available, ie which command finds it.
     *
     * @todo what do we do if an exception is thrown? log it or ignore it?
     * @return boolean
     */
    public boolean isBrowserAvailable() {
        boolean isAvailable = false;
        try {
            Process process = Runtime.getRuntime().exec(new String[] {"which",
                    browserArgName});
            int exitCode = process.waitFor();
            isAvailable = exitCode == 0;
        } catch (IOException ex) {
            // log this somewhere?
        } catch (InterruptedException ex) {
            // log this somewhere?
        }
        return isAvailable;
    }
}
