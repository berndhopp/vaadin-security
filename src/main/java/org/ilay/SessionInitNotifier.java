package org.ilay;

import com.vaadin.server.SessionInitListener;

interface SessionInitNotifier {
    void addSessionInitListener(SessionInitListener listener);
}
