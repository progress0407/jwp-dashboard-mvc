package nextstep.context;

import nextstep.context.box.PeanutBox;
import nextstep.context.box.PeanutPostProcessor;
import nextstep.context.test_case_4_tx.TestTargetService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PeanutPostProcessorTest {

    private static final Logger log = LoggerFactory.getLogger(PeanutPostProcessorTest.class);

    @Test
    void postProcess() {
        PeanutBox.INSTANCE.init("nextstep.context.post_process_test_case");
        PeanutPostProcessor.INSTANCE.init("nextstep.context.post_process_test_case");
        final TestTargetService proxy = PeanutBox.INSTANCE.findPeanut(TestTargetService.class);

        log.info("method call start !!");
        proxy.somethingDoWithTransactional();
        proxy.somethingDoWithNoTransactional();
        log.info("method call end !!");
    }
}
