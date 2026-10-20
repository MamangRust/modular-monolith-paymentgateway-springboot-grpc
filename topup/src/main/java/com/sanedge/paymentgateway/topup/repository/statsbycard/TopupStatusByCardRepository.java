package com.sanedge.paymentgateway.topup.repository.statsbycard;

import com.sanedge.paymentgateway.topup.models.TopupMonthStatusSuccess;
import com.sanedge.paymentgateway.topup.models.Topup;
import com.sanedge.paymentgateway.topup.models.TopupMonthStatusFailed;
import com.sanedge.paymentgateway.topup.models.TopupYearStatusSuccess;
import com.sanedge.paymentgateway.topup.models.TopupYearStatusFailed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopupStatusByCardRepository extends JpaRepository<Topup, Long> {
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
                CAST(COALESCE(SUM(CASE WHEN t.status = 'success' THEN t.topup_amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN topups t
                ON EXTRACT(MONTH FROM t.topup_time) = m.mon
                AND EXTRACT(YEAR FROM t.topup_time) = :startYear
                AND t.card_number = :cardNumber
                AND t.deleted_at IS NULL
            GROUP BY m.mon
            ORDER BY m.mon
            """, nativeQuery = true)
    List<TopupMonthStatusSuccess> findMonthTopupStatusSuccessByCard(
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
                CAST(COALESCE(SUM(CASE WHEN t.status = 'failed' THEN t.topup_amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN topups t
                ON EXTRACT(MONTH FROM t.topup_time) = m.mon
                AND EXTRACT(YEAR FROM t.topup_time) = :startYear
                AND t.card_number = :cardNumber
                AND t.deleted_at IS NULL
            GROUP BY m.mon
            ORDER BY m.mon
            """, nativeQuery = true)
    List<TopupMonthStatusFailed> findMonthTopupStatusFailedByCard(
            @Param("cardNumber") String cardNumber,
            @Param("startYear") Long startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Long endYear,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.topup_time) AS VARCHAR) AS reportYear,
                    CAST(COUNT(*) AS BIGINT) AS totalSuccess,
                    CAST(COALESCE(SUM(t.topup_amount), 0) AS BIGINT) AS totalAmount
                FROM topups t
                WHERE t.deleted_at IS NULL
                  AND t.status = 'success'
                  AND t.card_number = :cardNumber
                  AND (EXTRACT(YEAR FROM t.topup_time) = :year OR EXTRACT(YEAR FROM t.topup_time) = :year - 1)
                GROUP BY EXTRACT(YEAR FROM t.topup_time)
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
    List<TopupYearStatusSuccess> findYearlyTopupStatusSuccessByCard(
            @Param("cardNumber") String cardNumber,
            @Param("year") Long year);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.topup_time) AS VARCHAR) AS reportYear,
                    CAST(COUNT(*) AS BIGINT) AS totalFailed,
                    CAST(COALESCE(SUM(t.topup_amount), 0) AS BIGINT) AS totalAmount
                FROM topups t
                WHERE t.deleted_at IS NULL
                  AND t.status = 'failed'
                  AND t.card_number = :cardNumber
                  AND (EXTRACT(YEAR FROM t.topup_time) = :year OR EXTRACT(YEAR FROM t.topup_time) = :year - 1)
                GROUP BY EXTRACT(YEAR FROM t.topup_time)
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
    List<TopupYearStatusFailed> findYearlyTopupStatusFailedByCard(
            @Param("cardNumber") String cardNumber,
            @Param("year") Long year);
}
