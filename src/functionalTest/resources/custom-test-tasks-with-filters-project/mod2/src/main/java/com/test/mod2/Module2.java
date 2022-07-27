package com.test.mod2;

import com.test.mod3.Module3;

public class Module2 {

    public void methodCallerMod1() {
        new Module3().methodCallerMod2();
    }

}
