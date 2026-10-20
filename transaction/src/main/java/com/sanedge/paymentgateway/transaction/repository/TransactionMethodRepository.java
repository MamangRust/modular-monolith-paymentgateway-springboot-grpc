package com.sanedge.paymentgateway.transaction.repository;

import com.sanedge.paymentgateway.transaction.models.Transaction;
import com.sanedge.paymentgateway.transaction.models.TransactionMonthMethod;
import com.sanedge.paymentgateway.transaction.models.TransactionYearMethod;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionMethodRepository extends JpaRepository<Transaction, Long> {
    @Query(value = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            ),
            payment_methods AS (
                SELECT DISTINCT payment_method
                FROM transactions
                WHERE deleted_at IS NULL
            )
            SELECT
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' WHEN 4 THEN 'Apr'
                    WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug'
                    WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                pm.payment_method AS paymentMethod,
                CAST(COALESCE(COUNT(t.transaction_id), 0) AS BIGINT) AS totalTransactions,
                CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS totalAmount
            FROM months m
            CROSS JOIN payment_methods pm
            LEFT JOIN transactions t
                ON EXTRACT(MONTH FROM t.transaction_time) = m.m
               AND EXTRACT(YEAR FROM t.transaction_time) = :year
               AND t.payment_method = pm.payment_method
               AND t.deleted_at IS NULL
            GROUP BY m.m, pm.payment_method
            ORDER BY m.m, pm.payment_method
            """, nativeQuery = true)
    List<TransactionMonthMethod> findMonthlyPaymentMethods(@Param("year") Long year);

    @Query(value = """
            WITH years AS (
                SELECT :year - 4 AS y UNION ALL
                SELECT :year - 3 AS y UNION ALL
                SELECT :year - 2 AS y UNION ALL
                SELECT :year - 1 AS y UNION ALL
                SELECT :year AS y
            ),
            payment_methods AS (
                SELECT DISTINCT payment_method
                FROM transactions
                WHERE deleted_at IS NULL
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                pm.payment_method AS paymentMethod,
                CAST(COALESCE(COUNT(t.transaction_id), 0) AS BIGINT) AS totalTransactions,
                CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            CROSS JOIN payment_methods pm
            LEFT JOIN transactions t
                ON EXTRACT(YEAR FROM t.transaction_time) = y.y
               AND t.payment_method = pm.payment_method
               AND t.deleted_at IS NULL
            GROUP BY y.y, pm.payment_method
            ORDER BY y.y, pm.payment_method
            """, nativeQuery = true)
    List<TransactionYearMethod> findYearlyPaymentMethods(@Param("year") Long year);
}
