package nextstep.context.test_case_3;

import nextstep.web.annotation.ImPeanut;

@ImPeanut
public class TC3_Layer_2_2 implements TC3_ILayer_2_2 {

    private final TC3_Layer_3_3 tc3_layer_3_3;

    public TC3_Layer_2_2(TC3_Layer_3_3 tc3_layer_3_3) {
        this.tc3_layer_3_3 = tc3_layer_3_3;
    }
}
