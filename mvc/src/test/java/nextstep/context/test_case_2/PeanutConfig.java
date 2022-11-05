package nextstep.context.test_case_2;

import com.fasterxml.jackson.databind.ObjectMapper;
import nextstep.web.annotation.PeanutConfiguration;
import nextstep.web.annotation.ThisIsPeanut;

@PeanutConfiguration
public class PeanutConfig {

    @ThisIsPeanut
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
