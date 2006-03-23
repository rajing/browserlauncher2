/************************************************
    Copyright 2005,2006 Jeff Chapman

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
// $Id: WindowsBrowser.java,v 1.3 2006/03/23 20:54:32 jchapman0 Exp $
package edu.stanford.ejalbert.launching.windows;

import edu.stanford.ejalbert.launching.BrowserDescription;

/**
 * Encapsulates information on a Windows browser.
 *
 * @author Jeff Chapman
 * @version 1.0
 */
public class WindowsBrowser
        implements BrowserDescription {
    /**
     * The name for the browser suitable for display to a user.
     */
    private final String displayName; // in ctor
    /**
     * The name of the executable for the browser.
     */
    private final String exe; // in ctor

    /**
     * Splits the config string using the delimiter character and
     * sets the display name and executable.
     * <p>
     * Sample config string (with ; as the delim char): displayName;exeName
     *
     * @param delimChar String
     * @param configInfo String
     */
    WindowsBrowser(String delimChar, String configInfo) {
        String[] configItems = configInfo.split(delimChar);
        this.displayName = configItems[0];
        this.exe = configItems[1];
    }

    /**
     * Returns displayname and executable name for debugging.
     *
     * @return String
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(displayName);
        buf.append(' ');
        buf.append(exe);
        return buf.toString();
    }

    /* -------------------- from BrowserDescription ---------------------- */

    /**
     * Returns the display name for the browser.
     *
     * @return String
     */
    public String getBrowserDisplayName() {
        return displayName;
    }

    /**
     * Returns the name of the executable for the browser.
     *
     * @return String
     */
    public String getBrowserApplicationName() {
        return exe;
    }
}
