package org.sangyunpark.pointpay.domain.point.repository;

import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PointHistoryJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public PointHistoryJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void batchInsert(List<PointHistory> histories) {
        String sql = """
                INSERT INTO point_history
                    (created_at, point_balance, point_status, point_type, user_id)
                VALUES(NOW(),?,?,?,?)
                """;

        jdbcTemplate.batchUpdate(
                sql,
                histories,
                10000,
                (ps, history) -> {
                    ps.setInt(1, history.getPointBalance());
                    ps.setString(2, history.getPointStatus().name());
                    ps.setString(3, history.getPointType().name());
                    ps.setLong(4, history.getUser().getId());
                }
        );
    }
}
