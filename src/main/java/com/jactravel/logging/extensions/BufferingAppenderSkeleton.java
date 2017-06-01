package com.jactravel.logging.extensions;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

/**
 * Created by lordoftheflies on 2017.06.01..
 */
public abstract class BufferingAppenderSkeleton extends AppenderSkeleton {

    private final int DEFAULT_BUFFER_SIZE = 512;

    private int m_bufferSize = DEFAULT_BUFFER_SIZE;

    private ArrayBlockingQueue<LoggingEvent> m_cb;

    private TriggeringEventEvaluator m_evaluator;

    private boolean m_lossy = false;

    private TriggeringEventEvaluator m_lossyEvaluator;

    private FixFlags m_fixFlags = FixFlags.ALL;

    private boolean m_eventMustBeFixed;


    protected BufferingAppenderSkeleton() {
        this(true);
    }

    protected BufferingAppenderSkeleton(boolean eventMustBeFixed) {
        m_eventMustBeFixed = eventMustBeFixed;
    }

    public boolean isLossy() {
        return m_lossy;
    }

    public void setLossy(boolean m_lossy) {
        this.m_lossy = m_lossy;
    }

    private Class<TriggeringEventEvaluator> evaluatorClass = null;

    public Class<TriggeringEventEvaluator> getEvaluatorClass() {
        return evaluatorClass;
    }

    public void setEvaluatorClass(Class<TriggeringEventEvaluator> evaluatorClass) {
        this.evaluatorClass = evaluatorClass;
    }

    public TriggeringEventEvaluator getEvaluator() {
        if (m_evaluator == null) {
            try {
                return evaluatorClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        }
        return m_evaluator;
    }

    public void setEvaluator(TriggeringEventEvaluator m_evaluator) {
        this.m_evaluator = m_evaluator;
    }

    public int getBufferSize() {
        return m_bufferSize;
    }

    public void setBufferSize(int m_bufferSize) {
        this.m_bufferSize = m_bufferSize;
    }

    public boolean flush(int millisecondsTimeout) {
        this.flush();
        return true;
    }

    public void flush() {
        this.flush(false);
    }

    public void flush(boolean flushLossyBuffer) {
        // This method will be called outside of the AppenderSkeleton DoAppend() method
        // therefore it needs to be protected by its own lock. This will block any
        // Appends while the buffer is flushed.
        synchronized (this) {
            if (m_cb != null && m_cb.size() > 0) {
                if (m_lossy) {
                    // If we are allowed to eagerly flush from the lossy buffer
                    if (flushLossyBuffer) {
                        if (m_lossyEvaluator != null) {
                            // Test the contents of the buffer against the lossy evaluator
                            List<LoggingEvent> bufferedEvents = m_cb.stream().collect(Collectors.toList());
                            List<LoggingEvent> filteredEvents = new ArrayList(bufferedEvents.size());

                            for (LoggingEvent loggingEvent : bufferedEvents)
                            {
                                if (m_lossyEvaluator.isTriggeringEvent(loggingEvent)) {
                                    filteredEvents.add(loggingEvent);
                                }
                            }

                            // Send the events that meet the lossy evaluator criteria
                            if (filteredEvents.size() > 0) {
                                LoggingEvent[] events = new LoggingEvent[filteredEvents.size()];
                                sendBuffer(filteredEvents.toArray(events));
                            }

                        } else {
                            // No lossy evaluator, all buffered events are discarded
                            m_cb.clear();
                        }
                    }
                } else {
                    // Not lossy, send whole buffer
                    SendFromBuffer(null, m_cb);
                }
            }
        }
    }


    public void ActivateOptions() {
        super.activateOptions();

        // If the appender is in Lossy mode then we will
        // only send the buffer when the Evaluator triggers
        // therefore check we have an evaluator.
        if (m_lossy && m_evaluator == null) {
//             ErrorHandler.error("Appender [" + name + "] is Lossy but has no Evaluator. The buffer will never be sent!");
        }

        if (m_bufferSize > 1) {
            m_cb = new ArrayBlockingQueue<LoggingEvent>(m_bufferSize);
        } else {
            m_cb = null;
        }
    }

    protected void onClose() {
        // Flush the buffer on close
        flush(true);
    }

    protected void append(LoggingEvent loggingEvent) {
        // If the buffer size is set to 1 or less then the buffer will be
        // sent immediately because there is not enough space in the buffer
        // to buffer up more than 1 event. Therefore as a special case
        // we don't use the buffer at all.
        if (m_cb == null || m_bufferSize <= 1) {
            // Only send the event if we are in non lossy mode or the event is a triggering event
            if ((!m_lossy) ||
                    (m_evaluator != null && m_evaluator.isTriggeringEvent(loggingEvent)) ||
                    (m_lossyEvaluator != null && m_lossyEvaluator.isTriggeringEvent(loggingEvent))) {
                if (m_eventMustBeFixed) {
                    // Derive class expects fixed events
//                    loggingEvent. = this.Fix;
                }

                // Not buffering events, send immediately
                sendBuffer(new LoggingEvent[]{loggingEvent});
            }
        } else {
            // Because we are caching the LoggingEvent beyond the
            // lifetime of the Append() method we must fix any
            // volatile data in the event.
//            loggingEvent.Fix = this.Fix;

            // Add to the buffer, returns the event discarded from the buffer if there is no space remaining after the append
            LoggingEvent discardedLoggingEvent = null;
            try {
                m_cb.put(loggingEvent);
                discardedLoggingEvent = m_cb.peek();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (discardedLoggingEvent != null) {
                // Buffer is full and has had to discard an event
                if (!m_lossy) {
                    // Not lossy, must send all events
                    SendFromBuffer(discardedLoggingEvent, m_cb);
                } else {
                    // Check if the discarded event should not be logged
                    if (m_lossyEvaluator == null || !m_lossyEvaluator.isTriggeringEvent(discardedLoggingEvent)) {
                        // Clear the discarded event as we should not forward it
                        discardedLoggingEvent = null;
                    }

                    // Check if the event should trigger the whole buffer to be sent
                    if (m_evaluator != null && m_evaluator.isTriggeringEvent(loggingEvent)) {
                        SendFromBuffer(discardedLoggingEvent, m_cb);
                    } else if (discardedLoggingEvent != null) {
                        // Just send the discarded event
                        sendBuffer(new LoggingEvent[]{discardedLoggingEvent});
                    }
                }
            } else {
                // Buffer is not yet full

                // Check if the event should trigger the whole buffer to be sent
                if (m_evaluator != null && m_evaluator.isTriggeringEvent(loggingEvent)) {
                    SendFromBuffer(null, m_cb);
                }
            }
        }
    }

    protected void SendFromBuffer(LoggingEvent firstLoggingEvent, ArrayBlockingQueue<LoggingEvent> buffer) {
        LoggingEvent[] bufferEvents = new LoggingEvent[buffer.size()];
        bufferEvents = buffer.toArray(bufferEvents);

        if (firstLoggingEvent == null) {
            sendBuffer(bufferEvents);
        } else if (bufferEvents.length == 0) {
            sendBuffer(new LoggingEvent[]{firstLoggingEvent});
        } else {
            // Create new array with the firstLoggingEvent at the head
            LoggingEvent[] events = new LoggingEvent[bufferEvents.length + 1];
            events = Arrays.copyOfRange(bufferEvents, 1, bufferEvents.length);
            events[0] = firstLoggingEvent;
            sendBuffer(events);
        }
    }

    abstract protected void sendBuffer(LoggingEvent[] events);

}
