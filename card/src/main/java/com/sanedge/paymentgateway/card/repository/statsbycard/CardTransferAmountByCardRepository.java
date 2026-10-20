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
public interface CardTransferAmountByCardRepository extends JpaRepository<Card, Long> {
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
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM
                months m
            LEFT JOIN
                transfers t ON EXTRACT(MONTH FROM t.transfer_time) = m.m
                AND EXTRACT(YEAR FROM t.transfer_time) = EXTRACT(YEAR FROM :yearDate)
                AND t.deleted_at IS NULL
                AND t.transfer_from = :sender
            GROUP BY
                m.m
            ORDER BY
                m.m
            """, nativeQuery = true)
    List<CardMonthAmount> getMonthlyTransferAmountBySender(
            @Param("sender") String sender,
            @Param("yearDate") LocalDate yearDate);

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
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM
                months m
            LEFT JOIN
                transfers t ON EXTRACT(MONTH FROM t.transfer_time) = m.m
                AND EXTRACT(YEAR FROM t.transfer_time) = EXTRACT(YEAR FROM :yearDate)
                AND t.deleted_at IS NULL
                AND t.transfer_to = :receiver
            GROUP BY
                m.m
            ORDER BY
                m.m
            """, nativeQuery = true)
    List<CardMonthAmount> getMonthlyTransferAmountByReceiver(
            @Param("receiver") String receiver,
            @Param("yearDate") LocalDate yearDate);

    @Query(value = """
                WITH years AS (
                    SELECT :year - 4 AS y UNION ALL
                    SELECT :year - 3 AS y UNION ALL
                    SELECT :year - 2 AS y UNION ALL
                    SELECT :year - 1 AS y UNION ALL
                    SELECT :year AS y
                )
                SELECT
                    CAST(y.y AS VARCHAR) AS reportYear,
                    CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
                FROM
                    years y
                LEFT JOIN
                    transfers t ON EXTRACT(YEAR FROM t.transfer_time) = y.y
                      AND t.deleted_at IS NULL
                      AND t.transfer_from = :sender
                GROUP BY
                    y.y
                ORDER BY
                    y.y
            """, nativeQuery = true)
    List<CardYearAmount> getYearlyTransferAmountBySender(
            @Param("sender") String sender,
            @Param("year") Long year);

    @Query(value = """
                WITH years AS (
                    SELECT :year - 4 AS y UNION ALL
                    SELECT :year - 3 AS y UNION ALL
                    SELECT :year - 2 AS y UNION ALL
                    SELECT :year - 1 AS y UNION ALL
                    SELECT :year AS y
                )
                SELECT
                    CAST(y.y AS VARCHAR) AS reportYear,
                    CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
                FROM
                    years y
                LEFT JOIN
                    transfers t ON EXTRACT(YEAR FROM t.transfer_time) = y.y
                      AND t.deleted_at IS NULL
                      AND t.transfer_to = :receiver
                GROUP BY
                    y.y
                ORDER BY
                    y.y
            """, nativeQuery = true)
    List<CardYearAmount> getYearlyTransferAmountByReceiver(
            @Param("receiver") String receiver,
            @Param("year") Long year);
}