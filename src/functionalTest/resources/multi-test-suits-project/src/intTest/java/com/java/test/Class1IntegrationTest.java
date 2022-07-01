package com.java.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Class1IntegrationTest {
    @Test
    public void coveredShouldReturn1() {
        int covered = new Class1().method( true);
        assertEquals(1, covered);
    }
}
