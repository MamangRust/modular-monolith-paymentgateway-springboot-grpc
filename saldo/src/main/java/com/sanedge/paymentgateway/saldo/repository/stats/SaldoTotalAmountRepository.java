package com.sanedge.paymentgateway.saldo.repository.stats;

import com.sanedge.paymentgateway.saldo.models.Saldo;
import com.sanedge.paymentgateway.saldo.models.SaldoMonthTotalBalance;
import com.sanedge.paymentgateway.saldo.models.SaldoYearTotalBalance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaldoTotalAmountRepository extends JpaRepository<Saldo, Long> {
    @Query(value = """
            WITH complete_months AS (
                SELECT CAST(:startYear AS INTEGER) AS reportYear, 1 AS mon UNION ALL
                SELECT CAST(:startYear AS INTEGER), 2 UNION ALL
                SELECT CAST(:startYear AS INTEGER), 3 UNION ALL
                SELECT CAST(:startYear AS INTEGER), 4 UNION ALL
                SELECT CAST(:startYear AS INTEGER), 5 UNION ALL
                SELECT CAST(:startYear AS INTEGER), 6 UNION ALL
                SELECT CAST(:startYear AS INTEGER), 7 UNION ALL
                SELECT CAST(:startYear AS INTEGER), 8 UNION ALL
                SELECT CAST(:startYear AS INTEGER), 9 UNION ALL
                SELECT CAST(:startYear AS INTEGER), 10 UNION ALL
                SELECT CAST(:startYear AS INTEGER), 11 UNION ALL
                SELECT CAST(:startYear AS INTEGER), 12
            ),
            monthly_totals AS (
                SELECT
                    EXTRACT(YEAR FROM s.created_at) AS reportYear,
                    EXTRACT(MONTH FROM s.created_at) AS mon,
                    SUM(s.total_balance) AS total_balance
                FROM saldos s
                WHERE s.deleted_at IS NULL
                  AND EXTRACT(YEAR FROM s.created_at) = CAST(:startYear AS INTEGER)
                GROUP BY EXTRACT(YEAR FROM s.created_at), EXTRACT(MONTH FROM s.created_at)
            )
            SELECT
                CAST(cm.reportYear AS CHAR(4)) AS reportYear,
                CASE cm.mon
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                    WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                    WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                    WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(mt.total_balance, 0) AS BIGINT) AS total_balance
            FROM complete_months cm
            LEFT JOIN monthly_totals mt
                ON cm.reportYear = mt.reportYear
                AND cm.mon = mt.mon
            ORDER BY cm.reportYear DESC, cm.mon DESC
            """, nativeQuery = true)
    List<SaldoMonthTotalBalance> findMonthlyTotalSaldoBalance(
            @Param("startYear") Long startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Long endYear,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            WITH years AS (
                SELECT :year AS y UNION ALL
                SELECT :year - 1
            )
            SELECT
                CAST(y.y AS CHAR(4)) AS reportYear,
                CAST(COALESCE(SUM(s.total_balance), 0) AS BIGINT) AS totalBalance
            FROM years y
            LEFT JOIN saldos s
                ON EXTRACT(YEAR FROM s.created_at) = y.y
                AND s.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y DESC
            """, nativeQuery = true)
    List<SaldoYearTotalBalance> findYearlyTotalSaldoBalance(@Param("year") Long year);
}
