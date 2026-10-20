package com.sanedge.paymentgateway.merchant.repository.statsbyid;

import com.sanedge.paymentgateway.merchant.models.Merchant;
import com.sanedge.paymentgateway.merchant.models.MerchantMonthlyPaymentMethod;
import com.sanedge.paymentgateway.merchant.models.MerchantYearlyPaymentMethod;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantMethodByIdRepository extends JpaRepository<Merchant, Long> {

    @Query(value = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            ),
            payment_methods AS (
                SELECT DISTINCT payment_method
                FROM transactions t
                WHERE t.deleted_at IS NULL
                  AND t.merchant_id = :merchantId
            )
            SELECT
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                    WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                    WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                    WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                pm.payment_method AS paymentMethod,
                CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS totalAmount
            FROM
                months m
            CROSS JOIN
                payment_methods pm
            LEFT JOIN
                transactions t ON EXTRACT(MONTH FROM t.transaction_time) = m.m
                AND EXTRACT(YEAR FROM t.transaction_time) = :year
                AND t.payment_method = pm.payment_method
                AND t.deleted_at IS NULL
                AND t.merchant_id = :merchantId
            GROUP BY
                m.m,
                pm.payment_method
            ORDER BY
                m.m,
                pm.payment_method
            """, nativeQuery = true)
    List<MerchantMonthlyPaymentMethod> findMonthlyPaymentMethodsById(
            @Param("merchantId") Long merchantId,
            @Param("year") Long year);

    @Query(value = """
            WITH years AS (
                SELECT :endYear AS reportYear UNION ALL
                SELECT :endYear - 1 UNION ALL
                SELECT :endYear - 2 UNION ALL
                SELECT :endYear - 3 UNION ALL
                SELECT :endYear - 4
            ),
            payment_methods AS (
                SELECT DISTINCT payment_method
                FROM transactions t
                WHERE t.deleted_at IS NULL
                  AND t.merchant_id = :merchantId
            )
            SELECT
                CAST(y.reportYear AS CHAR(4)) AS reportYear,
                pm.payment_method AS paymentMethod,
                CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            CROSS JOIN payment_methods pm
            LEFT JOIN transactions t
                ON EXTRACT(YEAR FROM t.transaction_time) = y.reportYear
                AND t.payment_method = pm.payment_method
                AND t.deleted_at IS NULL
                AND t.merchant_id = :merchantId
            GROUP BY y.reportYear, pm.payment_method
            ORDER BY y.reportYear ASC, pm.payment_method ASC
            """, nativeQuery = true)
    List<MerchantYearlyPaymentMethod> findYearlyPaymentMethodsById(
            @Param("merchantId") Long merchantId,
            @Param("endYear") Long endYear);
}
