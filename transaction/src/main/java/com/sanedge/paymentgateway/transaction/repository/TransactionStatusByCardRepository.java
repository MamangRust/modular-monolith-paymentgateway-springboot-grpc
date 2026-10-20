package com.sanedge.paymentgateway.transaction.repository;

import com.sanedge.paymentgateway.transaction.models.TransactionMonthStatusSuccess;
import com.sanedge.paymentgateway.transaction.models.Transaction;
import com.sanedge.paymentgateway.transaction.models.TransactionMonthStatusFailed;
import com.sanedge.paymentgateway.transaction.models.TransactionYearStatusSuccess;
import com.sanedge.paymentgateway.transaction.models.TransactionYearStatusFailed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionStatusByCardRepository extends JpaRepository<Transaction, Long> {
    @Query(value = """
            WITH months AS (
                SELECT 1 AS mon UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CAST(:startYear AS VARCHAR) AS reportYear,
                CASE m.mon
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' WHEN 4 THEN 'Apr'
                    WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug'
                    WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'success' THEN 1 ELSE 0 END), 0) AS BIGINT) AS totalSuccess,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'success' THEN t.amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN transactions t
                ON EXTRACT(MONTH FROM t.transaction_time) = m.mon
                AND EXTRACT(YEAR FROM t.transaction_time) = :startYear
                AND t.card_number = :cardNumber
                AND t.deleted_at IS NULL
            GROUP BY m.mon
            ORDER BY m.mon
            """, nativeQuery = true)
    List<TransactionMonthStatusSuccess> findMonthTransactionStatusSuccessByCard(
            @Param("cardNumber") String cardNumber,
            @Param("startYear") Long startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Long endYear,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            WITH months AS (
                SELECT 1 AS mon UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CAST(:startYear AS VARCHAR) AS reportYear,
                CASE m.mon
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' WHEN 4 THEN 'Apr'
                    WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug'
                    WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'failed' THEN 1 ELSE 0 END), 0) AS BIGINT) AS totalFailed,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'failed' THEN t.amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN transactions t
                ON EXTRACT(MONTH FROM t.transaction_time) = m.mon
                AND EXTRACT(YEAR FROM t.transaction_time) = :startYear
                AND t.card_number = :cardNumber
                AND t.deleted_at IS NULL
            GROUP BY m.mon
            ORDER BY m.mon
            """, nativeQuery = true)
    List<TransactionMonthStatusFailed> findMonthTransactionStatusFailedByCard(
            @Param("cardNumber") String cardNumber,
            @Param("startYear") Long startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Long endYear,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.transaction_time) AS VARCHAR) AS reportYear,
                    CAST(COUNT(*) AS BIGINT) AS totalSuccess,
                    CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS totalAmount
                FROM transactions t
                WHERE t.deleted_at IS NULL
                  AND t.status = 'success'
                  AND t.card_number = :cardNumber
                  AND (EXTRACT(YEAR FROM t.transaction_time) = :year OR EXTRACT(YEAR FROM t.transaction_time) = :year - 1)
                GROUP BY EXTRACT(YEAR FROM t.transaction_time)
            ), formatted_data AS (
                SELECT reportYear, totalSuccess, totalAmount FROM yearly_data
                UNION ALL
                SELECT CAST(:year AS VARCHAR), 0, 0
                WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year AS VARCHAR))
                UNION ALL
                SELECT CAST(:year - 1 AS VARCHAR), 0, 0
                WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year - 1 AS VARCHAR))
            )
            SELECT * FROM formatted_data
            ORDER BY reportYear DESC
            """, nativeQuery = true)
    List<TransactionYearStatusSuccess> findYearlyTransactionStatusSuccessByCard(
            @Param("cardNumber") String cardNumber,
            @Param("year") Long year);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.transaction_time) AS VARCHAR) AS reportYear,
                    CAST(COUNT(*) AS BIGINT) AS totalFailed,
                    CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS totalAmount
                FROM transactions t
                WHERE t.deleted_at IS NULL
                  AND t.status = 'failed'
                  AND t.card_number = :cardNumber
                  AND (EXTRACT(YEAR FROM t.transaction_time) = :year OR EXTRACT(YEAR FROM t.transaction_time) = :year - 1)
                GROUP BY EXTRACT(YEAR FROM t.transaction_time)
            ), formatted_data AS (
                SELECT reportYear, totalFailed, totalAmount FROM yearly_data
                UNION ALL
                SELECT CAST(:year AS VARCHAR), 0, 0
                WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year AS VARCHAR))
                UNION ALL
                SELECT CAST(:year - 1 AS VARCHAR), 0, 0
                WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year - 1 AS VARCHAR))
            )
            SELECT * FROM formatted_data
            ORDER BY reportYear DESC
            """, nativeQuery = true)
    List<TransactionYearStatusFailed> findYearlyTransactionStatusFailedByCard(
            @Param("cardNumber") String cardNumber,
            @Param("year") Long year);
}
