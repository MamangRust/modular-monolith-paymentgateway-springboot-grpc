package com.sanedge.paymentgateway.card.repository.stats;

import com.sanedge.paymentgateway.card.models.Card;
import com.sanedge.paymentgateway.card.models.CardMonthBalance;
import com.sanedge.paymentgateway.card.models.CardYearBalance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CardBalanceRepository extends JpaRepository<Card, Long> {
    @Query(value = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' WHEN 4 THEN 'Apr'
                    WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug'
                    WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(s.total_balance), 0) AS BIGINT) AS totalBalance
            FROM
                months m
            LEFT JOIN
                saldos s ON EXTRACT(MONTH FROM s.created_at) = m.m
                AND EXTRACT(YEAR FROM s.created_at) = EXTRACT(YEAR FROM :yearDate)
                AND s.deleted_at IS NULL
            LEFT JOIN
                cards c ON s.card_number = c.card_number
                AND c.deleted_at IS NULL
            GROUP BY
                m.m
            ORDER BY
                m.m
            """, nativeQuery = true)
    List<CardMonthBalance> getMonthlyBalances(@Param("yearDate") LocalDate yearDate);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    EXTRACT(YEAR FROM s.created_at) AS reportYear,
                    COALESCE(SUM(s.total_balance), 0) AS totalBalance
                FROM saldos s
                JOIN cards c ON s.card_number = c.card_number
                WHERE s.deleted_at IS NULL AND c.deleted_at IS NULL
                  AND EXTRACT(YEAR FROM s.created_at) >= :year - 4
                  AND EXTRACT(YEAR FROM s.created_at) <= :year
                GROUP BY EXTRACT(YEAR FROM s.created_at)
            ), yearly AS (
                SELECT :year AS reportYear UNION ALL
                SELECT :year - 1 UNION ALL
                SELECT :year - 2 UNION ALL
                SELECT :year - 3 UNION ALL
                SELECT :year - 4
            )
            SELECT
                CAST(y.reportYear AS CHAR(4)) AS reportYear,
                CAST(COALESCE(yd.totalBalance, 0) AS BIGINT) AS totalBalance
            FROM yearly y
            LEFT JOIN yearly_data yd ON y.reportYear = yd.reportYear
            ORDER BY y.reportYear DESC
            """, nativeQuery = true)
    List<CardYearBalance> getYearlyBalances(@Param("year") Long year);
}