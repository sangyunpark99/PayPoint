package org.sangyunpark.pointpay.domain.point.repository;

import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PointHistoryJdbcRepository {

    private static final int BATCH_SIZE = 10_000;
    private final JdbcTemplate jdbcTemplate;

    public PointHistoryJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void batchInsert(List<PointHistory> histories) {

        String sql = """
               
                INSERT INTO point_history
                (created_at, balance_before, balance_after, point_type, user_id, history_date)
                VALUES (?,?,?,?,?,?)
                """;

        jdbcTemplate.batchUpdate(
                sql,
                histories,
                BATCH_SIZE,
                (ps, history) -> {
                    ps.setObject(1, history.getCreatedAt());
                    ps.setLong(2, history.getBalanceBefore());
                    ps.setLong(3, history.getBalanceAfter());
                    ps.setString(4, history.getPointType().name());
                    ps.setLong(5, history.getUser().getId());
                    ps.setObject(6, history.getCreatedAt().toLocalDate());
                }
        );
    }
}
