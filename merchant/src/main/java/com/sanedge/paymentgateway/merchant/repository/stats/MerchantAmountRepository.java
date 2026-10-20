package com.sanedge.paymentgateway.merchant.repository.stats;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.merchant.models.Merchant;
import com.sanedge.paymentgateway.merchant.models.MerchantMonthlyAmount;
import com.sanedge.paymentgateway.merchant.models.MerchantYearlyAmount;

@Repository
public interface MerchantAmountRepository extends JpaRepository<Merchant, Long> {

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
                CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS totalAmount
            FROM
                months m
            LEFT JOIN
                transactions t ON EXTRACT(MONTH FROM t.transaction_time) = m.m
                AND EXTRACT(YEAR FROM t.transaction_time) = :year
                AND t.deleted_at IS NULL
            GROUP BY
                m.m
            ORDER BY
                m.m
            """, nativeQuery = true)
    List<MerchantMonthlyAmount> findMonthlyAmount(@Param("year") Long year);

    @Query(value = """
            WITH years AS (
                SELECT :year AS reportYear UNION ALL
                SELECT :year - 1 UNION ALL
                SELECT :year - 2 UNION ALL
                SELECT :year - 3 UNION ALL
                SELECT :year - 4
            )
            SELECT
                CAST(y.reportYear AS CHAR(4)) AS reportYear,
                CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN transactions t
                ON EXTRACT(YEAR FROM t.transaction_time) = y.reportYear
                AND t.deleted_at IS NULL
            GROUP BY y.reportYear
            ORDER BY y.reportYear
            """, nativeQuery = true)
    List<MerchantYearlyAmount> findYearlyAmount(@Param("year") Long year);
}
