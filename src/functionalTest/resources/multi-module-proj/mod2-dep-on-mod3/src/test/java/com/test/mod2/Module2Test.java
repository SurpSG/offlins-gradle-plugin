package com.test.mod2;

import org.junit.jupiter.api.Test;

public class Module2Test {

    @Test
    void testMethodCallerThis() {
        new Module2().methodCallerThis();
    }

    @Test
    void testMethodCallerMod1() {
        new Module2().methodCallerMod1();
    }

}
