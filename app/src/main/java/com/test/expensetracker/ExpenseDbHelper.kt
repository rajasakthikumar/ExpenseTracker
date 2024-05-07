package com.test.expensetracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// ExpenseDbHelper.kt
class ExpenseDbHelper(context: Context) : SQLiteOpenHelper(context, ExpenseContract.DATABASE_NAME, null, ExpenseContract.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createExpensesTable = "CREATE TABLE ${ExpenseContract.ExpenseEntry.TABLE_NAME} (" +
                "${ExpenseContract.ExpenseEntry.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${ExpenseContract.ExpenseEntry.COLUMN_AMOUNT} REAL," +
                "${ExpenseContract.ExpenseEntry.COLUMN_CATEGORY} TEXT," +
                "${ExpenseContract.ExpenseEntry.COLUMN_DATE} TEXT)"
        db.execSQL(createExpensesTable)

        val createCategoriesTable = "CREATE TABLE ${ExpenseContract.CategoryEntry.TABLE_NAME} (" +
                "${ExpenseContract.CategoryEntry.COLUMN_NAME} TEXT PRIMARY KEY)"
        db.execSQL(createCategoriesTable)

        // Insert default categories
        val defaultCategories = arrayOf("Food", "Transportation", "Shopping", "Entertainment", "Bills", "Other")
        defaultCategories.forEach { category ->
            val values = ContentValues().apply {
                put(ExpenseContract.CategoryEntry.COLUMN_NAME, category)
            }
            db.insert(ExpenseContract.CategoryEntry.TABLE_NAME, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ExpenseContract.ExpenseEntry.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${ExpenseContract.CategoryEntry.TABLE_NAME}")
        onCreate(db)
    }
}