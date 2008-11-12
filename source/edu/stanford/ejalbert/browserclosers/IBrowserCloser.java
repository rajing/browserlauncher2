package edu.stanford.ejalbert.browserclosers;

import edu.stanford.ejalbert.browserevents.BrowserEventListener;
import edu.stanford.ejalbert.browserevents.BrowserEvent;

/**
 *
 * @author not attributable
 * @version 1.0
 */
public interface IBrowserCloser extends BrowserEventListener {
    public BrowserEvent[] getBrowserEvents();
    public void closeBrowser(BrowserEvent event);
}
