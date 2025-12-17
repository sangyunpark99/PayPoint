CREATE INDEX idx_point_history_status
    ON point_history(point_status);

CREATE INDEX idx_point_history_user_history_date ON point_history(user_id, history_date);
