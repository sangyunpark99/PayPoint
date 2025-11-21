package org.sangyunpark.pointpay.config.db;

import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RoutingDataSourceTest {

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private DataSource dataSource;

    @Test
    @Transactional(readOnly = true)
    void givenReadOnlyTransaction_whenSelectingUser_thenUseSlaveDataSource() throws Exception {
        userRepository.findById(1L);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        String url = connection.getMetaData().getURL();
        assertThat(url)
                .contains("3307")
                .doesNotContain("3306");
    }

    @Test
    void givenDefaultTransaction_whenSelectingUser_thenUserMasterDataSource() throws Exception {
        userRepository.findById(1L);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        String url = connection.getMetaData().getURL();
        assertThat(url)
                .contains("3306")
                .doesNotContain("3307");
    }
}