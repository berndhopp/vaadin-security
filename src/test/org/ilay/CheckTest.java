package org.ilay;

import org.ilay.api.Restrict;
import org.ilay.api.Reverter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CheckTest {

    @Test(expected = IllegalArgumentException.class)
    public void null_collection_should_fail() {
        Check.notNullOrEmpty((Collection) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void empty_collection_should_fail() {
        Check.notNullOrEmpty(new ArrayList<>());
    }

    @Test
    public void non_empty_string_should_fail() {
        Check.notNullOrEmpty("hallo welt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_string_should_fail() {
        Check.notNullOrEmpty((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void empty_string_should_fail() {
        Check.notNullOrEmpty("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void empty_map_should_fail() {
        Check.notNullOrEmpty(new HashMap<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_map_should_fail() {
        Check.notNullOrEmpty((Map<Object, Object>) null);
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

    @Test(expected = IllegalStateException.class)
    public void test_unknown_restrict_class_should_throw_exception() {
        Check.setCurrentRestrict(new Restrict() {
            @Override
            public Reverter to(Object permission) {
                return null;
            }

            @Override
            public Reverter to(Object... permissions) {
                return null;
            }
        });

        Check.noUnclosedRestrict();
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
