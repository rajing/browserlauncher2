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
// $Id: BrowserLauncherTestApp.java,v 1.6 2005/01/29 21:01:17 roskakori Exp $
package edu.stanford.ejalbert.testing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 * Standalone gui that allows for testing the broserlauncher code and provides
 * a sample implementation.
 *
 * @author Jeff Chapman
 */
public class BrowserLauncherTestApp extends JFrame {
    private JPanel mainPanel = new JPanel();
    private JButton browseButton = new JButton();
    private JLabel enterUrlLabel = new JLabel();
    private JTextField urlTextField = new JTextField();
    private BrowserLauncher launcher; // in ctor

    public BrowserLauncherTestApp() {
        super("BrowserLauncher Test App");
        try {
            launcher = new BrowserLauncher();
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BrowserLauncherTestApp app = new BrowserLauncherTestApp();
        app.pack();
        app.setVisible(true);
    }

    private void jbInit() throws Exception {
        urlTextField.setColumns(20);
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browseButton_actionPerformed(e);
            }
        });
        browseButton.setText("Browse");
        enterUrlLabel.setText("Enter a url:");
        urlTextField.setText("file://localhost/");
        mainPanel.add(enterUrlLabel);
        mainPanel.add(urlTextField);
        mainPanel.add(browseButton);
        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(browseButton);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void browseButton_actionPerformed(ActionEvent e) {
        String urlString = urlTextField.getText();
        try {
            BrowserLauncherRunner runner =
                    new BrowserLauncherRunner(urlString, launcher);
            Thread launcherThread = new Thread(runner);
            launcherThread.start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                                          ex.getMessage(),
                                          "Error Message",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class BrowserLauncherRunner implements Runnable {
        private String urlString; // in ctor
        private BrowserLauncher launcher; // in ctor

        BrowserLauncherRunner(String urlString, BrowserLauncher launcher) throws MalformedURLException {
            // validate URL
            if (urlString == null || urlString.length() == 0) {
                throw new MalformedURLException("URL to browse must be specified");
            }
            new URL(urlString);
            this.urlString = urlString;
            this.launcher = launcher;
        }

        public void run() {
            try {
                launcher.openURLinBrowser(urlString);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                              ex.getMessage(),
                                              "Error Message",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
