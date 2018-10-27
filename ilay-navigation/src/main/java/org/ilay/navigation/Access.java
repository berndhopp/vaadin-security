package org.ilay.navigation;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

/**
 * Determines the outcome of {@link AccessEvaluator#evaluate(Location, Class, Annotation)}. Access
 * can basically be either granted or restricted. Granted means that the route-target that is being
 * navigated to is available for the current user, a granted access is being returned by {@link
 * Access#granted()}. A restricted Access means that the current user is not allowed to navigate to
 * the route-target that is currently being navigated to and the navigation will be re-routed. A
 * restricted access can be constructed by the various restricted()-methods, that all map to the
 * {@link BeforeEnterEvent}s rerouteTo-methods with the respective parameters.
 */
@SuppressWarnings("unused")
public abstract class Access implements Serializable {

    private static final long serialVersionUID = -5142617945164430893L;

    private Access() {
    }

    /**
     * a granted access, the user will be allowed to enter the route-target.
     *
     * @return the granted access
     */
    public static Access granted() {
        return new Access() {
            @Override
            void exec(BeforeEnterEvent enterEvent) {
            }
        };
    }

    /**
     * A restricted access that will call {@link BeforeEnterEvent#rerouteToError(Exception,
     * String)}
     *
     * @param errorTarget  see {@link BeforeEnterEvent#rerouteToError(Exception, String)}
     * @param errorMessage see {@link BeforeEnterEvent#rerouteToError(Exception, String)}
     * @return the restricted Access
     */
    public static Access restricted(Exception errorTarget, String errorMessage) {
        Objects.requireNonNull(errorTarget, "errorTarget must not be null");

        return new Access() {
            @Override
            void exec(BeforeEnterEvent enterEvent) {
                enterEvent.rerouteToError(errorTarget, errorMessage);
            }
        };
    }

    /**
     * A restricted access that will call {@link BeforeEnterEvent#rerouteToError(Class)}
     *
     * @param errorTarget see {@link BeforeEnterEvent#rerouteToError(Class)}
     * @return the restricted Access
     */
    public static Access restricted(Class<? extends Exception> errorTarget) {
        Objects.requireNonNull(errorTarget, "errorTarget must not be null");

        return new Access() {
            @Override
            void exec(BeforeEnterEvent enterEvent) {
                enterEvent.rerouteToError(errorTarget);
            }
        };
    }

    /**
     * A restricted access that will call {@link BeforeEnterEvent#rerouteTo(String)}
     *
     * @param rerouteTarget see {@link BeforeEnterEvent#rerouteTo(String)}
     * @return the restricted Access
     */
    public static Access restricted(String rerouteTarget) {
        Objects.requireNonNull(rerouteTarget, "rerouteTarget must not be null");

        return new Access() {
            @Override
            void exec(BeforeEnterEvent enterEvent) {
                enterEvent.rerouteTo(rerouteTarget);
            }
        };
    }


    /**
     * A restricted access that will call {@link BeforeEnterEvent#rerouteTo(String, List)}
     *
     * @param rerouteTarget see {@link BeforeEnterEvent#rerouteTo(String, List)}
     * @return the restricted Access
     */
    public static <T> Access restricted(String rerouteTarget, List<T> parameters) {
        Objects.requireNonNull(rerouteTarget, "rerouteTarget must not be null");
        Objects.requireNonNull(parameters, "parameters must not be null");

        return new Access() {
            @Override
            void exec(BeforeEnterEvent enterEvent) {
                enterEvent.rerouteTo(rerouteTarget, parameters);
            }
        };
    }

    /**
     * A restricted access that will call {@link BeforeEnterEvent#rerouteTo(String, Object)}
     *
     * @param rerouteTarget see {@link BeforeEnterEvent#rerouteTo(String, Object)}
     * @return the restricted Access
     */
    public static <T> Access restricted(String rerouteTarget, T parameter) {
        Objects.requireNonNull(rerouteTarget, "rerouteTarget must not be null");
        Objects.requireNonNull(parameter, "parameters must not be null");

        return new Access() {
            @Override
            void exec(BeforeEnterEvent enterEvent) {
                enterEvent.rerouteTo(rerouteTarget, parameter);
            }
        };
    }

    abstract void exec(BeforeEnterEvent enterEvent);
}

