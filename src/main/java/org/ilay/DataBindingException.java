package org.ilay;

public class DataBindingException extends RuntimeException {
    DataBindingException(ClassCastException e) {
        super(e);
    }
}
