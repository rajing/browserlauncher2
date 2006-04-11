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
// $Id: WindowsBrowserLaunching.java,v 1.5 2006/04/11 13:36:48 jchapman0 Exp $
package edu.stanford.ejalbert.launching.windows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import edu.stanford.ejalbert.launching.IBrowserLaunching;
import net.sf.wraplog.AbstractLogger;

/**
 * Handles initialization, configuration, and calls to open a url.
 *
 * @author Markus Gebhard, Jeff Chapman, Chris Dance
 */
public class WindowsBrowserLaunching
        implements IBrowserLaunching {
    /**
     * windows configuration file -- info on commands and browsers
     */
    private static final String CONFIGFILE_WINDOWS =
            "/edu/stanford/ejalbert/launching/windows/windowsConfig.properties";
    /**
     * config file key for Windows 2000
     */
    public static final String WINKEY_WIN2000 = "windows.win2000";
    /**
     * config file key for Windows 9x
     */
    public static final String WINKEY_WIN9X = "windows.win9x";
    /**
     * config file key for Windows NT
     */
    public static final String WINKEY_WINNT = "windows.winNT";
    /**
     * collects valid config keys for key validation
     */
    private static final String[] WIN_KEYS = {
                                             WINKEY_WIN2000,
                                             WINKEY_WIN9X,
                                             WINKEY_WINNT};
    static {
        Arrays.sort(WIN_KEYS);
    }

    protected final AbstractLogger logger; // in ctor

    /**
     * Maps display name and exe name to {@link WindowsBrowser WindowsBrowser}
     * objects. Using name and exe as keys for backward compatiblity.
     */
    private Map browserNameAndExeMap = null;

    /**
     * List of {@link WindowsBrowser WindowsBrowser} objects that
     * will be used to determine which browsers are available
     * on the machine. The list is created from the windows
     * config file.
     */
    private List browsersToCheck = new ArrayList();

    /**
     * Arguments for starting the default browser.
     */
    private String commandsDefaultBrowser; // in initialize
    /**
     * Arguments for starting a specific browser.
     */
    private String commandsTargettedBrowser; // in initialize
    /**
     * The key for accessing information from the windows config
     * file for a particular version of windows.
     * @see WINKEY_WIN2000
     * @see WINKEY_WIN9X
     * @see WINKEY_WINNT
     */
    private final String windowsKey; // in ctor

    /**
     * Checks that the windows key is valid.
     *
     * @param logger AbstractLogger
     * @param windowsKey String
     */
    public WindowsBrowserLaunching(AbstractLogger logger,
                                   String windowsKey) {
        if (windowsKey == null) {
            throw new IllegalArgumentException("windowsKey cannot be null");
        }
        if (Arrays.binarySearch(WIN_KEYS, windowsKey) < 0) {
            throw new IllegalArgumentException(windowsKey + " is invalid");
        }
        this.logger = logger;
        this.windowsKey = windowsKey;
        logger.info(windowsKey);
    }

    private String getArrayAsString(String[] array) {
        return Arrays.asList(array).toString();
    }

    /**
     * Returns the protocol for the url.
     *
     * @param urlString String
     * @return String
     * @throws MalformedURLException
     */
    private String getProtocol(String urlString)
            throws MalformedURLException {
        URL url = new URL(urlString);
        return url.getProtocol();
    }

    /**
     * Returns map of browser names and exe names to
     * {@link WindowsBrowser WindowsBrowser} objects.
     * <p>
     * This is the preferred method for accessing the browser name and exe map.
     * @return Map
     */
    private Map getBrowserMap() {
        initBrowserMap();
        return browserNameAndExeMap;
    }

    /**
     * Handles lazy instantiation of available browser map.
     */
    private void initBrowserMap() {
        synchronized (WindowsBrowserLaunching.class) {
            if (browserNameAndExeMap == null) {
                // pull additional browsers from system property
                browserNameAndExeMap = getAvailableBrowsers(browsersToCheck);
            }
        }
    }

    /**
     * Accesses the Windows registry to look for browser exes. The
     * browsers search for are in the browsersToCheck list. The returned
     * map will use display names and exe names as keys to the
     * {@link WindowsBrowser WindowsBrowser} objects.
     *
     * @param browsersToCheck List
     * @return Map
     */
    private Map getAvailableBrowsers(List browsersToCheck) {
        logger.debug("entering getAvailableBrowsers");
        logger.debug("browsers to check: " + browsersToCheck);
        Map browsersAvailable = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        /*
         * We determine the list of available browsers by looking for the browser
         * executables defined on the path.  "cmd start" determines the location of the
         * executable by looking at paths defined in the registry key:
         *   HKEY_LOCAL_MACHINE\Software\Microsoft\Windows\CurrentVersion\App Paths\
         *
         * To avoid native code we expect this section of the registry to a file using:
         *   regedit.exe /E
         * Not the most efficient method but works on all versions of Windows.
         */
        try {

            File tmpFile = File.createTempFile("bl2-app-paths", ".reg");
            String[] cmdArgs = new String[] {
                               "regedit.exe",
                               "/E",
                               "\"" + tmpFile.getAbsolutePath() + "\"",
                               "\"HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\""};
            Process process = Runtime.getRuntime().exec(cmdArgs);
            int exitCode = -1;
            try {
                exitCode = process.waitFor();
            }
            catch (InterruptedException e) {
                logger.error("InterruptedException exec'ing regedit.exe: " +
                             e.getMessage());
            }

            if (exitCode != 0) {
                logger.error(
                        "Unable to exec regedit.exe to extract available browsers.");
                tmpFile.delete();
                return browsersAvailable;
            }
            /*
             * Open the newly created registry file.  First we read the first two bytes
             * of the file to check format/encoding (i.e. look for UTF-16 magic number).
             * Walk through the registry key App Paths looking for the browser executables
             * defined on the path.
             */
            FileInputStream fis = new FileInputStream(tmpFile);
            byte magic[] = new byte[2];
            fis.read(magic);
            fis.close();
            InputStreamReader in = null;
            if (magic[0] == -1 && magic[1] == -2) { // magic number 0xff 0xfe
                in = new InputStreamReader(new FileInputStream(tmpFile),
                                           "UTF-16");
            }
            else {
                in = new InputStreamReader(new FileInputStream(tmpFile));
            }
            BufferedReader reader = new BufferedReader(in);
            String line;
            while ((line = reader.readLine()) != null) {
                Iterator iter = browsersToCheck.iterator();
                boolean foundBrowser = false;
                while (iter.hasNext() && !foundBrowser) {
                    WindowsBrowser winBrowser = (WindowsBrowser) iter.next();
                    String exeName = winBrowser.getBrowserApplicationName().
                                     toLowerCase() + ".exe";
                    if (line.toLowerCase().lastIndexOf(exeName) >= 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(line);
                            logger.debug("Adding browser " +
                                         winBrowser.getBrowserDisplayName() +
                                         " to available list.");
                        }
                        // adding display and exe for backward compatibility and
                        // ease of use if someone passes in the name of an exe
                        browsersAvailable.put(winBrowser.getBrowserDisplayName(),
                                              winBrowser);
                        browsersAvailable.put(winBrowser.
                                              getBrowserApplicationName(),
                                              winBrowser);
                        iter.remove(); // already found so remove from check list.
                        foundBrowser = true;
                    }
                }
            }
            in.close();
            tmpFile.delete();
        }
        catch (IOException e) {
            logger.error("Error listing available browsers: " + e.getMessage());
        }
        return browsersAvailable;
    }

    /**
     * Returns the windows arguments for launching a default browser.
     *
     * @todo implement
     *
     * @param protocol String
     * @param urlString String
     * @return String[]
     */
    private String[] getCommandArgs(String protocol,
                                    String urlString) {
        String commandArgs = commandsDefaultBrowser.replaceAll(
                "<url>",
                urlString);
        return commandArgs.split("[ ]");
    }

    /**
     * Returns the windows arguments for launching a specified browser.
     *
     * @todo implement
     *
     * @param protocol String
     * @param browserName String
     * @param urlString String
     * @return String[]
     */
    private String[] getCommandArgs(String protocol,
                                    String browserName,
                                    String urlString) {
        String commandArgs = commandsTargettedBrowser.replaceAll(
                "<url>",
                urlString);
        commandArgs = commandArgs.replaceAll("<browser>", browserName);
        return commandArgs.split("[ ]");
    }

    /**
     * Attempts to open a url with the specified browser. This is
     * a utility method called by the openUrl methods.
     *
     * @param winBrowser WindowsBrowser
     * @param protocol String
     * @param urlString String
     * @return boolean
     * @throws BrowserLaunchingExecutionException
     */
    private boolean openUrlWithBrowser(WindowsBrowser winBrowser,
                                       String protocol,
                                       String urlString)
            throws BrowserLaunchingExecutionException {
        boolean success = false;
        try {
            logger.info(winBrowser.getBrowserDisplayName());
            logger.info(urlString);
            logger.info(protocol);
            String[] args = getCommandArgs(
                    protocol,
                    winBrowser.getBrowserApplicationName(),
                    urlString);
            if (logger.isDebugEnabled()) {
                logger.debug(getArrayAsString(args));
            }
            Process process = Runtime.getRuntime().exec(args);
            // This avoids a memory leak on some versions of Java on Windows.
            // That's hinted at in <http://developer.java.sun.com/developer/qow/archive/68/>.
            process.waitFor();
            success = process.exitValue() == 0;
        }
        // Runtimes may throw InterruptedException
        // want to catch every possible exception and wrap it
        catch (Exception e) {
            throw new BrowserLaunchingExecutionException(e);
        }
        return success;
    }

    /* ----------------- from IBrowserLaunching -------------------- */

    /**
     * Initializes the browser launcher from the windows config
     * file. It initializes the browsers to check list and
     * the command line args to use for version of windows
     * referenced by the windowsKey.
     *
     * @see windowsKey
     * @throws BrowserLaunchingInitializingException
     */
    public void initialize()
            throws BrowserLaunchingInitializingException {
        try {
            URL configUrl = getClass().getResource(CONFIGFILE_WINDOWS);
            if (configUrl == null) {
                throw new BrowserLaunchingInitializingException(
                        "unable to find config file: " + CONFIGFILE_WINDOWS);
            }
            Properties configProps = new Properties();
            configProps.load(configUrl.openStream());
            // get sep char
            String sepChar = configProps.getProperty(PROP_KEY_DELIMITER);
            // load different types of browsers
            Iterator keysIter = configProps.keySet().iterator();
            while (keysIter.hasNext()) {
                String key = (String) keysIter.next();
                if (key.startsWith(PROP_KEY_BROWSER_PREFIX)) {
                    WindowsBrowser winBrowser = new WindowsBrowser(
                            sepChar,
                            configProps.getProperty(key));
                    browsersToCheck.add(winBrowser);
                }
            }
            // load the type of windows based on the windows key
            String windowsConfigStr = configProps.getProperty(
                    windowsKey,
                    null);
            if (windowsConfigStr == null) {
                throw new BrowserLaunchingInitializingException(
                        windowsKey + " is not a valid property");
            }
            String[] winConfigItems = windowsConfigStr.split(sepChar);
            commandsDefaultBrowser = winConfigItems[0];
            commandsTargettedBrowser = winConfigItems[1];
        }
        catch (IOException ioex) {
            throw new BrowserLaunchingInitializingException(ioex);
        }
    }

    /**
     * Opens a url using the default browser.
     *
     * @param urlString String
     * @throws UnsupportedOperatingSystemException
     * @throws BrowserLaunchingExecutionException
     * @throws BrowserLaunchingInitializingException
     */
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

    /**
     * Opens a url using a specific browser.
     * <p>
     * If the specified browser is not available, the method will
     * fall through to calling the default openUrl method.
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
        if (IBrowserLaunching.BROWSER_DEFAULT.equals(browser) ||
            browser == null) {
            logger.info(
                    "default or null browser target; falling through to non-targetted openUrl");
            openUrl(urlString);
        }
        else {
            Map browserMap = getBrowserMap();
            WindowsBrowser winBrowser = (WindowsBrowser) browserMap.get(browser);
            if (winBrowser == null) {
                logger.info("the available browsers list does not contain: " +
                            browser);
                logger.info("falling through to non-targetted openUrl");
                openUrl(urlString);
            }
            else {
                String protocol = null;
                try {
                    protocol = getProtocol(urlString);
                }
                catch (MalformedURLException malrulex) {
                    throw new BrowserLaunchingExecutionException(malrulex);
                }
                boolean successfullLaunch = openUrlWithBrowser(
                        winBrowser,
                        protocol,
                        urlString);
                if (!successfullLaunch) {
                    logger.debug("falling through to non-targetted openUrl");
                    openUrl(urlString);
                }
            }
        }
    }

    /**
     * Allows user to target several browsers. The names of
     * potential browsers can be accessed via the
     * {@link #getBrowserList() getBrowserList} method.
     * <p>
     * The browsers from the list will be tried in order
     * (first to last) until one of the calls succeeds. If
     * all the calls to the requested browsers fail, the code
     * will fail over to the default browser.
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
        if (browsers == null || browsers.isEmpty()) {
            logger.debug("falling through to non-targetted openUrl");
            openUrl(urlString);
        }
        else {
            String protocol = null;
            try {
                protocol = getProtocol(urlString);
            }
            catch (MalformedURLException malrulex) {
                throw new BrowserLaunchingExecutionException(malrulex);
            }
            Map browserMap = getBrowserMap();
            boolean success = false;
            Iterator iter = browsers.iterator();
            while (iter.hasNext() && !success) {
                WindowsBrowser winBrowser = (WindowsBrowser) browserMap.get(
                        iter.next());
                if(winBrowser != null) {
                    success = openUrlWithBrowser(winBrowser,
                                                 protocol,
                                                 urlString);
                }
            }
            if (!success) {
                logger.debug(
                        "none of listed browsers succeeded; falling through to non-targetted openUrl");
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
        Map browserMap = getBrowserMap();
        List browsers = new ArrayList();
        browsers.add(IBrowserLaunching.BROWSER_DEFAULT);
        // exes are present in the map as well as display names
        Iterator iter = browserMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            WindowsBrowser winBrowser = (WindowsBrowser) browserMap.get(key);
            if (key.equals(winBrowser.getBrowserDisplayName())) {
                browsers.add(winBrowser.getBrowserDisplayName());
            }
        }
        return browsers;
    }
}
