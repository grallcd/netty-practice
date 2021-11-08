package com.grallcd;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @since 2021/11/5
 */
@Slf4j
public class LogTest {

    @Test
    void testLogLevel() {

        log.trace("trace log!");
        log.debug("debug log!");
        log.info("info log!");
        log.warn("warn log!");
        log.error("error log!");

    }

}
