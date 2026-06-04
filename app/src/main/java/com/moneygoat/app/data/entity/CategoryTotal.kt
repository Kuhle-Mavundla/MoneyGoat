package com.moneygoat.app.data.entity

/**
 * CategoryTotal is a data projection (POJO) used to hold the results of
 * aggregated SQL queries from the Room database.
 *
 * It specifically maps the output of the 'getCategoryTotals' query in ExpenseDao,
 * providing a clean structure for the Analytics UI to consume without needing
 * to parse raw cursor results.
 */
data class CategoryTotal(
    /**
     * The human-readable name of the category (e.g., "Food", "Transport").
     * Obtained via a JOIN with the categories table.
     */
    val categoryName: String,

    /**
     * The mathematical sum of all expense amounts mapped to this category
     * within the selected date range.
     */
    val totalAmount: Double
)
