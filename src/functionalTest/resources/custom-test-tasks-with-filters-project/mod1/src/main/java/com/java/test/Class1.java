package com.java.test;

import com.test.mod2.Module2;

public class Class1 {

    public int method(boolean isIntegrationTest) {
        Module2 mod2 = new Module2();
        if(isIntegrationTest) {
            mod2.methodCallerMod1();
            return 1;
        }
        mod2.methodCallerMod1();
        return 0;
    }

}
