package nextstep.context.test_case_3;

import nextstep.web.annotation.PeanutConfiguration;
import nextstep.web.annotation.ThisIsPeanut;

@PeanutConfiguration
public class TC3_Config {

    @ThisIsPeanut
    public TC3_Layer_3_3 tc3_layer_3_3() {
        return new TC3_Layer_3_3();
    }
}
