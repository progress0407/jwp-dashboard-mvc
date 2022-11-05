package nextstep.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import nextstep.context.test_case_1.TC1_Layer_1_1;
import nextstep.context.test_case_1.TC1_Layer_1_2;
import nextstep.context.test_case_1.TC1_Layer_1_3;
import nextstep.context.test_case_1.TC1_Layer_2_1;
import nextstep.context.test_case_1.TC1_Layer_2_2;
import nextstep.context.test_case_1.TC1_Layer_2_3;
import nextstep.context.test_case_1.TC1_Layer_3_1;
import nextstep.context.test_case_2.InMemoryUserRepository;
import nextstep.context.test_case_2.UserController;
import nextstep.context.test_case_2.UserService;
import nextstep.context.test_case_3.TC3_Layer_1;
import nextstep.context.test_case_3.TC3_Layer_2_1;
import nextstep.context.test_case_3.TC3_Layer_2_2;
import nextstep.context.test_case_3.TC3_Layer_3_1;
import nextstep.context.test_case_3.TC3_Layer_3_2;
import nextstep.context.test_case_3.TC3_Layer_3_3;
import nextstep.context.test_case_3.TC3_Layer_4;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PeanutBoxTest {

    @AfterEach
    void tearDown() {
        PeanutBox.INSTANCE.clear();
    }

    @Test
    void getPeanut() {
        PeanutBox.INSTANCE.init("nextstep.context.test_case_1");
        assertAll(
                () -> assert_peanut_contains(TC1_Layer_1_1.class),
                () -> assert_peanut_contains(TC1_Layer_1_2.class),
                () -> assert_peanut_contains(TC1_Layer_1_3.class),
                () -> assert_peanut_contains(TC1_Layer_2_1.class),
                () -> assert_peanut_contains(TC1_Layer_2_2.class),
                () -> assert_peanut_contains(TC1_Layer_2_3.class),
                () -> assert_peanut_contains(TC1_Layer_3_1.class)
        );
    }

    @Test
    void getPeanut_2() {
        PeanutBox.INSTANCE.init("nextstep.context.test_case_2");

        final UserController userController = PeanutBox.INSTANCE.findPeanut(UserController.class);

        assertAll(
                () -> assert_peanut_contains(UserService.class),
                () -> assert_peanut_contains(InMemoryUserRepository.class),
                () -> assert_peanut_contains(UserController.class),
                () -> assert_peanut_contains(ObjectMapper.class),
                () -> assertThat(userController.getUserService()).isExactlyInstanceOf(UserService.class),
                () -> assertThat(userController.getObjectMapper()).isExactlyInstanceOf(ObjectMapper.class)
        );
    }

    @Test
    void getPeanut_3() {
        PeanutBox.INSTANCE.init("nextstep.context.test_case_3");
        assertAll(
                () -> assert_peanut_contains(TC3_Layer_1.class),
                () -> assert_peanut_contains(TC3_Layer_2_1.class),
                () -> assert_peanut_contains(TC3_Layer_2_2.class),
                () -> assert_peanut_contains(TC3_Layer_3_1.class),
                () -> assert_peanut_contains(TC3_Layer_3_2.class),
                () -> assert_peanut_contains(TC3_Layer_3_3.class),
                () -> assert_peanut_contains(TC3_Layer_4.class)
        );
    }

    private void assert_peanut_contains(final Class<?> clazz) {

        assertThat(PeanutBox.INSTANCE.findPeanut(clazz)).isInstanceOf(clazz);
    }
}
