package com.test.mod2;

import org.junit.Test;

public class Module2Test {

    @Test
    public void testMethodCallerThis() {
        new Module2().methodCallerThis();
    }

    @Test
    public void testMethodCallerMod1() {
        new Module2().methodCallerMod1();
    }

}
