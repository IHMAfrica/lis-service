CREATE INDEX ix_order_status_mfl_created
    ON lab.order_status (mfl_code, created_at);

CREATE INDEX ix_order_ack_status_mfl_created
    ON lab.order_ack_status (receiving_facility_mfl_code, created_at);

CREATE INDEX ix_lab_result_mfl_received
    ON lab.lab_result (ordering_mfl_code, received_at);
