package com.test.expensetracker

object ExpenseContract {
    const val DATABASE_NAME = "expense_database"
    const val DATABASE_VERSION = 1

    object ExpenseEntry {
        const val TABLE_NAME = "expenses"
        const val COLUMN_ID = "id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DATE = "date"
    }

    object CategoryEntry {
        const val TABLE_NAME = "categories"
        const val COLUMN_NAME = "name"
    }
}