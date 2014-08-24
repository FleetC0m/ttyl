package io.github.fleetc0m.ttyl.core;

import io.github.fleetc0m.ttyl.events.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Event bus handles all incoming events from Updater and propagate events to all observers.
 */
public class EventBus {
    public static final String TAG = "EventBus";
    public static interface Updater {

    }

    public static interface Observer {
        public void onStateChanged(Event event);
        public boolean shouldResponseTo(int eventType);
    }

    private static EventBus mEventBus;
    private final List<Updater> mUpdaterList;
    private final List<Observer> mObserverList;

    protected EventBus() {
        mEventBus = new EventBus();
        mUpdaterList = new ArrayList<Updater>();
        mObserverList = new ArrayList<Observer>();
    }

    public synchronized void registerUpdater(Updater updater) {
        mUpdaterList.add(updater);
    }

    public synchronized void registerObserver(Observer observer) {
        mObserverList.add(observer);
    }

    public synchronized void onStateChanged(Event event) {
        for (Observer observer : mObserverList) {
            if (observer.shouldResponseTo(event.getEventType())) {
                observer.onStateChanged(event);
            }
        }
    }

    public static synchronized EventBus getEventBus() {
        if (mEventBus != null) {
            return mEventBus;
        }
        mEventBus = new EventBus();
        return mEventBus;
    }
}
