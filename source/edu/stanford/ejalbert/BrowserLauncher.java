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
// $Id: BrowserLauncher.java,v 1.1 2005/01/06 17:07:05 jchapman0 Exp $
package edu.stanford.ejalbert;

import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import edu.stanford.ejalbert.exception.*;
import edu.stanford.ejalbert.launching.BrowserLaunchingFactory;
import edu.stanford.ejalbert.launching.IBrowserLaunching;

/**
 * BrowserLauncher is a class that provides one static method, openURL, which opens the default
 * web browser for the current user of the system to the given URL.  It may support other
 * protocols depending on the system -- mailto, ftp, etc. -- but that has not been rigorously
 * tested and is not guaranteed to work.
 * <p>
 * Yes, this is platform-specific code, and yes, it may rely on classes on certain platforms
 * that are not part of the standard JDK.  What we're trying to do, though, is to take something
 * that's frequently desirable but inherently platform-specific -- opening a default browser --
 * and allow programmers (you, for example) to do so without worrying about dropping into native
 * code or doing anything else similarly evil.
 * <p>
 * Anyway, this code is completely in Java and will run on all JDK 1.1-compliant systems without
 * modification or a need for additional libraries.  All classes that are required on certain
 * platforms to allow this to run are dynamically loaded at runtime via reflection and, if not
 * found, will not cause this to do anything other than returning an error when opening the
 * browser.
 * <p>
 * There are certain system requirements for this class, as it's running through Runtime.exec(),
 * which is Java's way of making a native system call.  Currently, this requires that a Macintosh
 * have a Finder which supports the GURL event, which is true for Mac OS 8.0 and 8.1 systems that
 * have the Internet Scripting AppleScript dictionary installed in the Scripting Additions folder
 * in the Extensions folder (which is installed by default as far as I know under Mac OS 8.0 and
 * 8.1), and for all Mac OS 8.5 and later systems.  On Windows, it only runs under Win32 systems
 * (Windows 95, 98, and NT 4.0, as well as later versions of all).  On other systems, this drops
 * back from the inherently platform-sensitive concept of a default browser and simply attempts
 * to launch Netscape via a shell command.
 * <p>
 * This code is Copyright 1999-2001 by Eric Albert (ejalbert@cs.stanford.edu) and may be
 * redistributed or modified in any form without restrictions as long as the portion of this
 * comment from this paragraph through the end of the comment is not removed.  The author
 * requests that he be notified of any application, applet, or other binary that makes use of
 * this code, but that's more out of curiosity than anything and is not required.  This software
 * includes no warranty.  The author is not repsonsible for any loss of data or functionality
 * or any adverse or unexpected effects of using this software.
 * <p>
 * Credits:
 * <br>Steven Spencer, JavaWorld magazine (<a href="http://www.javaworld.com/javaworld/javatips/jw-javatip66.html">Java Tip 66</a>)
 * <br>Thanks also to Ron B. Yeh, Eric Shapiro, Ben Engber, Paul Teitlebaum, Andrea Cantatore,
 * Larry Barowski, Trevor Bedzek, Frank Miedrich, and Ron Rabakukk
 *
 * @author Eric Albert (<a href="mailto:ejalbert@cs.stanford.edu">ejalbert@cs.stanford.edu</a>)
 * @version 1.4b1 (Released June 20, 2001)
 */
public class BrowserLauncher {

    /**
     * The Java virtual machine that we are running on.  Actually, in most cases we only care
     * about the operating system, but some operating systems require us to switch on the VM. */
    private static IBrowserLaunching launching;


    /**
     * The message from any exception thrown throughout the initialization process.
     */
    private static UnsupportedOperatingSystemException uosError;
    private static BrowserLaunchingInitializingException bliError;

    /**
     * An initialization block that determines the operating system and loads the necessary
     * runtime data.
     */
    static {
        try {
            launching = BrowserLaunchingFactory.createSystemBrowserLaunching();
            launching.initialize();
        } catch (UnsupportedOperatingSystemException e) {
            uosError = e;
        } catch (BrowserLaunchingInitializingException bliex) {
            bliError = bliex;
        }
    }

    /**
     * This class should be never be instantiated; this just ensures so.
     */
    private BrowserLauncher() {
    }

    /**
     * Attempts to open the default web browser to the given URL.
     * @todo what if the url is null or empty?
     * @param url The URL to open
     * @throws IOException If the web browser could not be located or does not run
     */
    public static void openURL(String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException {
        if (uosError != null) {
            throw uosError;
        }
        if(bliError != null) {
            throw bliError;
        }
        launching.openUrl(urlString);
    }

    /**
     * @todo populate main method; provide usage info and take arg[0] as the
     * url value
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        if(args.length == 0) {
            System.err.println("Usage: java -jar BrowserLauncher.jar url_value");
        }
        else {
            try {
                launching.openUrl(args[0]);
            }
            catch (BrowserLaunchingInitializingException ex) {
                ex.printStackTrace();
            }
            catch (BrowserLaunchingExecutionException ex) {
                ex.printStackTrace();
            }
            catch (UnsupportedOperatingSystemException ex) {
                ex.printStackTrace();
            }
        }
    }
}
