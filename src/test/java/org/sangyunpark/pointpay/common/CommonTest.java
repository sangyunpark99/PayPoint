package org.sangyunpark.pointpay.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public abstract class CommonTest {

    @Autowired
    protected DbCleaner dbCleaner;
}
