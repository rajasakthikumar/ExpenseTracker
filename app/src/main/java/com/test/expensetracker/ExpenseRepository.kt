package com.test.expensetracker

import android.content.ContentValues

class ExpenseRepository(private val dbHelper: ExpenseDbHelper) {
    fun addExpense(amount: Double, category: Category, date: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ExpenseContract.ExpenseEntry.COLUMN_AMOUNT, amount)
            put(ExpenseContract.ExpenseEntry.COLUMN_CATEGORY, category)
            put(ExpenseContract.ExpenseEntry.COLUMN_DATE, date)
        }
        db.insert(ExpenseContract.ExpenseEntry.TABLE_NAME, null, values)
        db.close()
    }

    fun getExpenses(): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            ExpenseContract.ExpenseEntry.COLUMN_ID,
            ExpenseContract.ExpenseEntry.COLUMN_AMOUNT,
            ExpenseContract.ExpenseEntry.COLUMN_CATEGORY,
            ExpenseContract.ExpenseEntry.COLUMN_DATE
        )
        val cursor = db.query(
            ExpenseContract.ExpenseEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            "${ExpenseContract.ExpenseEntry.COLUMN_DATE} DESC"
        )
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_ID))
                val amount = getDouble(getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_AMOUNT))
                val categoryName = getString(getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_CATEGORY))
                val category = categoryName
                val date = getString(getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_DATE))
                expenses.add(Expense(id, amount, category, date))
            }
        }
        cursor.close()
        db.close()
        return expenses
    }

    fun deleteExpense(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete(ExpenseContract.ExpenseEntry.TABLE_NAME, "${ExpenseContract.ExpenseEntry.COLUMN_ID} = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getMonthlyTotal(month: Int, year: Int): Double {
        var total = 0.0
        val db = dbHelper.readableDatabase
        val projection = arrayOf("SUM(${ExpenseContract.ExpenseEntry.COLUMN_AMOUNT}) AS total")
        val selection = "${ExpenseContract.ExpenseEntry.COLUMN_DATE} BETWEEN ? AND ?"
        val startDate = "$year-${"%02d".format(month)}-01"
        val endDate = "$year-${"%02d".format(month)}-31"
        val selectionArgs = arrayOf(startDate, endDate)
        val cursor = db.query(
            ExpenseContract.ExpenseEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        with(cursor) {
            if (moveToFirst()) {
                total = getDouble(getColumnIndexOrThrow("total"))
            }
        }
        cursor.close()
        db.close()
        return total
    }

    fun getCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val db = dbHelper.readableDatabase
        val projection = arrayOf(ExpenseContract.CategoryEntry.COLUMN_NAME)
        val cursor = db.query(
            ExpenseContract.CategoryEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        with(cursor) {
            while (moveToNext()) {
                val categoryName = getString(getColumnIndexOrThrow(ExpenseContract.CategoryEntry.COLUMN_NAME))
                categories.add(categoryName)
            }
        }
        cursor.close()
        db.close()
        return categories
    }

    fun addCategory(category: Category) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ExpenseContract.CategoryEntry.COLUMN_NAME, category)
        }
        db.insert(ExpenseContract.CategoryEntry.TABLE_NAME, null, values)
        db.close()
    }

    fun deleteCategory(category: Category) {
        val db = dbHelper.writableDatabase
        db.delete(ExpenseContract.CategoryEntry.TABLE_NAME, "${ExpenseContract.CategoryEntry.COLUMN_NAME} = ?", arrayOf(category))
        db.close()
    }
}