package com.sanedge.paymentgateway.transfer.repository.statsbycard;

import com.sanedge.paymentgateway.transfer.models.Transfer;
import com.sanedge.paymentgateway.transfer.models.TransferMonthAmount;
import com.sanedge.paymentgateway.transfer.models.TransferYearAmount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferAmountByCardRepository extends JpaRepository<Transfer, Long> {
    @Query(value = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                    WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                    WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                    WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN transfers t
                ON EXTRACT(MONTH FROM t.transfer_time) = m.m
                AND EXTRACT(YEAR FROM t.transfer_time) = :year
                AND t.transfer_from = :transferFrom
                AND t.deleted_at IS NULL
            GROUP BY m.m
            ORDER BY m.m
            """, nativeQuery = true)
    List<TransferMonthAmount> findMonthlyTransferAmountsBySender(
            @Param("transferFrom") String transferFrom,
            @Param("year") Long year);

    @Query(value = """
            WITH yearly AS (
                SELECT :endYear AS reportYear UNION ALL
                SELECT :endYear - 1 UNION ALL
                SELECT :endYear - 2 UNION ALL
                SELECT :endYear - 3 UNION ALL
                SELECT :endYear - 4
            )
            SELECT
                CAST(y.reportYear AS CHAR(4)) AS reportYear,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM yearly y
            LEFT JOIN transfers t
                ON EXTRACT(YEAR FROM t.transfer_time) = y.reportYear
                AND t.transfer_from = :transferFrom
                AND t.deleted_at IS NULL
            GROUP BY y.reportYear
            ORDER BY y.reportYear
            """, nativeQuery = true)
    List<TransferYearAmount> findYearlyTransferAmountsBySender(
            @Param("transferFrom") String transferFrom,
            @Param("endYear") Long endYear);

    @Query(value = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                    WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                    WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                    WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN transfers t
                ON EXTRACT(MONTH FROM t.transfer_time) = m.m
                AND EXTRACT(YEAR FROM t.transfer_time) = :year
                AND t.transfer_to = :transferTo
                AND t.deleted_at IS NULL
            GROUP BY m.m
            ORDER BY m.m
            """, nativeQuery = true)
    List<TransferMonthAmount> findMonthlyTransferAmountsByReceiver(
            @Param("transferTo") String transferTo,
            @Param("year") Long year);

    @Query(value = """
            WITH yearly AS (
                SELECT :endYear AS reportYear UNION ALL
                SELECT :endYear - 1 UNION ALL
                SELECT :endYear - 2 UNION ALL
                SELECT :endYear - 3 UNION ALL
                SELECT :endYear - 4
            )
            SELECT
                CAST(y.reportYear AS CHAR(4)) AS reportYear,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM yearly y
            LEFT JOIN transfers t
                ON EXTRACT(YEAR FROM t.transfer_time) = y.reportYear
                AND t.transfer_to = :transferTo
                AND t.deleted_at IS NULL
            GROUP BY y.reportYear
            ORDER BY y.reportYear
            """, nativeQuery = true)
    List<TransferYearAmount> findYearlyTransferAmountsByReceiver(
            @Param("transferTo") String transferTo,
            @Param("endYear") Long endYear);
}
