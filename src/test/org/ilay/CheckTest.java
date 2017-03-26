package org.ilay;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CheckTest {

    @Test(expected = NullPointerException.class)
    public void null_should_fail() {
        Check.notNullOrEmpty(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void empty_should_fail() {
        Check.notNullOrEmpty(new ArrayList<>());
    }

    @Test
    public void not_empty_should_work() {
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(1);

        Check.notNullOrEmpty(integers);
    }

    @Test
    public void test_state_positive() {
        Check.state(true);
    }

    @Test(expected = IllegalStateException.class)
    public void test_state_negative() {
        Check.state(false);
    }

    @Test
    public void test_arg_positive() {
        Check.arg(true, "no message", 1, 2, null);
    }

    @Test
    public void test_arg_negative() {
        try {
            Check.arg(false, "%s %s %s", 1, 2, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("1 2 null", e.getMessage());
        }
    }
}
