package org.ilay.api;

/**
 * An DataAuthorizer is a special type of evaluators that can be used in cases where {@link
 * com.vaadin.data.provider.DataProvider}s are filtered by the evaluator, such as when
 * using ILAY to filter data on a Grid.
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
public interface DataAuthorizer<T, U> extends Authorizer<T> {

    /**
     * the filter to be used for DataProviders, see {@link com.vaadin.data.provider.DataProvider}
     */
    U asFilter();
}
