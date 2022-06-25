package com.java.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Class1IntegrationTest {
    @Test
    public void coveredShouldReturn1() {
        int covered = new Class1().method( true);
        assertEquals(1, covered);
    }
}
