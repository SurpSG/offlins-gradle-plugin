package com.java.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Class1Test {
    @Test
    public void coveredShouldReturn1() {
        int res = new Class1().method( false);
        assertEquals(0, res);
    }
}
