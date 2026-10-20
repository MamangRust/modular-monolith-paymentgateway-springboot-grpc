package com.sanedge.paymentgateway.merchant.repository.statsbyid;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.merchant.models.Merchant;
import com.sanedge.paymentgateway.merchant.models.MerchantMonthlyTotalAmount;
import com.sanedge.paymentgateway.merchant.models.MerchantYearlyTotalAmount;

@Repository
public interface MerchantTotalAmountByIdRepository extends JpaRepository<Merchant, Long> {

    @Query(value = """
            WITH monthly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.transaction_time) AS CHAR(4)) AS reportYear,
                    CASE EXTRACT(MONTH FROM t.transaction_time)
                        WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                        WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                        WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                        WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                    END AS monthName,
                    CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS totalAmount,
                    CAST(EXTRACT(YEAR FROM t.transaction_time) * 100 + EXTRACT(MONTH FROM t.transaction_time) AS INTEGER) AS month_sort
                FROM
                    transactions t
                INNER JOIN
                    merchants m ON t.merchant_id = m.merchant_id
                WHERE
                    t.deleted_at IS NULL
                    AND m.deleted_at IS NULL
                    AND m.merchant_id = :merchantId
                    AND t.transaction_time >= CAST(DATEADD('MONTH', -1, CAST(:date AS DATE)) AS TIMESTAMP)
                    AND t.transaction_time < CAST(DATEADD('MONTH', 1, CAST(:date AS DATE)) AS TIMESTAMP)
                GROUP BY
                    EXTRACT(YEAR FROM t.transaction_time),
                    EXTRACT(MONTH FROM t.transaction_time)
            ),
            missing_months AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM CAST(:date AS DATE)) AS CHAR(4)) AS reportYear,
                    CASE EXTRACT(MONTH FROM CAST(:date AS DATE))
                        WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                        WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                        WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                        WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                    END AS monthName,
                    CAST(0 AS BIGINT) AS totalAmount,
                    CAST(EXTRACT(YEAR FROM CAST(:date AS DATE)) * 100 + EXTRACT(MONTH FROM CAST(:date AS DATE)) AS INTEGER) AS month_sort
                WHERE NOT EXISTS (
                    SELECT 1 FROM monthly_data
                    WHERE month_sort = CAST(EXTRACT(YEAR FROM CAST(:date AS DATE)) * 100 + EXTRACT(MONTH FROM CAST(:date AS DATE)) AS INTEGER)
                )
                UNION ALL
                SELECT
                    CAST(EXTRACT(YEAR FROM DATEADD('MONTH', -1, CAST(:date AS DATE))) AS CHAR(4)) AS reportYear,
                    CASE EXTRACT(MONTH FROM DATEADD('MONTH', -1, CAST(:date AS DATE)))
                        WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                        WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                        WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                        WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                    END AS monthName,
                    CAST(0 AS BIGINT) AS totalAmount,
                    CAST(EXTRACT(YEAR FROM DATEADD('MONTH', -1, CAST(:date AS DATE))) * 100 + EXTRACT(MONTH FROM DATEADD('MONTH', -1, CAST(:date AS DATE))) AS INTEGER) AS month_sort
                WHERE NOT EXISTS (
                    SELECT 1 FROM monthly_data
                    WHERE month_sort = CAST(EXTRACT(YEAR FROM DATEADD('MONTH', -1, CAST(:date AS DATE))) * 100 + EXTRACT(MONTH FROM DATEADD('MONTH', -1, CAST(:date AS DATE))) AS INTEGER)
                )
            )
            SELECT reportYear, monthName, totalAmount
            FROM (
                SELECT reportYear, monthName, totalAmount, month_sort FROM monthly_data
                UNION ALL
                SELECT reportYear, monthName, totalAmount, month_sort FROM missing_months
            ) combined
            ORDER BY month_sort DESC
            """, nativeQuery = true)
    List<MerchantMonthlyTotalAmount> findMonthlyTotalAmountById(
            @Param("merchantId") Long merchantId,
            @Param("date") LocalDate date);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.transaction_time) AS CHAR(4)) AS reportYear,
                    CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS totalAmount
                FROM
                    transactions t
                INNER JOIN
                    merchants m ON t.merchant_id = m.merchant_id
                WHERE
                    t.deleted_at IS NULL
                    AND m.deleted_at IS NULL
                    AND m.merchant_id = :merchantId
                    AND EXTRACT(YEAR FROM t.transaction_time) IN (:year, :previousYear)
                GROUP BY
                    EXTRACT(YEAR FROM t.transaction_time)
            ), formatted_data AS (
                SELECT reportYear, totalAmount FROM yearly_data
                UNION ALL
                SELECT CAST(:year AS CHAR(4)) AS reportYear, CAST(0 AS BIGINT) AS totalAmount
                WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year AS CHAR(4)))
                UNION ALL
                SELECT CAST(:previousYear AS CHAR(4)) AS reportYear, CAST(0 AS BIGINT) AS totalAmount
                WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:previousYear AS CHAR(4)))
            )
            SELECT reportYear, totalAmount
            FROM formatted_data
            ORDER BY reportYear DESC
            """, nativeQuery = true)
    List<MerchantYearlyTotalAmount> findYearlyTotalAmountById(
            @Param("merchantId") Long merchantId,
            @Param("year") Long year,
            @Param("previousYear") Long previousYear);

}
