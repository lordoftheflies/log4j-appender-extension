package com.jactravel.logging.extensions;

import org.apache.log4j.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.AppenderAttachableImpl;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Level;

/**
 * Created by lordoftheflies on 2017.05.31..
 */
public class BufferingForwardingAppender extends RollingFileAppender
        implements AppenderAttachable {
    /**
     * The default buffer size is set to 128 events.
     */
    public static final int DEFAULT_BUFFER_SIZE = 128;

    private boolean concurrent;

    public boolean isConcurrent() {
        return concurrent;
    }

    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    /**
     * Event buffer, also used as monitor to protect itself and
     * discardMap from simulatenous modifications.
     */
    private final List<LoggingEvent> buffer = new ArrayList<LoggingEvent>();

    /**
     * Map of DiscardSummary objects keyed by logger name.
     */
    private final Map discardMap = new HashMap();

    private Priority triggerThreshold;

    private String appenderRef;

    /**
     * Buffer size.
     */
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * Nested appenders.
     */
    AppenderAttachableImpl aai;

    /**
     * Nested appenders.
     */
    private final AppenderAttachableImpl appenders;

    /**
     * Dispatcher.
     */
//    private final Thread dispatcher;

    /**
     * Should location info be included in dispatched messages.
     */
    private boolean locationInfo = false;

    /**
     * Does appender block when buffer is full.
     */
    private boolean blocking = true;


    public BufferingForwardingAppender() {
        super();
        appenders = new AppenderAttachableImpl();
    }

//    public BufferingForwardingAppender(Layout layout, String filename, boolean append) throws IOException {
//        super(layout, filename, append);
//        appenders = new AppenderAttachableImpl();
//        aai = appenders;
//    }
//
//    public BufferingForwardingAppender(Layout layout, String filename) throws IOException {
//        super(layout, filename);
//        appenders = new AppenderAttachableImpl();
//        aai = appenders;
//    }

    /**
     * Create new instance.
     */
//    public BufferingForwardingAppender() {
//        super(true);
//        appenders = new AppenderAttachableImpl();
//
//        //
//        //   only set for compatibility
//        aai = appenders;
//
////        dispatcher = new Thread(new Dispatcher(this, buffer, discardMap, appenders));
//
//        // It is the user's responsibility to close appenders before
//        // exiting.
////        dispatcher.setDaemon(true);
//
//        // set the dispatcher priority to lowest possible value
//        //        dispatcher.setPriority(Thread.MIN_PRIORITY);
////        dispatcher.setName("Dispatcher-" + dispatcher.getName());
////        dispatcher.start();
//    }
    public Priority getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(Priority triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

//    public String getAppenderRef() {
//        return appenderRef;
//    }
//
//    public void setAppenderRef(String appenderRef) {
//        Enumeration appender = null;
//        if (this.appenderRef == null) {
//            this.appenderRef = appenderRef;
//            appender = this.getAllAppenders();
//        }
//        this.appenderRef = appenderRef;
//    }

    public void setAppenderFromLogger(String name) {
        Logger l = Logger.getLogger(name);

        Enumeration<Appender> e = l.getAllAppenders();

        while (e.hasMoreElements()) {
            Appender a = e.nextElement();
            this.addAppender(a);
            System.out.println("The newAppender " + a.getName() + " attach status " + this.isAttached(a));
        }
    }

    /**
     * Add appender.
     *
     * @param newAppender appender to add, may not be null.
     */
    @Override
    public void addAppender(final Appender newAppender) {
        synchronized (appenders) {
            appenders.addAppender(newAppender);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final LoggingEvent event) {

        if (this.getTriggerThreshold().isGreaterOrEqual(event.getLevel())) {
            System.out.println("Add event to buffer: " + event.getLevel() + ":" + event.getMessage());
            buffer.add(event);
        }


        if (event.getLevel().isGreaterOrEqual(this.getTriggerThreshold())) {
            System.out.println("Forwarding buffer with " + buffer.size() + ": " + event.getLevel() + ":" + event.getMessage());
            buffer.stream().forEach(e -> BufferingForwardingAppender.super.append(e));
            buffer.clear();
        }
        //
        //   if dispatcher thread has died then
        //      append subsequent events synchronously
        //   See bug 23021
//        if ((dispatcher == null) || !dispatcher.isAlive() || (bufferSize <= 0)) {
//            synchronized (appenders) {
//                appenders.appendLoopOnAppenders(event);
//            }
//
//            return;
//        }

        // TODO: Check
        // extract all the thread dependent information now as later it will be too late.
        //event.prepareForDeferredProcessing();

//        if (locationInfo) {
//            event.getLocationInformation();
//        }

//        synchronized (buffer) {
//            while (true) {
//                int previousSize = buffer.size();

        // TODO: Check
//                if (previousSize < bufferSize) {
//                    buffer.add(event);
//
//                    //
//                    //   if buffer had been empty
//                    //       signal all threads waiting on buffer
//                    //       to check their conditions.
//                    //
//                    if (previousSize == 0) {
//                        buffer.notifyAll();
//                    }
//
//                    break;
//                }

        // TODO: Check
//                //
//                //   Following code is only reachable if buffer is full
//                //
//                //
//                //   if blocking and thread is not already interrupted
//                //      and not the dispatcher then
//                //      wait for a buffer notification
//                boolean discard = true;
//                if (blocking
//                        && !Thread.interrupted()
//                        && Thread.currentThread() != dispatcher) {
//                    try {
//                        buffer.wait();
//                        discard = false;
//                    } catch (InterruptedException e) {
//                        //
//                        //  reset interrupt status so
//                        //    calling code can see interrupt on
//                        //    their next wait or sleep.
//                        Thread.currentThread().interrupt();
//                    }
//                }

        // TODO: Check
        //
        //   if blocking is false or thread has been interrupted
        //   add event to discard map.
        //
//                if (discard) {
//                    String loggerName = event.getLoggerName();
////                    DiscardSummary summary = (DiscardSummary) discardMap.get(loggerName);
////
////                    if (summary == null) {
////                        summary = new DiscardSummary(event);
////                        discardMap.put(loggerName, summary);
////                    } else {
////                        summary.add(event);
////                    }
//
//                    break;
//                }
//            }
//        }
    }


    /**
     * Close this <code>AsyncAppender</code> by interrupting the dispatcher
     * thread which will process all pending events before exiting.
     */
    public void close() {
        /**
         * Set closed flag and notify all threads to check their conditions.
         * Should result in dispatcher terminating.
         */
        synchronized (buffer) {
            closed = true;
//            buffer.notifyAll();
        }

//        try {
//            dispatcher.join();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            getLogger().error(
//                    "Got an InterruptedException while waiting for the "
//                            + "dispatcher to finish.", e);
//        }

        //
        //    close all attached appenders.
        //
        synchronized (appenders) {
            Enumeration iter = appenders.getAllAppenders();

            if (iter != null) {
                while (iter.hasMoreElements()) {
                    Object next = iter.nextElement();

                    if (next instanceof Appender) {
                        ((Appender) next).close();
                    }
                }
            }
        }
    }

    /**
     * Get iterator over attached appenders.
     *
     * @return iterator or null if no attached appenders.
     */
    public Enumeration getAllAppenders() {
        synchronized (appenders) {
            return appenders.getAllAppenders();
        }
    }

    /**
     * Get appender by name.
     *
     * @param name name, may not be null.
     * @return matching appender or null.
     */
    public Appender getAppender(final String name) {
        synchronized (appenders) {
            return appenders.getAppender(name);
        }
    }

    /**
     * Gets whether the location of the logging request call
     * should be captured.
     *
     * @return the current value of the <b>LocationInfo</b> option.
     */
    public boolean getLocationInfo() {
        return locationInfo;
    }

    /**
     * Determines if specified appender is attached.
     *
     * @param appender appender.
     * @return true if attached.
     */
    public boolean isAttached(final Appender appender) {
        synchronized (appenders) {
            return appenders.isAttached(appender);
        }
    }

    /**
     * {@inheritDoc}
     */
//    public boolean requiresLayout() {
//        return true;
//    }

    /**
     * Removes and closes all attached appenders.
     */
    public void removeAllAppenders() {
        synchronized (appenders) {
            appenders.removeAllAppenders();
        }
    }

    /**
     * Removes an appender.
     *
     * @param appender appender to remove.
     */
    public void removeAppender(final Appender appender) {
        synchronized (appenders) {
            appenders.removeAppender(appender);
        }
    }

    /**
     * Remove appender by name.
     *
     * @param name name.
     */
    public void removeAppender(final String name) {
        synchronized (appenders) {
            appenders.removeAppender(name);
        }
    }

    /**
     * The <b>LocationInfo</b> option takes a boolean value. By default, it is
     * set to false which means there will be no effort to extract the location
     * information related to the event. As a result, the event that will be
     * ultimately logged will likely to contain the wrong location information
     * (if present in the log format).
     * <p/>
     * <p/>
     * Location information extraction is comparatively very slow and should be
     * avoided unless performance is not a concern.
     * </p>
     *
     * @param flag true if location information should be extracted.
     */
    public void setLocationInfo(final boolean flag) {
        locationInfo = flag;
    }

    /**
     * Sets the number of messages allowed in the event buffer
     * before the calling thread is blocked (if blocking is true)
     * or until messages are summarized and discarded.  Changing
     * the size will not affect messages already in the buffer.
     *
     * @param size buffer size, must be positive.
     */
    public void setBufferSize(final int size) {
        //
        //   log4j 1.2 would throw exception if size was negative
        //      and deadlock if size was zero.
        //
        if (size < 0) {
            throw new java.lang.NegativeArraySizeException("size");
        }

        synchronized (buffer) {
            //
            //   don't let size be zero.
            //
            bufferSize = (size < 1) ? 1 : size;
            buffer.notifyAll();
        }
    }

    /**
     * Gets the current buffer size.
     *
     * @return the current value of the <b>BufferSize</b> option.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets whether appender should wait if there is no
     * space available in the event buffer or immediately return.
     *
     * @param value true if appender should wait until available space in buffer.
     */
    public void setBlocking(final boolean value) {
        synchronized (buffer) {
            blocking = value;
            buffer.notifyAll();
        }
    }

    /**
     * Gets whether appender should block calling thread when buffer is full.
     * If false, messages will be counted by logger and a summary
     * message appended after the contents of the buffer have been appended.
     *
     * @return true if calling thread will be blocked when buffer is full.
     */
    public boolean getBlocking() {
        return blocking;
    }

    public boolean isClosed() {
        return closed;
    }

    public Logger getLogger() {
        return LogManager.getRootLogger();
    }

//    /**
//     * Summary of discarded logging events for a logger.
//     */
//    private static final class DiscardSummary {
//        /**
//         * First event of the highest severity.
//         */
//        private LoggingEvent maxEvent;
//
//        /**
//         * Total count of messages discarded.
//         */
//        private int count;
//
//        /**
//         * Create new instance.
//         *
//         * @param event event, may not be null.
//         */
//        public DiscardSummary(final LoggingEvent event) {
//            maxEvent = event;
//            count = 1;
//        }
//
//        /**
//         * Add discarded event to summary.
//         *
//         * @param event event, may not be null.
//         */
//        public void add(final LoggingEvent event) {
//            if (event.getLevel().toInt() > maxEvent.getLevel().toInt()) {
//                maxEvent = event;
//            }
//
//            count++;
//        }
//
//        /**
//         * Create event with summary information.
//         *
//         * @return new event.
//         */
//        public LoggingEvent createEvent() {
//            String msg =
//                    MessageFormat.format(
//                            "Discarded {0} messages due to full event buffer including: {1}",
//                            new Object[] { new Integer(count), maxEvent.getMessage() });
//
//            return new LoggingEvent(
//                    "org.apache.log4j.AsyncAppender.DONT_REPORT_LOCATION",
//                    maxEvent.getLogger(),
//                    maxEvent.getLevel(),
//                    msg,
//                    null);
//        }
//    }
//
//    /**
//     * Event dispatcher.
//     */
//    private static class Dispatcher implements Runnable {
//        /**
//         * Parent AsyncAppender.
//         */
//        private final BufferingForwardingAppender parent;
//
//        /**
//         * Event buffer.
//         */
//        private final List buffer;
//
//        /**
//         * Map of DiscardSummary keyed by logger name.
//         */
//        private final Map discardMap;
//
//        /**
//         * Wrapped appenders.
//         */
//        private final AppenderAttachableImpl appenders;
//
//        /**
//         * Create new instance of dispatcher.
//         *
//         * @param parent     parent AsyncAppender, may not be null.
//         * @param buffer     event buffer, may not be null.
//         * @param discardMap discard map, may not be null.
//         * @param appenders  appenders, may not be null.
//         */
//        public Dispatcher(
//                final BufferingForwardingAppender parent, final List buffer, final Map discardMap,
//                final AppenderAttachableImpl appenders) {
//
//            this.parent = parent;
//            this.buffer = buffer;
//            this.appenders = appenders;
//            this.discardMap = discardMap;
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        public void run() {
//            boolean isActive = true;
//
//            //
//            //   if interrupted (unlikely), end thread
//            //
//            try {
//                //
//                //   loop until the AsyncAppender is closed.
//                //
//                while (isActive) {
//                    LoggingEvent[] events = null;
//
//                    //
//                    //   extract pending events while synchronized
//                    //       on buffer
//                    //
//                    synchronized (buffer) {
//                        int bufferSize = buffer.size();
//                        isActive = !parent.isClosed();
//
//                        while ((bufferSize == 0) && isActive) {
//                            buffer.wait();
//                            bufferSize = buffer.size();
//                            isActive = !parent.isClosed();
//                        }
//
//                        if (bufferSize > 0) {
//                            events = new LoggingEvent[bufferSize + discardMap.size()];
//                            buffer.toArray(events);
//
//                            //
//                            //   add events due to buffer overflow
//                            //
//                            int index = bufferSize;
//
//                            for (
//                                    Iterator iter = discardMap.values().iterator();
//                                    iter.hasNext(); ) {
//                                events[index++] = ((DiscardSummary) iter.next()).createEvent();
//                            }
//
//                            //
//                            //    clear buffer and discard map
//                            //
//                            buffer.clear();
//                            discardMap.clear();
//
//                            //
//                            //    allow blocked appends to continue
//                            buffer.notifyAll();
//                        }
//                    }
//
//                    //
//                    //   process events after lock on buffer is released.
//                    //
//                    if (events != null) {
//                        for (int i = 0; i < events.length; i++) {
//                            synchronized (appenders) {
//                                appenders.appendLoopOnAppenders(events[i]);
//                            }
//                        }
//                    }
//                }
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//            }
//        }
//    }
//
//
}