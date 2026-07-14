INSERT INTO ref.outbound_event_type (id, code, name, description, sort_order) VALUES
    (1,  'LAB_ORDER_CREATED', 'Lab order created', '', 10),
    (2,  'LAB_RESULT_ACK_CREATED', 'Lab result ack created', '', 20);

INSERT INTO ref.inbound_event_type (id, code, name, source_service, description, sort_order) VALUES
    (1, 'LAB_ORDER_ACK_CREATED',            'Lab order ack created',       'disa', '',                 10),
    (2, 'LAB_RESULT_CREATED',            'Lab result created',       'disa', '',             20);