package com.sanedge.paymentgateway.card.repository.statsbycard;

import com.sanedge.paymentgateway.card.models.Card;
import com.sanedge.paymentgateway.card.models.CardMonthAmount;
import com.sanedge.paymentgateway.card.models.CardYearAmount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CardWithdrawAmountByCardRepository extends JpaRepository<Card, Long> {
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
                CAST(COALESCE(SUM(w.withdraw_amount), 0) AS BIGINT) AS totalWithdrawAmount
            FROM months m
            LEFT JOIN withdraws w
                ON EXTRACT(MONTH FROM w.withdraw_time) = m.m
                AND EXTRACT(YEAR FROM w.withdraw_time) = EXTRACT(YEAR FROM CAST(:yearDate AS date))
                AND w.deleted_at IS NULL
                AND w.card_number = :cardNumber
            GROUP BY m.m
            ORDER BY m.m
            """, nativeQuery = true)
    List<CardMonthAmount> getMonthlyWithdrawAmountByCard(
            @Param("cardNumber") String cardNumber,
            @Param("yearDate") LocalDate yearDate);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    EXTRACT(YEAR FROM w.withdraw_time) AS reportYear,
                    COALESCE(SUM(w.withdraw_amount), 0) AS totalAmount
                FROM withdraws w
                JOIN cards c ON w.card_number = c.card_number
                WHERE w.deleted_at IS NULL
                  AND c.deleted_at IS NULL
                  AND w.card_number = :cardNumber
                  AND EXTRACT(YEAR FROM w.withdraw_time) >= :year - 4
                  AND EXTRACT(YEAR FROM w.withdraw_time) <= :year
                GROUP BY EXTRACT(YEAR FROM w.withdraw_time)
            ), yearly AS (
                SELECT :year AS reportYear UNION ALL
                SELECT :year - 1 UNION ALL
                SELECT :year - 2 UNION ALL
                SELECT :year - 3 UNION ALL
                SELECT :year - 4
            )
            SELECT
                CAST(y.reportYear AS CHAR(4)) AS reportYear,
                CAST(COALESCE(yd.totalAmount, 0) AS BIGINT) AS totalWithdrawAmount
            FROM yearly y
            LEFT JOIN yearly_data yd ON y.reportYear = yd.reportYear
            ORDER BY y.reportYear
            """, nativeQuery = true)
    List<CardYearAmount> getYearlyWithdrawAmountByCard(@Param("cardNumber") String cardNumber,
            @Param("year") Long year);
}
