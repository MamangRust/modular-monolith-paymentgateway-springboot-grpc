package com.sanedge.paymentgateway.withdraw.repository.stats;

import com.sanedge.paymentgateway.withdraw.models.WithdrawMonthStatusSuccess;
import com.sanedge.paymentgateway.withdraw.models.Withdraw;
import com.sanedge.paymentgateway.withdraw.models.WithdrawMonthStatusFailed;
import com.sanedge.paymentgateway.withdraw.models.WithdrawYearStatusSuccess;
import com.sanedge.paymentgateway.withdraw.models.WithdrawYearStatusFailed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawStatusRepository extends JpaRepository<Withdraw, Long> {
    @Query(value = """
            SELECT
                CAST(:startYear AS text) AS reportYear,
                m.monthName AS monthName,
                CAST(COALESCE(SUM(CASE WHEN w.status = 'success' THEN 1 ELSE 0 END), 0) AS bigint) AS totalSuccess,
                CAST(COALESCE(SUM(CASE WHEN w.status = 'success' THEN w.withdraw_amount ELSE 0 END), 0) AS bigint) AS totalAmount
            FROM (
                SELECT 'Jan' AS monthName, 1 AS monthNum UNION ALL
                SELECT 'Feb', 2 UNION ALL
                SELECT 'Mar', 3 UNION ALL
                SELECT 'Apr', 4 UNION ALL
                SELECT 'May', 5 UNION ALL
                SELECT 'Jun', 6 UNION ALL
                SELECT 'Jul', 7 UNION ALL
                SELECT 'Aug', 8 UNION ALL
                SELECT 'Sep', 9 UNION ALL
                SELECT 'Oct', 10 UNION ALL
                SELECT 'Nov', 11 UNION ALL
                SELECT 'Dec', 12
            ) m
            LEFT JOIN withdraws w ON EXTRACT(MONTH FROM w.withdraw_time) = m.monthNum
                AND EXTRACT(YEAR FROM w.withdraw_time) = :startYear
                AND w.deleted_at IS NULL
            WHERE m.monthNum >= :startMonth AND m.monthNum <= :endMonth
            GROUP BY m.monthName, m.monthNum
            ORDER BY m.monthNum
            """, nativeQuery = true)
    List<WithdrawMonthStatusSuccess> findMonthWithdrawStatusSuccess(
            @Param("startYear") Long startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Long endYear,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            SELECT
                CAST(:startYear AS text) AS reportYear,
                m.monthName AS monthName,
                CAST(COALESCE(SUM(CASE WHEN w.status = 'failed' THEN 1 ELSE 0 END), 0) AS bigint) AS totalFailed,
                CAST(COALESCE(SUM(CASE WHEN w.status = 'failed' THEN w.withdraw_amount ELSE 0 END), 0) AS bigint) AS totalAmount
            FROM (
                SELECT 'Jan' AS monthName, 1 AS monthNum UNION ALL
                SELECT 'Feb', 2 UNION ALL
                SELECT 'Mar', 3 UNION ALL
                SELECT 'Apr', 4 UNION ALL
                SELECT 'May', 5 UNION ALL
                SELECT 'Jun', 6 UNION ALL
                SELECT 'Jul', 7 UNION ALL
                SELECT 'Aug', 8 UNION ALL
                SELECT 'Sep', 9 UNION ALL
                SELECT 'Oct', 10 UNION ALL
                SELECT 'Nov', 11 UNION ALL
                SELECT 'Dec', 12
            ) m
            LEFT JOIN withdraws w ON EXTRACT(MONTH FROM w.withdraw_time) = m.monthNum
                AND EXTRACT(YEAR FROM w.withdraw_time) = :startYear
                AND w.deleted_at IS NULL
            WHERE m.monthNum >= :startMonth AND m.monthNum <= :endMonth
            GROUP BY m.monthName, m.monthNum
            ORDER BY m.monthNum
            """, nativeQuery = true)
    List<WithdrawMonthStatusFailed> findMonthWithdrawStatusFailed(
            @Param("startYear") Long startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Long endYear,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            WITH years AS (
                SELECT :year AS y UNION ALL SELECT :year - 1 AS y
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                CAST(COALESCE(COUNT(w.withdraw_id), 0) AS BIGINT) AS totalSuccess,
                CAST(COALESCE(SUM(w.withdraw_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN withdraws w
                ON EXTRACT(YEAR FROM w.withdraw_time) = y.y
                AND w.status = 'success'
                AND w.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y DESC
            """, nativeQuery = true)
    List<WithdrawYearStatusSuccess> findYearlyWithdrawStatusSuccess(@Param("year") Long year);

    @Query(value = """
            WITH years AS (
                SELECT :year AS y UNION ALL SELECT :year - 1 AS y
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                CAST(COALESCE(COUNT(w.withdraw_id), 0) AS BIGINT) AS totalFailed,
                CAST(COALESCE(SUM(w.withdraw_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN withdraws w
                ON EXTRACT(YEAR FROM w.withdraw_time) = y.y
                AND w.status = 'failed'
                AND w.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y DESC
            """, nativeQuery = true)
    List<WithdrawYearStatusFailed> findYearlyWithdrawStatusFailed(@Param("year") Long year);
}
