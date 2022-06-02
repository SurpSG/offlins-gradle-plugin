package com.java.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StaticClass.class })
public class MockStaticTest {

    private Class1 class1 = new Class1();

    @Test
    public void mockStaticTest() {
        PowerMockito.mockStatic(StaticClass.class);
        PowerMockito.when(StaticClass.abs(-1)).thenReturn(-1);

        assertEquals(-1, class1.absOfValue(-1));
    }
}
