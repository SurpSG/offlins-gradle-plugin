package com.test.mod2;

import com.test.mod3.Module3;
public class Module2 {

    public void methodCallerThis() {
        System.out.println("method called by this mod2");
    }

    public void methodCallerMod1() {
        System.out.println("method called by mod1");
        new Module3().methodCallerMod2();
    }

}
