package com.sanedge.paymentgateway.transfer.repository.stats;

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
public interface TransferStatusRepository extends JpaRepository<Transfer, Long> {
    @Query(value = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CAST(:startYear AS VARCHAR) AS reportYear,
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' WHEN 4 THEN 'Apr'
                    WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug'
                    WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'success' THEN 1 ELSE 0 END), 0) AS BIGINT) AS totalSuccess,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'success' THEN t.transfer_amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN transfers t
                ON EXTRACT(MONTH FROM t.transfer_time) = m.m
               AND EXTRACT(YEAR FROM t.transfer_time) = :startYear
               AND t.deleted_at IS NULL
            WHERE m.m >= :startMonth AND m.m <= :endMonth
            GROUP BY m.m
            ORDER BY m.m
            """, nativeQuery = true)
    List<TransferMonthStatusSuccess> findMonthTransferStatusSuccess(
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
                CAST(:startYear AS VARCHAR) AS reportYear,
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' WHEN 4 THEN 'Apr'
                    WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug'
                    WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'failed' THEN 1 ELSE 0 END), 0) AS BIGINT) AS totalFailed,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'failed' THEN t.transfer_amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN transfers t
                ON EXTRACT(MONTH FROM t.transfer_time) = m.m
               AND EXTRACT(YEAR FROM t.transfer_time) = :startYear
               AND t.deleted_at IS NULL
            WHERE m.m >= :startMonth AND m.m <= :endMonth
            GROUP BY m.m
            ORDER BY m.m
            """, nativeQuery = true)
    List<TransferMonthStatusFailed> findMonthTransferStatusFailed(
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
                CAST(COALESCE(COUNT(t.transfer_id), 0) AS BIGINT) AS totalSuccess,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN transfers t
                ON EXTRACT(YEAR FROM t.transfer_time) = y.y
                AND t.status = 'success'
                AND t.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y DESC
            """, nativeQuery = true)
    List<TransferYearStatusSuccess> findYearlyTransferStatusSuccess(@Param("year") Long year);

    @Query(value = """
            WITH years AS (
                SELECT :year AS y UNION ALL SELECT :year - 1 AS y
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                CAST(COALESCE(COUNT(t.transfer_id), 0) AS BIGINT) AS totalFailed,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN transfers t
                ON EXTRACT(YEAR FROM t.transfer_time) = y.y
                AND t.status = 'failed'
                AND t.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y DESC
            """, nativeQuery = true)
    List<TransferYearStatusFailed> findYearlyTransferStatusFailed(@Param("year") Long year);

}
