package com.java.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Class1UnitTest {
    @Test
    public void coveredShouldReturn1() {
        int res = new Class1().method(false);
        assertEquals(0, res);

        int res2 = new Class1().method(true);
        assertEquals(1, res2);
    }
}
