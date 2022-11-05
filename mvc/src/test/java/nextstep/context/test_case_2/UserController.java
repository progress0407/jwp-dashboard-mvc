package nextstep.context.test_case_2;

import com.fasterxml.jackson.databind.ObjectMapper;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.GiveMePeanut;

@Controller
public class UserController {

    @GiveMePeanut
    private UserService userService;

    @GiveMePeanut
    private ObjectMapper objectMapper;

    public UserService getUserService() {
        return userService;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
