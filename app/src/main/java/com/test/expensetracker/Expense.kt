package com.test.expensetracker

data class Expense(
    val id: Int = 0,
    val amount: Double,
    val category: Category,
    val date: String
)