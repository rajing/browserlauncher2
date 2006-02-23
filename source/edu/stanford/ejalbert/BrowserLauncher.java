/************************************************
    Copyright 2004,2005,2006 Markus Gebhard, Jeff Chapman

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
// $Id: BrowserLauncher.java,v 1.9 2006/02/23 18:50:47 jchapman0 Exp $
package edu.stanford.ejalbert;

import java.util.List;

import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import edu.stanford.ejalbert.launching.BrowserLaunchingFactory;
import edu.stanford.ejalbert.launching.IBrowserLaunching;
import net.sf.wraplog.AbstractLogger;
import net.sf.wraplog.Level;
import net.sf.wraplog.NoneLogger;
import edu.stanford.ejalbert.exceptionhandler.BrowserLauncherErrorHandler;
import edu.stanford.ejalbert.exceptionhandler.
        BrowserLauncherDefaultErrorHandler;

/**
 * BrowserLauncher is a class that provides a method, openURLinBrowser, which opens the default
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
 * @version 1.0rc1
 */
public class BrowserLauncher {

    private final IBrowserLaunching launching; // in ctor
    private AbstractLogger logger; // in init method
    private BrowserLauncherErrorHandler errorHandler; // in ctor

    /**
     * Initializes the browser launcher for the operating system on which
     * the application is running.
     * <p>
     * This method will use the default logger
     * {@link net.sf.wraplog.NoneLogger NoneLogger}. All log messages are
     * ignored by this logger.
     * <p>
     * This method will use the default errorHandler
     * {@link edu.stanford.ejalbert.exceptionhandler.BrowserLauncherDefaultErrorHandler BrowserLauncherDefaultErrorHandler}.
     * It will print a stack trace to the console. The errorHandler is used
     * to catch and handle exceptions when executing the browser
     * launch in a separate thread.
     *
     * @throws BrowserLaunchingInitializingException
     * @throws UnsupportedOperatingSystemException
     */
    public BrowserLauncher()
            throws BrowserLaunchingInitializingException,
            UnsupportedOperatingSystemException {
        this(null, null);
    }

    /**
     * Initializes the browser launcher for the operating system on which
     * the application is running.
     * <p>
     * If null is passed in as a logger, the default logger used will
     * be {@link net.sf.wraplog.NoneLogger NoneLogger}. All log messages are
     * ignored by this logger.
     * <p>
     * This method will use the default errorHandler
     * {@link edu.stanford.ejalbert.exceptionhandler.BrowserLauncherDefaultErrorHandler BrowserLauncherDefaultErrorHandler}.
     * It will print a stack trace to the console. The errorHandler is used
     * to catch and handle exceptions when executing the browser
     * launch in a separate thread.

     * @param logger AbstractLogger
     * @throws BrowserLaunchingInitializingException
     * @throws UnsupportedOperatingSystemException
     */
    public BrowserLauncher(AbstractLogger logger)
            throws BrowserLaunchingInitializingException,
            UnsupportedOperatingSystemException {
        this(logger, null);
    }

    /**
     * Initializes the browser launcher for the operating system on which
     * the application is running.
     * <p>
     * If null is passed in as a logger, the default logger used will
     * be {@link net.sf.wraplog.NoneLogger NoneLogger}. All log messages are
     * ignored by this logger.
     * <p>
     * If null is passed for the errorHandler, the default errorHandler
     * used will be
     * {@link edu.stanford.ejalbert.exceptionhandler.BrowserLauncherDefaultErrorHandler BrowserLauncherDefaultErrorHandler}.
     * It will print a stack trace to the console. The errorHandler is used
     * to catch and handle exceptions when executing the browser
     * launch in a separate thread.
     *
     * @param logger AbstractLogger
     * @param errorHandler BrowserLauncherErrorHandler
     * @throws BrowserLaunchingInitializingException
     * @throws UnsupportedOperatingSystemException
     */
    public BrowserLauncher(AbstractLogger logger,
                           BrowserLauncherErrorHandler errorHandler)
            throws BrowserLaunchingInitializingException,
            UnsupportedOperatingSystemException {
        // assign logger or use default
        if (logger == null) {
            logger = new NoneLogger();
        }
        this.logger = logger;
        // assign error handler or use default
        if (errorHandler == null) {
            errorHandler = new BrowserLauncherDefaultErrorHandler();
        }
        this.errorHandler = errorHandler;
        // init and assign IBrowserLaunching instance
        // this method assumes the logger is not null
        this.launching = initBrowserLauncher();
    }

    /**
     * Returns the logger being used by this BrowserLauncher instance.
     *
     * @return AbstractLogger
     */
    public AbstractLogger getLogger() {
        return logger;
    }

    /**
     * Returns a list of browsers to be used for browser targetting.
     * This list will always contain at least one item:
     * {@link edu.stanford.ejalbert.launching.IBrowserLaunching#BROWSER_DEFAULT BROWSER_DEFAULT}.
     * @see IBrowserLaunching
     * @return List
     */
    public List getBrowserList() {
        return launching.getBrowserList();
    }

    /**
     * Determines the operating system and loads the necessary runtime data.
     * <p>
     * If null is passed in as a logger, the default logger used will
     * be {@link net.sf.wraplog.NoneLogger NoneLogger}. All log messages are
     * ignored by this logger.
     *
     * @param logger AbstractLogger
     * @return IBrowserLaunching
     * @throws UnsupportedOperatingSystemException
     * @throws BrowserLaunchingInitializingException
     */
    private IBrowserLaunching initBrowserLauncher()
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingInitializingException {
        if (logger == null) {
            throw new IllegalArgumentException(
                    "the logger cannot be null at this point.");
        }
        IBrowserLaunching launching =
                BrowserLaunchingFactory.createSystemBrowserLaunching(logger);
        launching.initialize();
        return launching;
    }

    /**
     * Attempts to open a browser and direct it to the passed url.
     *
     * @todo what to do if the url is null or empty?
     * @param urlString String
     */
    public void openURLinBrowser(String urlString) {
        Runnable runner = new BrowserLauncherRunner(
                launching,
                urlString,
                logger,
                errorHandler);
        Thread launcherThread = new Thread(runner);
        launcherThread.start();
    }

    /**
     * Attempts to open a specific browser and direct it to the passed url. If
     * the call to the requested browser fails, the code will fail over to the
     * default browser.
     * <p>
     * The name for the targetted browser should come from the list
     * returned from {@link #getBrowserList() getBrowserList}.
     *
     * @param browser String
     * @param urlString String
     */
    public void openURLinBrowser(String browser,
                                 String urlString) {
        Runnable runner = new BrowserLauncherRunner(
                launching,
                browser,
                urlString,
                logger,
                errorHandler);
        Thread launcherThread = new Thread(runner);
        launcherThread.start();
    }

    /**
     * Attempts to open the default web browser to the given URL.
     * @deprecated -- create a BrowserLauncher object and use it instead of
     *                calling this static method.
     * @param urlString The URL to open
     * @throws UnsupportedOperatingSystemException
     * @throws BrowserLaunchingExecutionException
     * @throws BrowserLaunchingInitializingException
     */
    public static void openURL(String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException {
        BrowserLauncher launcher = new BrowserLauncher(null);
        launcher.openURLinBrowser(urlString);
    }

    /**
     * Opens a browser and url from the command line. Useful for testing.
     * The first argument is the url to be opened. All other arguments will
     * be ignored.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java -jar BrowserLauncher.jar url_value");
        }
        else {
            try {
                BrowserLauncher launcher = new BrowserLauncher(null);
                launcher.openURLinBrowser(args[0]);
            }
            catch (BrowserLaunchingInitializingException ex) {
                ex.printStackTrace();
            }
            //catch (BrowserLaunchingExecutionException ex) {
            //    ex.printStackTrace();
           // }
            catch (UnsupportedOperatingSystemException ex) {
                ex.printStackTrace();
            }
        }
    }
}
