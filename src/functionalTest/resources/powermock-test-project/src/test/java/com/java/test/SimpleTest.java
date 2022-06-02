package com.java.test;

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleTest {

    private Class1 class1 = new Class1();

    @Test
    public void absOfOneMustBeOne() {
        assertEquals(1, class1.absOfValue(1));
    }
}
