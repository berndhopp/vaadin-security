package org.ilay;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.DefaultDeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Registration;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;

class TestUtil {

    static void newSession() {
        Service service = (Service) VaadinService.getCurrent();

        try {
            service.newSession();
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        final UI ui = new UI() {
            @Override
            protected void init(VaadinRequest request) {
            }
        };

        UI.setCurrent(ui);
        Navigator navigator = Mockito.mock(Navigator.class);
        ui.setNavigator(navigator);
    }

    static void beforeTest() throws ServiceException {
        Authorization.reset();
        CurrentInstance.clearAll();
        VaadinService.setCurrent(new Service());
    }

    static class Session extends VaadinSession {

        private final Map<Class<?>, Object> map = new HashMap<>();

        public Session(VaadinService service) {
            super(service);
        }

        @Override
        public <T> void setAttribute(Class<T> type, T value) {
            map.put(type, value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getAttribute(Class<T> type) {
            return (T) map.get(type);
        }
    }

    static class Service extends VaadinServletService {
        private static final long serialVersionUID = -2341047711806094131L;
        private List<SessionInitListener> sessionInitListeners = new ArrayList<>();

        Service() throws ServiceException {
            super(new VaadinServlet(), new DefaultDeploymentConfiguration(Object.class, new Properties()));
        }

        @Override
        public Registration addSessionInitListener(SessionInitListener listener) {
            sessionInitListeners.add(listener);
            return () -> sessionInitListeners.remove(listener);
        }

        void newSession() throws ServiceException {
            final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            final VaadinSession vaadinSession = new Session(this);
            final SessionInitEvent event = new SessionInitEvent(this, vaadinSession, new VaadinServletRequest(httpServletRequest, this));
            VaadinSession.setCurrent(vaadinSession);

            for (SessionInitListener sessionInitListener : sessionInitListeners) {
                sessionInitListener.sessionInit(event);
            }

        }
    }
}
