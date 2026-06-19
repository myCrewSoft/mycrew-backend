package com.mycrewsoft.perfomance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class MailReadPerformanceTest {

    private static final int DEFAULT_TARGET_COUNT = 2_000;
    private static final int ORACLE_IN_LIMIT = 1_000;

    @Test
    void compareSingleRowLoopWithIndexedBatchMailProcessing() throws Exception {
        assumeTrue(Boolean.getBoolean("mail.performance.enabled"),
                "Run with -Dmail.performance.enabled=true to execute the mail performance benchmark.");

        int targetCount = Integer.getInteger("mail.performance.target-count", DEFAULT_TARGET_COUNT);
        if (targetCount < 1) {
            throw new IllegalArgumentException("mail.performance.target-count must be greater than 0");
        }

        try (Connection connection = DriverManager.getConnection(
                property("mail.performance.db-url", "DB_URL", "jdbc:oracle:thin:@//localhost:1521/XEPDB1"),
                property("mail.performance.db-username", "DB_USERNAME", "final_schema"),
                property("mail.performance.db-password", "DB_PASSWORD", "1234"))) {
            connection.setAutoCommit(false);
            try {
                long empId = selectLong(connection, "SELECT MIN(EMP_ID) FROM TB_EMPLOYEE");
                IdBase idBase = loadIdBase(connection);
                List<Long> mailIds = insertPerformanceRows(connection, empId, idBase, targetCount);
                List<ScenarioResult> results = new ArrayList<>();

                Measurement readSingle = measureSingleRowLoopDeleteLabel(
                        connection, empId, mailIds, "read-single-row-loop", "UNREAD");
                restoreLabelMaps(connection, empId, idBase.unreadLabelId(),
                        idBase.restoreUnreadLabelMapBase(), mailIds);
                Measurement readBatch = measureIndexedBatchDeleteLabel(
                        connection, empId, mailIds, "read-indexed-batch", "UNREAD");
                results.add(new ScenarioResult("mark-read", readSingle, readBatch));

                Measurement importantSingle = measureSingleRowLoopAddLabel(
                        connection, empId, mailIds, idBase.importantSingleLabelMapBase(),
                        "important-single-row-loop", "IMPORTANT");
                deleteLabelMapsByLabelId(connection, empId, idBase.importantLabelId(), mailIds);
                Measurement importantBatch = measureIndexedBatchAddLabel(
                        connection, empId, mailIds, idBase.importantBatchLabelMapBase(),
                        "important-indexed-batch", "IMPORTANT");
                results.add(new ScenarioResult("mark-important", importantSingle, importantBatch));

                Measurement trashSingle = measureSingleRowLoopAddLabel(
                        connection, empId, mailIds, idBase.trashSingleLabelMapBase(),
                        "trash-single-row-loop", "TRASH");
                deleteLabelMapsByLabelId(connection, empId, idBase.trashLabelId(), mailIds);
                Measurement trashBatch = measureIndexedBatchAddLabel(
                        connection, empId, mailIds, idBase.trashBatchLabelMapBase(),
                        "trash-indexed-batch", "TRASH");
                results.add(new ScenarioResult("move-trash", trashSingle, trashBatch));

                Measurement clearTrashSingle = measureSingleRowLoopMarkDeleted(
                        connection, empId, mailIds, "clear-trash-single-row-loop");
                resetDeletedFlag(connection, empId, mailIds);
                Measurement clearTrashBatch = measureIndexedBatchMarkDeleted(
                        connection, empId, mailIds, "clear-trash-indexed-batch");
                results.add(new ScenarioResult("clear-trash", clearTrashSingle, clearTrashBatch));

                printReport(targetCount, results);

                for (ScenarioResult result : results) {
                    assertEquals(targetCount, result.singleRowLoop().affectedRows(), result.scenario());
                    assertEquals(targetCount, result.indexedBatch().affectedRows(), result.scenario());
                }
            } finally {
                connection.rollback();
            }
        }
    }

    private List<Long> insertPerformanceRows(Connection connection, long empId, IdBase idBase, int count)
            throws SQLException {
        insertLabel(connection, idBase.unreadLabelId(), empId, "PERF_UNREAD_" + idBase.runId(), "UNREAD");
        insertLabel(connection, idBase.importantLabelId(), empId, "PERF_IMPORTANT_" + idBase.runId(), "IMPORTANT");
        insertLabel(connection, idBase.trashLabelId(), empId, "PERF_TRASH_" + idBase.runId(), "TRASH");

        List<Long> mailIds = new ArrayList<>(count);
        try (PreparedStatement messageStatement = connection.prepareStatement("""
                INSERT INTO TB_MAIL_MESSAGE (
                    MAIL_MSG_ID, EMP_ID, EXTERNAL_MSG_ID, EXTERNAL_THREAD_ID,
                    MAIL_SJ, SNIPPET, FROM_EMAIL, TO_SUMMARY, MAIL_SNDNG_DT,
                    INTERNAL_DATE, DRAFT_YN, BODY_SYNC_YN, DEL_YN, FRST_REG_DT
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'N', 'Y', 'N', SYSDATE)
                """);
             PreparedStatement labelMapStatement = connection.prepareStatement("""
                INSERT INTO TB_MAIL_LABEL_MAP (
                    MAIL_LABEL_MAP_ID, MAIL_MSG_ID, EMP_ID, MAIL_LABEL_ID
                ) VALUES (?, ?, ?, ?)
                """)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            for (int index = 0; index < count; index++) {
                long mailId = idBase.mailBase() + index;
                long labelMapId = idBase.initialUnreadLabelMapBase() + index;
                mailIds.add(mailId);

                messageStatement.setLong(1, mailId);
                messageStatement.setLong(2, empId);
                messageStatement.setString(3, "perf-msg-" + idBase.runId() + "-" + index);
                messageStatement.setString(4, "perf-thread-" + idBase.runId() + "-" + index);
                messageStatement.setString(5, "mail performance benchmark " + index);
                messageStatement.setString(6, "performance snippet");
                messageStatement.setString(7, "perf.sender@example.com");
                messageStatement.setString(8, "perf.receiver@example.com");
                messageStatement.setTimestamp(9, now);
                messageStatement.setTimestamp(10, now);
                messageStatement.addBatch();

                labelMapStatement.setLong(1, labelMapId);
                labelMapStatement.setLong(2, mailId);
                labelMapStatement.setLong(3, empId);
                labelMapStatement.setLong(4, idBase.unreadLabelId());
                labelMapStatement.addBatch();
            }
            messageStatement.executeBatch();
            labelMapStatement.executeBatch();
        }
        return mailIds;
    }

    private void insertLabel(Connection connection, long labelId, long empId, String externalLabelId, String labelTypeCd)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO TB_MAIL_LABEL (
                    MAIL_LABEL_ID, EMP_ID, EXTERNAL_LABEL_ID, LABEL_NM,
                    SYSTEM_YN, LABEL_TYPE_CD, USE_YN, FRST_REG_DT
                ) VALUES (?, ?, ?, ?, 'Y', ?, 'Y', SYSDATE)
                """)) {
            statement.setLong(1, labelId);
            statement.setLong(2, empId);
            statement.setString(3, externalLabelId);
            statement.setString(4, labelTypeCd);
            statement.setString(5, labelTypeCd);
            statement.executeUpdate();
        }
    }

    private Measurement measureSingleRowLoopDeleteLabel(Connection connection,
                                                       long empId,
                                                       List<Long> mailIds,
                                                       String name,
                                                       String labelTypeCd) throws SQLException {
        int sqlExecutions = 0;
        int affectedRows = 0;
        long start = System.nanoTime();
        try (PreparedStatement selectStatement = selectMailStatement(connection);
             PreparedStatement deleteStatement = singleDeleteLabelStatement(connection)) {
            for (Long mailId : mailIds) {
                if (!selectMail(selectStatement, empId, mailId)) {
                    sqlExecutions++;
                    continue;
                }
                sqlExecutions++;

                deleteStatement.setLong(1, empId);
                deleteStatement.setLong(2, mailId);
                deleteStatement.setString(3, labelTypeCd);
                affectedRows += deleteStatement.executeUpdate();
                sqlExecutions++;
            }
        }
        return new Measurement(name, sqlExecutions, affectedRows, System.nanoTime() - start);
    }

    private Measurement measureIndexedBatchDeleteLabel(Connection connection,
                                                      long empId,
                                                      List<Long> mailIds,
                                                      String name,
                                                      String labelTypeCd) throws SQLException {
        int sqlExecutions = 0;
        int affectedRows = 0;
        long start = System.nanoTime();
        for (List<Long> chunk : chunks(mailIds)) {
            executeBatchSelect(connection, empId, chunk);
            sqlExecutions++;
            affectedRows += executeBatchDeleteLabel(connection, empId, chunk, labelTypeCd);
            sqlExecutions++;
        }
        return new Measurement(name, sqlExecutions, affectedRows, System.nanoTime() - start);
    }

    private Measurement measureSingleRowLoopAddLabel(Connection connection,
                                                    long empId,
                                                    List<Long> mailIds,
                                                    long labelMapBase,
                                                    String name,
                                                    String labelTypeCd) throws SQLException {
        int sqlExecutions = 0;
        int affectedRows = 0;
        long start = System.nanoTime();
        try (PreparedStatement selectStatement = selectMailStatement(connection);
             PreparedStatement labelStatement = selectLabelByTypeStatement(connection);
             PreparedStatement existsStatement = existsLabelMapStatement(connection);
             PreparedStatement insertStatement = insertLabelMapStatement(connection)) {
            for (int index = 0; index < mailIds.size(); index++) {
                Long mailId = mailIds.get(index);
                if (!selectMail(selectStatement, empId, mailId)) {
                    sqlExecutions++;
                    continue;
                }
                sqlExecutions++;

                long labelId = selectLabelId(labelStatement, empId, labelTypeCd);
                sqlExecutions++;

                boolean exists = existsLabelMap(existsStatement, empId, mailId, labelId);
                sqlExecutions++;
                if (exists) {
                    continue;
                }

                insertStatement.setLong(1, labelMapBase + index);
                insertStatement.setLong(2, mailId);
                insertStatement.setLong(3, empId);
                insertStatement.setLong(4, labelId);
                affectedRows += insertStatement.executeUpdate();
                sqlExecutions++;
            }
        }
        return new Measurement(name, sqlExecutions, affectedRows, System.nanoTime() - start);
    }

    private Measurement measureIndexedBatchAddLabel(Connection connection,
                                                   long empId,
                                                   List<Long> mailIds,
                                                   long labelMapBase,
                                                   String name,
                                                   String labelTypeCd) throws SQLException {
        int sqlExecutions = 0;
        int affectedRows = 0;
        long start = System.nanoTime();
        int chunkIndex = 0;
        for (List<Long> chunk : chunks(mailIds)) {
            executeBatchSelect(connection, empId, chunk);
            sqlExecutions++;
            affectedRows += executeBatchInsertLabel(connection, empId, chunk,
                    labelMapBase + ((long) chunkIndex * ORACLE_IN_LIMIT), labelTypeCd);
            sqlExecutions++;
            chunkIndex++;
        }
        return new Measurement(name, sqlExecutions, affectedRows, System.nanoTime() - start);
    }

    private Measurement measureSingleRowLoopMarkDeleted(Connection connection,
                                                       long empId,
                                                       List<Long> mailIds,
                                                       String name) throws SQLException {
        int sqlExecutions = 0;
        int affectedRows = 0;
        long start = System.nanoTime();
        try (PreparedStatement selectStatement = singleSelectTrashMailStatement(connection);
             PreparedStatement updateStatement = connection.prepareStatement("""
                UPDATE TB_MAIL_MESSAGE
                SET DEL_YN = 'Y',
                    LAST_MDFCN_DT = SYSDATE
                WHERE EMP_ID = ?
                  AND MAIL_MSG_ID = ?
                  AND DEL_YN = 'N'
                """)) {
            for (Long mailId : mailIds) {
                if (!selectTrashMail(selectStatement, empId, mailId)) {
                    sqlExecutions++;
                    continue;
                }
                sqlExecutions++;

                updateStatement.setLong(1, empId);
                updateStatement.setLong(2, mailId);
                affectedRows += updateStatement.executeUpdate();
                sqlExecutions++;
            }
        }
        return new Measurement(name, sqlExecutions, affectedRows, System.nanoTime() - start);
    }

    private Measurement measureIndexedBatchMarkDeleted(Connection connection,
                                                      long empId,
                                                      List<Long> mailIds,
                                                      String name) throws SQLException {
        int sqlExecutions = 0;
        int affectedRows = 0;
        long start = System.nanoTime();
        for (List<Long> chunk : chunks(mailIds)) {
            executeBatchSelectTrashRows(connection, empId, chunk);
            sqlExecutions++;
            affectedRows += executeBatchMarkDeleted(connection, empId, chunk);
            sqlExecutions++;
        }
        return new Measurement(name, sqlExecutions, affectedRows, System.nanoTime() - start);
    }

    private PreparedStatement selectMailStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("""
                SELECT MAIL_MSG_ID, EXTERNAL_MSG_ID
                FROM TB_MAIL_MESSAGE
                WHERE EMP_ID = ?
                  AND MAIL_MSG_ID = ?
                  AND DEL_YN = 'N'
                """);
    }

    private PreparedStatement selectLabelByTypeStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("""
                SELECT MAIL_LABEL_ID
                FROM TB_MAIL_LABEL
                WHERE EMP_ID = ?
                  AND TRIM(LABEL_TYPE_CD) = ?
                  AND USE_YN = 'Y'
                """);
    }

    private PreparedStatement existsLabelMapStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("""
                SELECT 1
                FROM TB_MAIL_LABEL_MAP
                WHERE EMP_ID = ?
                  AND MAIL_MSG_ID = ?
                  AND MAIL_LABEL_ID = ?
                """);
    }

    private PreparedStatement insertLabelMapStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("""
                INSERT INTO TB_MAIL_LABEL_MAP (
                    MAIL_LABEL_MAP_ID, MAIL_MSG_ID, EMP_ID, MAIL_LABEL_ID
                ) VALUES (?, ?, ?, ?)
                """);
    }

    private PreparedStatement singleDeleteLabelStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("""
                DELETE FROM TB_MAIL_LABEL_MAP LM
                WHERE LM.EMP_ID = ?
                  AND LM.MAIL_MSG_ID = ?
                  AND EXISTS (
                      SELECT 1
                      FROM TB_MAIL_LABEL L
                      WHERE L.MAIL_LABEL_ID = LM.MAIL_LABEL_ID
                        AND L.EMP_ID = LM.EMP_ID
                        AND TRIM(L.LABEL_TYPE_CD) = ?
                  )
                """);
    }

    private PreparedStatement singleSelectTrashMailStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("""
                SELECT M.MAIL_MSG_ID
                FROM TB_MAIL_MESSAGE M
                WHERE M.EMP_ID = ?
                  AND M.MAIL_MSG_ID = ?
                  AND M.DEL_YN = 'N'
                  AND EXISTS (
                      SELECT 1
                      FROM TB_MAIL_LABEL_MAP LM
                      JOIN TB_MAIL_LABEL L
                        ON L.MAIL_LABEL_ID = LM.MAIL_LABEL_ID
                       AND L.EMP_ID = LM.EMP_ID
                      WHERE LM.EMP_ID = M.EMP_ID
                        AND LM.MAIL_MSG_ID = M.MAIL_MSG_ID
                        AND TRIM(L.LABEL_TYPE_CD) = 'TRASH'
                        AND L.USE_YN = 'Y'
                  )
                """);
    }

    private boolean selectMail(PreparedStatement statement, long empId, long mailId) throws SQLException {
        statement.setLong(1, empId);
        statement.setLong(2, mailId);
        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
        }
    }

    private long selectLabelId(PreparedStatement statement, long empId, String labelTypeCd) throws SQLException {
        statement.setLong(1, empId);
        statement.setString(2, labelTypeCd);
        try (ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Label not found: " + labelTypeCd);
            }
            return resultSet.getLong(1);
        }
    }

    private boolean existsLabelMap(PreparedStatement statement, long empId, long mailId, long labelId)
            throws SQLException {
        statement.setLong(1, empId);
        statement.setLong(2, mailId);
        statement.setLong(3, labelId);
        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
        }
    }

    private boolean selectTrashMail(PreparedStatement statement, long empId, long mailId) throws SQLException {
        statement.setLong(1, empId);
        statement.setLong(2, mailId);
        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
        }
    }

    private void executeBatchSelect(Connection connection, long empId, List<Long> mailIds)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT MAIL_MSG_ID, EXTERNAL_MSG_ID
                FROM TB_MAIL_MESSAGE
                WHERE EMP_ID = ?
                  AND DEL_YN = 'N'
                  AND MAIL_MSG_ID IN (%s)
                """.formatted(placeholders(mailIds.size())))) {
            statement.setLong(1, empId);
            bindMailIds(statement, 2, mailIds);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultSet.getLong(1);
                }
            }
        }
    }

    private int executeBatchDeleteLabel(Connection connection, long empId, List<Long> mailIds, String labelTypeCd)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM TB_MAIL_LABEL_MAP LM
                WHERE LM.EMP_ID = ?
                  AND LM.MAIL_MSG_ID IN (%s)
                  AND LM.MAIL_LABEL_ID IN (
                      SELECT L.MAIL_LABEL_ID
                      FROM TB_MAIL_LABEL L
                      WHERE L.EMP_ID = ?
                        AND TRIM(L.LABEL_TYPE_CD) = ?
                        AND L.USE_YN = 'Y'
                  )
                """.formatted(placeholders(mailIds.size())))) {
            statement.setLong(1, empId);
            int nextIndex = bindMailIds(statement, 2, mailIds);
            statement.setLong(nextIndex++, empId);
            statement.setString(nextIndex, labelTypeCd);
            return statement.executeUpdate();
        }
    }

    private int executeBatchInsertLabel(Connection connection,
                                        long empId,
                                        List<Long> mailIds,
                                        long labelMapBase,
                                        String labelTypeCd) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO TB_MAIL_LABEL_MAP (
                    MAIL_LABEL_MAP_ID, MAIL_MSG_ID, EMP_ID, MAIL_LABEL_ID
                )
                SELECT
                    ? + ROW_NUMBER() OVER (ORDER BY S.MAIL_MSG_ID) - 1,
                    S.MAIL_MSG_ID,
                    ?,
                    L.MAIL_LABEL_ID
                FROM (
                    %s
                ) S
                JOIN TB_MAIL_LABEL L
                  ON L.EMP_ID = ?
                 AND TRIM(L.LABEL_TYPE_CD) = ?
                 AND L.USE_YN = 'Y'
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM TB_MAIL_LABEL_MAP LM
                    WHERE LM.EMP_ID = ?
                      AND LM.MAIL_MSG_ID = S.MAIL_MSG_ID
                      AND LM.MAIL_LABEL_ID = L.MAIL_LABEL_ID
                )
                """.formatted(dualMailIds(mailIds.size())))) {
            statement.setLong(1, labelMapBase);
            statement.setLong(2, empId);
            int nextIndex = bindMailIds(statement, 3, mailIds);
            statement.setLong(nextIndex++, empId);
            statement.setString(nextIndex++, labelTypeCd);
            statement.setLong(nextIndex, empId);
            return statement.executeUpdate();
        }
    }

    private void executeBatchSelectTrashRows(Connection connection, long empId, List<Long> mailIds)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT M.MAIL_MSG_ID
                FROM TB_MAIL_MESSAGE M
                WHERE M.EMP_ID = ?
                  AND M.DEL_YN = 'N'
                  AND M.MAIL_MSG_ID IN (%s)
                  AND EXISTS (
                      SELECT 1
                      FROM TB_MAIL_LABEL_MAP LM
                      JOIN TB_MAIL_LABEL L
                        ON L.MAIL_LABEL_ID = LM.MAIL_LABEL_ID
                       AND L.EMP_ID = LM.EMP_ID
                      WHERE LM.EMP_ID = M.EMP_ID
                        AND LM.MAIL_MSG_ID = M.MAIL_MSG_ID
                        AND TRIM(L.LABEL_TYPE_CD) = 'TRASH'
                        AND L.USE_YN = 'Y'
                  )
                """.formatted(placeholders(mailIds.size())))) {
            statement.setLong(1, empId);
            bindMailIds(statement, 2, mailIds);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultSet.getLong(1);
                }
            }
        }
    }

    private int executeBatchMarkDeleted(Connection connection, long empId, List<Long> mailIds)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE TB_MAIL_MESSAGE M
                SET M.DEL_YN = 'Y',
                    M.LAST_MDFCN_DT = SYSDATE
                WHERE M.EMP_ID = ?
                  AND M.MAIL_MSG_ID IN (%s)
                """.formatted(placeholders(mailIds.size())))) {
            statement.setLong(1, empId);
            bindMailIds(statement, 2, mailIds);
            return statement.executeUpdate();
        }
    }

    private void restoreLabelMaps(Connection connection, long empId, long labelId, long labelMapBase,
                                  List<Long> mailIds) throws SQLException {
        try (PreparedStatement statement = insertLabelMapStatement(connection)) {
            for (int index = 0; index < mailIds.size(); index++) {
                statement.setLong(1, labelMapBase + index);
                statement.setLong(2, mailIds.get(index));
                statement.setLong(3, empId);
                statement.setLong(4, labelId);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void deleteLabelMapsByLabelId(Connection connection, long empId, long labelId, List<Long> mailIds)
            throws SQLException {
        for (List<Long> chunk : chunks(mailIds)) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    DELETE FROM TB_MAIL_LABEL_MAP
                    WHERE EMP_ID = ?
                      AND MAIL_LABEL_ID = ?
                      AND MAIL_MSG_ID IN (%s)
                    """.formatted(placeholders(chunk.size())))) {
                statement.setLong(1, empId);
                statement.setLong(2, labelId);
                bindMailIds(statement, 3, chunk);
                statement.executeUpdate();
            }
        }
    }

    private void resetDeletedFlag(Connection connection, long empId, List<Long> mailIds) throws SQLException {
        for (List<Long> chunk : chunks(mailIds)) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE TB_MAIL_MESSAGE
                    SET DEL_YN = 'N',
                        LAST_MDFCN_DT = SYSDATE
                    WHERE EMP_ID = ?
                      AND MAIL_MSG_ID IN (%s)
                    """.formatted(placeholders(chunk.size())))) {
                statement.setLong(1, empId);
                bindMailIds(statement, 2, chunk);
                statement.executeUpdate();
            }
        }
    }

    private IdBase loadIdBase(Connection connection) throws SQLException {
        long runId = System.currentTimeMillis();
        long mailBase = selectLong(connection, "SELECT NVL(MAX(MAIL_MSG_ID), 0) + 100000 FROM TB_MAIL_MESSAGE");
        long labelBase = selectLong(connection, "SELECT NVL(MAX(MAIL_LABEL_ID), 0) + 100000 FROM TB_MAIL_LABEL");
        long labelMapBase = selectLong(connection, "SELECT NVL(MAX(MAIL_LABEL_MAP_ID), 0) + 100000 FROM TB_MAIL_LABEL_MAP");
        return new IdBase(
                runId,
                mailBase,
                labelBase,
                labelBase + 1,
                labelBase + 2,
                labelMapBase,
                labelMapBase + 1_000_000L,
                labelMapBase + 2_000_000L,
                labelMapBase + 3_000_000L,
                labelMapBase + 4_000_000L,
                labelMapBase + 5_000_000L);
    }

    private long selectLong(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new IllegalStateException("No result for SQL: " + sql);
            }
            return resultSet.getLong(1);
        }
    }

    private List<List<Long>> chunks(List<Long> mailIds) {
        List<List<Long>> chunks = new ArrayList<>();
        for (int start = 0; start < mailIds.size(); start += ORACLE_IN_LIMIT) {
            int end = Math.min(start + ORACLE_IN_LIMIT, mailIds.size());
            chunks.add(mailIds.subList(start, end));
        }
        return chunks;
    }

    private String placeholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }

    private String dualMailIds(int count) {
        return String.join(" UNION ALL ", Collections.nCopies(count, "SELECT ? AS MAIL_MSG_ID FROM DUAL"));
    }

    private int bindMailIds(PreparedStatement statement, int startIndex, List<Long> mailIds) throws SQLException {
        int parameterIndex = startIndex;
        for (Long mailId : mailIds) {
            statement.setLong(parameterIndex++, mailId);
        }
        return parameterIndex;
    }

    private void printReport(int targetCount, List<ScenarioResult> results) {
        System.out.println();
        System.out.println("===== Mail Performance Benchmark =====");
        System.out.println("targetCount=" + targetCount);
        for (ScenarioResult result : results) {
            printScenario(result);
        }
        System.out.println("======================================");
    }

    private void printScenario(ScenarioResult result) {
        double improvement = result.singleRowLoop().elapsedMillis() / result.indexedBatch().elapsedMillis();
        double reduction = 100.0 * (result.singleRowLoop().sqlExecutions() - result.indexedBatch().sqlExecutions())
                / result.singleRowLoop().sqlExecutions();

        System.out.println();
        System.out.println("scenario=" + result.scenario());
        printMeasurement(result.singleRowLoop());
        printMeasurement(result.indexedBatch());
        System.out.printf(Locale.ROOT, "elapsedImprovement=%.2fx%n", improvement);
        System.out.printf(Locale.ROOT, "sqlExecutionReduction=%.2f%%%n", reduction);
    }

    private void printMeasurement(Measurement measurement) {
        System.out.printf(Locale.ROOT,
                "%s: elapsed=%.3f ms, sqlExecutions=%d, affectedRows=%d%n",
                measurement.name(),
                measurement.elapsedMillis(),
                measurement.sqlExecutions(),
                measurement.affectedRows());
    }

    private String property(String propertyName, String envName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return defaultValue;
    }

    private record IdBase(long runId,
                          long mailBase,
                          long unreadLabelId,
                          long importantLabelId,
                          long trashLabelId,
                          long initialUnreadLabelMapBase,
                          long restoreUnreadLabelMapBase,
                          long importantSingleLabelMapBase,
                          long importantBatchLabelMapBase,
                          long trashSingleLabelMapBase,
                          long trashBatchLabelMapBase) {
    }

    private record ScenarioResult(String scenario, Measurement singleRowLoop, Measurement indexedBatch) {
    }

    private record Measurement(String name, int sqlExecutions, int affectedRows, long elapsedNanos) {
        double elapsedMillis() {
            return elapsedNanos / 1_000_000.0;
        }
    }
}
