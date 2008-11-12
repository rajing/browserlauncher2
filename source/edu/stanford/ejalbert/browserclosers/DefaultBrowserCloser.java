package edu.stanford.ejalbert.browserclosers;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.ejalbert.browserevents.BrowserEvent;

/**
 *
 * @author not attributable
 * @version 1.0
 */
public abstract class DefaultBrowserCloser
        implements IBrowserCloser {
    private final Map browserEvents = new HashMap();

    public DefaultBrowserCloser() {
    }

    protected abstract void killBrowser(BrowserEvent event);

    /* --------------- from BrowserEventListener -------------- */

    public void handleBrowserEvent(BrowserEvent event) {
        if (BrowserEvent.ID_BROWSER_LAUNCHED == event.getEventId()) {
            Integer key = new Integer(event.getAttemptId());
            synchronized (browserEvents) {
                browserEvents.put(key, event);
            }
        }
    }

    /* ------------------ from IBrowserCloser ----------------- */

    public BrowserEvent[] getBrowserEvents() {
        int cnt = 0;
        BrowserEvent[] events = null;
        synchronized (browserEvents) {
            cnt = browserEvents.size();
            events = (BrowserEvent[]) browserEvents.values().toArray(
                    new BrowserEvent[cnt]);
        }
        return events;
    }

    public void closeBrowser(BrowserEvent event) {
        if(event != null) {
            Integer key = new Integer(event.getAttemptId());
            BrowserEvent bevent = null;
            synchronized(browserEvents) {
                bevent = (BrowserEvent)browserEvents.remove(key);
            }
            if(bevent != null) {
                killBrowser(bevent);
            }
        }
    }
}
