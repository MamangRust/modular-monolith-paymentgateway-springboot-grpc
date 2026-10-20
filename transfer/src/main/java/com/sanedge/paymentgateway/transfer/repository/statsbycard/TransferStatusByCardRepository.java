package com.sanedge.paymentgateway.transfer.repository.statsbycard;

import com.sanedge.paymentgateway.transfer.models.TransferMonthStatusSuccess;
import com.sanedge.paymentgateway.transfer.models.Transfer;
import com.sanedge.paymentgateway.transfer.models.TransferMonthStatusFailed;
import com.sanedge.paymentgateway.transfer.models.TransferYearStatusSuccess;
import com.sanedge.paymentgateway.transfer.models.TransferYearStatusFailed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferStatusByCardRepository extends JpaRepository<Transfer, Long> {
    @Query(value = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CAST(:startYear AS CHAR(4)) AS reportYear,
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                    WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                    WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                    WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'success' THEN 1 ELSE 0 END), 0) AS BIGINT) AS totalSuccess,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'success' THEN t.transfer_amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN transfers t
                ON EXTRACT(MONTH FROM t.transfer_time) = m.m
                AND EXTRACT(YEAR FROM t.transfer_time) = :startYear
                AND (t.transfer_from = :cardNumber OR t.transfer_to = :cardNumber)
                AND t.deleted_at IS NULL
            GROUP BY m.m
            ORDER BY m.m
            """, nativeQuery = true)
    List<TransferMonthStatusSuccess> findMonthTransferStatusSuccessByCard(
            @Param("cardNumber") String cardNumber,
            @Param("startYear") Long startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Long endYear,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CAST(:startYear AS CHAR(4)) AS reportYear,
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                    WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                    WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                    WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'failed' THEN 1 ELSE 0 END), 0) AS BIGINT) AS totalFailed,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'failed' THEN t.transfer_amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN transfers t
                ON EXTRACT(MONTH FROM t.transfer_time) = m.m
                AND EXTRACT(YEAR FROM t.transfer_time) = :startYear
                AND (t.transfer_from = :cardNumber OR t.transfer_to = :cardNumber)
                AND t.deleted_at IS NULL
            GROUP BY m.m
            ORDER BY m.m
            """, nativeQuery = true)
    List<TransferMonthStatusFailed> findMonthTransferStatusFailedByCard(
            @Param("cardNumber") String cardNumber,
            @Param("startYear") Long startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Long endYear,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.transfer_time) AS CHAR(4)) AS reportYear,
                    COUNT(*) AS total_success,
                    COALESCE(SUM(t.transfer_amount), 0) AS totalAmount
                FROM transfers t
                WHERE t.deleted_at IS NULL
                  AND t.status = 'success'
                  AND (t.transfer_from = :cardNumber OR t.transfer_to = :cardNumber)
                  AND (EXTRACT(YEAR FROM t.transfer_time) = :year OR EXTRACT(YEAR FROM t.transfer_time) = :year - 1)
                GROUP BY EXTRACT(YEAR FROM t.transfer_time)
            ), formatted_data AS (
                SELECT reportYear, CAST(total_success AS BIGINT) AS totalSuccess, CAST(totalAmount AS BIGINT) AS totalAmount FROM yearly_data
                UNION ALL
                SELECT CAST(:year AS CHAR(4)), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year AS CHAR(4)))
                UNION ALL
                SELECT CAST(:year - 1 AS CHAR(4)), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year - 1 AS CHAR(4)))
            )
            SELECT * FROM formatted_data
            ORDER BY reportYear DESC
            """, nativeQuery = true)
    List<TransferYearStatusSuccess> findYearlyTransferStatusSuccessByCard(
            @Param("cardNumber") String cardNumber,
            @Param("year") Long year);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.transfer_time) AS CHAR(4)) AS reportYear,
                    COUNT(*) AS total_failed,
                    COALESCE(SUM(t.transfer_amount), 0) AS totalAmount
                FROM transfers t
                WHERE t.deleted_at IS NULL
                  AND t.status = 'failed'
                  AND (t.transfer_from = :cardNumber OR t.transfer_to = :cardNumber)
                  AND (EXTRACT(YEAR FROM t.transfer_time) = :year OR EXTRACT(YEAR FROM t.transfer_time) = :year - 1)
                GROUP BY EXTRACT(YEAR FROM t.transfer_time)
            ), formatted_data AS (
                SELECT reportYear, CAST(total_failed AS BIGINT) AS totalFailed, CAST(totalAmount AS BIGINT) AS totalAmount FROM yearly_data
                UNION ALL
                SELECT CAST(:year AS CHAR(4)), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year AS CHAR(4)))
                UNION ALL
                SELECT CAST(:year - 1 AS CHAR(4)), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year - 1 AS CHAR(4)))
            )
            SELECT * FROM formatted_data
            ORDER BY reportYear DESC
            """, nativeQuery = true)
    List<TransferYearStatusFailed> findYearlyTransferStatusFailedByCard(
            @Param("cardNumber") String cardNumber,
            @Param("year") Long year);

}
