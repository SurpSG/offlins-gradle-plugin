package com.test.mod1;

import org.junit.jupiter.api.Test;
import com.test.mod3.Module3;

public class Module1Test {

    @Test
    void testMethod() {
        new Module1().methodCallerMod1(true);
    }

    @Test
    void testMethod2() {
        new Module3().methodCallerMod1();
    }

}
