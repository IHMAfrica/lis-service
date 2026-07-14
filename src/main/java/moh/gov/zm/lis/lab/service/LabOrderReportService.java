package moh.gov.zm.lis.lab.service;

import io.r2dbc.spi.Readable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel export of a facility's lab orders joined to their acknowledgement and
 * current result, with the order-to-result turn-around time (TAT). Each order is
 * the driving row (LEFT-joined to at most one ack and one current result via
 * LATERAL sub-queries), so orders still without an ack or a result are included
 * with those columns blank. The join runs as a single database round-trip and the
 * (blocking) workbook assembly is pushed to a bounded-elastic scheduler.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabOrderReportService {
    private static final String[] HEADERS = {
            "order_id", "order_date", "order_time", "lab_code", "test_loinc",
            "order_ack_date", "order_ack_time", "ack_code", "text_message",
            "result_received_at", "result_status", "message_kind", "Tat"
    };
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String REPORT_SQL = """
            SELECT
                os.order_id,
                os.order_date,
                os.order_time,
                os.lab_code,
                os.test_loinc,
                oa.order_ack_date,
                oa.order_ack_time,
                oa.ack_code,
                oa.text_message,
                lr.received_at AS result_received_at,
                lr.result_status,
                lr.message_kind,
                CASE
                    WHEN lr.received_at IS NOT NULL AND os.order_date IS NOT NULL
                    THEN EXTRACT(EPOCH FROM (
                            lr.received_at
                            - ((os.order_date + COALESCE(os.order_time, TIME '00:00')) AT TIME ZONE 'UTC')))
                END AS tat_seconds
            FROM lab.order_status os
            LEFT JOIN LATERAL (
                SELECT a.order_ack_date, a.order_ack_time, a.ack_code, a.text_message
                FROM lab.order_ack_status a
                WHERE a.ref_message_control_id = os.message_control_id
                ORDER BY a.created_at DESC
                LIMIT 1
            ) oa ON TRUE
            LEFT JOIN LATERAL (
                SELECT r.received_at, r.result_status, r.message_kind
                FROM lab.lab_result r
                WHERE r.order_status_id = os.id AND r.is_current = TRUE
                ORDER BY r.received_at DESC
                LIMIT 1
            ) lr ON TRUE
            WHERE os.mfl_code = :mfl
            """;

    private final DatabaseClient databaseClient;

    public Mono<byte[]> generate(String mflCode, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder(REPORT_SQL);
        if (from != null) {
            sql.append(" AND os.created_at >= :from");
        }
        if (to != null) {
            sql.append(" AND os.created_at < :toExclusive");
        }
        sql.append(" ORDER BY os.created_at DESC");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString()).bind("mfl", mflCode);
        if (from != null) {
            spec = spec.bind("from", from.atStartOfDay().atOffset(ZoneOffset.UTC));
        }
        if (to != null) {
            spec = spec.bind("toExclusive", to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC));
        }

        return spec.map(LabOrderReportService::toRow).all()
                .collectList()
                .flatMap(rows -> Mono.fromCallable(() -> buildWorkbook(rows))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private static ReportRow toRow(Readable row) {
        return new ReportRow(
                row.get("order_id", String.class),
                row.get("order_date", LocalDate.class),
                row.get("order_time", LocalTime.class),
                row.get("lab_code", String.class),
                row.get("test_loinc", String.class),
                row.get("order_ack_date", LocalDate.class),
                row.get("order_ack_time", LocalTime.class),
                row.get("ack_code", String.class),
                row.get("text_message", String.class),
                row.get("result_received_at", OffsetDateTime.class),
                row.get("result_status", String.class),
                row.get("message_kind", String.class),
                row.get("tat_seconds", BigDecimal.class));
    }

    private byte[] buildWorkbook(List<ReportRow> rows) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Lab orders");

            XSSFCellStyle headerStyle = wb.createCellStyle();
            XSSFFont bold = wb.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            Row header = sheet.createRow(0);
            for (int c = 0; c < HEADERS.length; c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(HEADERS[c]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(c, 20 * 256);
            }
            sheet.createFreezePane(0, 1);

            int r = 1;
            for (ReportRow row : rows) {
                Row dataRow = sheet.createRow(r++);
                setCell(dataRow, 0, row.orderId());
                setCell(dataRow, 1, format(row.orderDate(), DATE));
                setCell(dataRow, 2, format(row.orderTime(), TIME));
                setCell(dataRow, 3, row.labCode());
                setCell(dataRow, 4, row.testLoinc());
                setCell(dataRow, 5, format(row.orderAckDate(), DATE));
                setCell(dataRow, 6, format(row.orderAckTime(), TIME));
                setCell(dataRow, 7, row.ackCode());
                setCell(dataRow, 8, row.textMessage());
                setCell(dataRow, 9, row.resultReceivedAt() == null ? null
                        : row.resultReceivedAt().atZoneSameInstant(ZoneOffset.UTC).format(TIMESTAMP));
                setCell(dataRow, 10, row.resultStatus());
                setCell(dataRow, 11, row.messageKind());
                setCell(dataRow, 12, formatTat(row.tatSeconds()));
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    private static void setCell(Row row, int column, String value) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private static String format(java.time.temporal.TemporalAccessor value, DateTimeFormatter fmt) {
        return value == null ? null : fmt.format(value);
    }

    /** TAT rendered as "Xd Yh" (whole days + whole hours). Null when there is no result yet. */
    private static String formatTat(BigDecimal seconds) {
        if (seconds == null) {
            return null;
        }
        long total = seconds.longValue();
        boolean negative = total < 0;
        long abs = Math.abs(total);
        long days = abs / 86_400;
        long hours = (abs % 86_400) / 3_600;
        return (negative ? "-" : "") + days + "d " + hours + "h";
    }

    private record ReportRow(
            String orderId,
            LocalDate orderDate,
            LocalTime orderTime,
            String labCode,
            String testLoinc,
            LocalDate orderAckDate,
            LocalTime orderAckTime,
            String ackCode,
            String textMessage,
            OffsetDateTime resultReceivedAt,
            String resultStatus,
            String messageKind,
            BigDecimal tatSeconds) {
    }
}
