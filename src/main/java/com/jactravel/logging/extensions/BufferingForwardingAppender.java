package com.jactravel.logging.extensions;

import org.apache.log4j.Appender;
import org.apache.log4j.helpers.AppenderAttachableImpl;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Stream;

/**
 * Created by lordoftheflies on 2017.05.31..
 */
public class BufferingForwardingAppender extends BufferingAppenderSkeleton implements AppenderAttachable {


    public BufferingForwardingAppender() {
    }


    @Override
    protected void onClose() {
        // Remove all the attached appenders
        synchronized (this) {
            // Delegate to base, which will flush buffers
            super.onClose();

            if (m_appenderAttachedImpl != null) {
                m_appenderAttachedImpl.removeAllAppenders();
            }
        }
    }

    @Override
    protected void sendBuffer(LoggingEvent[] events) {
        // Pass the logging event on to the attached appenders
        if (m_appenderAttachedImpl != null) {
            Stream.of(events).forEach(e -> m_appenderAttachedImpl.appendLoopOnAppenders(e));

        }
    }

    @Override
    public void addAppender(Appender newAppender) {
        if (newAppender == null) {
            throw new IllegalArgumentException("newAppender");
        }
        synchronized (this) {
            if (m_appenderAttachedImpl == null) {
                m_appenderAttachedImpl = new AppenderAttachableImpl();
            }
            m_appenderAttachedImpl.addAppender(newAppender);
        }
    }

    @Override
    public Enumeration<Appender> getAllAppenders() {
        synchronized (this) {
            if (m_appenderAttachedImpl == null) {
                return Collections.enumeration(Collections.emptyList());
            } else {
                return m_appenderAttachedImpl.getAllAppenders();
            }
        }
    }


    @Override
    public Appender getAppender(String name) {
        synchronized (this) {
            if (m_appenderAttachedImpl == null || name == null) {
                return null;
            }

            return m_appenderAttachedImpl.getAppender(name);
        }
    }

    @Override
    public boolean isAttached(Appender appender) {
        return false;
    }


    @Override
    public void removeAllAppenders() {
        synchronized (this) {
            if (m_appenderAttachedImpl != null) {
                m_appenderAttachedImpl.removeAllAppenders();
                m_appenderAttachedImpl = null;
            }
        }
    }

    @Override
    public void removeAppender(Appender appender) {
        synchronized (this) {
            if (appender != null && m_appenderAttachedImpl != null) {
                m_appenderAttachedImpl.removeAppender(appender);
            }
        }
    }

    @Override
    public void removeAppender(String name) {
        synchronized (this) {
            if (name != null && m_appenderAttachedImpl != null) {
                m_appenderAttachedImpl.getAppender(name);
                m_appenderAttachedImpl.removeAppender(name);
            }
        }
    }

    private AppenderAttachableImpl m_appenderAttachedImpl;

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
