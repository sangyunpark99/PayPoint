package org.sangyunpark.pointpay.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DbCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DbCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clear() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        jdbcTemplate.execute("TRUNCATE TABLE order_item");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE products");
        jdbcTemplate.execute("TRUNCATE TABLE users");

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }
}