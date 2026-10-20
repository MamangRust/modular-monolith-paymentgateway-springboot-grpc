package com.sanedge.paymentgateway.topup.repository.statsbycard;

import com.sanedge.paymentgateway.topup.models.Topup;
import com.sanedge.paymentgateway.topup.models.TopupMonthMethod;
import com.sanedge.paymentgateway.topup.models.TopupYearMethod;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopupMethodByCardRepository extends JpaRepository<Topup, Long> {

    @Query(value = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            ),
            topup_methods AS (
                SELECT DISTINCT topup_method
                FROM topups t
                WHERE t.deleted_at IS NULL
                  AND t.card_number = :cardNumber
            )
            SELECT
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' WHEN 4 THEN 'Apr'
                    WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug'
                    WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                tm.topup_method AS topupMethod,
                CAST(COALESCE(COUNT(t.topup_id), 0) AS BIGINT) AS totalTopups,
                CAST(COALESCE(SUM(t.topup_amount), 0) AS BIGINT) AS totalAmount
            FROM months m
            CROSS JOIN topup_methods tm
            LEFT JOIN topups t
                ON EXTRACT(MONTH FROM t.topup_time) = m.m
                AND EXTRACT(YEAR FROM t.topup_time) = :year
                AND t.topup_method = tm.topup_method
                AND t.card_number = :cardNumber
                AND t.deleted_at IS NULL
            GROUP BY m.m, tm.topup_method
            ORDER BY m.m, tm.topup_method
            """, nativeQuery = true)
    List<TopupMonthMethod> findMonthlyTopupMethodsByCard(
            @Param("cardNumber") String cardNumber,
            @Param("year") Long year);

    @Query(value = """
            WITH years AS (
                SELECT :endYear - 4 AS y UNION ALL
                SELECT :endYear - 3 AS y UNION ALL
                SELECT :endYear - 2 AS y UNION ALL
                SELECT :endYear - 1 AS y UNION ALL
                SELECT :endYear AS y
            ),
            topup_methods AS (
                SELECT DISTINCT topup_method
                FROM topups t
                WHERE t.deleted_at IS NULL
                  AND t.card_number = :cardNumber
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                tm.topup_method AS topupMethod,
                CAST(COALESCE(COUNT(t.topup_id), 0) AS BIGINT) AS totalTopups,
                CAST(COALESCE(SUM(t.topup_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            CROSS JOIN topup_methods tm
            LEFT JOIN topups t
                ON EXTRACT(YEAR FROM t.topup_time) = y.y
                AND t.topup_method = tm.topup_method
                AND t.card_number = :cardNumber
                AND t.deleted_at IS NULL
            GROUP BY y.y, tm.topup_method
            ORDER BY y.y, tm.topup_method
            """, nativeQuery = true)
    List<TopupYearMethod> findYearlyTopupMethodsByCardId(
            @Param("cardNumber") String cardNumber,
            @Param("endYear") Long endYear);
}
