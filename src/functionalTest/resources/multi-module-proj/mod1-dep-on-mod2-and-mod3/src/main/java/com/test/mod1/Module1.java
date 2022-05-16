package com.test.mod1;

import com.test.mod2.Module2;

public class Module1 {

    public void methodCallerMod1(boolean arg) {
        if (arg) {
            new Module2().methodCallerMod1();
        } else {
            System.out.println("module1");
        }
    }

}
