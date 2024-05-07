package com.test.expensetracker

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {
    private val _expenses = mutableStateOf<List<Expense>>(emptyList())
    val expenses: State<List<Expense>> = _expenses

    private val _monthlyTotal = mutableStateOf(0.0)
    val monthlyTotal: State<Double> = _monthlyTotal

    private val _categories = mutableStateOf<List<Category>>(emptyList())
    val categories: State<List<Category>> = _categories

    private val _isAddExpenseDialogVisible = mutableStateOf(false)
    val isAddExpenseDialogVisible: State<Boolean> = _isAddExpenseDialogVisible

    private val _isAddCategoryDialogVisible = mutableStateOf(false)
    val isAddCategoryDialogVisible: State<Boolean> = _isAddCategoryDialogVisible

    private var _isDeleteCategoryDialogVisible = mutableStateOf(false)
    val isDeleteCategoryDialogVisible: State<Boolean> = _isDeleteCategoryDialogVisible


    private val _categoryError = mutableStateOf(false)
    val categoryError: State<Boolean> = _categoryError

    init {
        fetchExpenses()
        fetchMonthlyTotal()
        fetchCategories()
    }

    fun addExpense(amount: Double, category: Category, date: String) {
        repository.addExpense(amount, category, date)
        fetchExpenses()
        fetchMonthlyTotal()
    }

    fun deleteExpense(id: Int) {
        repository.deleteExpense(id)
        fetchExpenses()
        fetchMonthlyTotal()
    }

    fun addCategory(categoryName: String) {
        if (categories.value.contains(categoryName)) {
            _categoryError.value = true
        } else {
            repository.addCategory(categoryName)
            fetchCategories()
        }
    }

    fun resetCategoryError() {
        _categoryError.value = false
    }

    fun deleteCategory(category: Category) {
        repository.deleteCategory(category)
        fetchCategories()
    }

    fun showAddExpenseDialog() {
        _isAddExpenseDialogVisible.value = true
    }

    fun showAddCategoryDialog() {
        _isAddCategoryDialogVisible.value = true
    }

    fun hideAddCategoryDialog() {
        _isAddCategoryDialogVisible.value = false
    }

    fun hideAddExpenseDialog() {
        _isAddExpenseDialogVisible.value = false
    }

    private fun fetchExpenses() {
        _expenses.value = repository.getExpenses()
    }

    private fun fetchMonthlyTotal() {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        _monthlyTotal.value = repository.getMonthlyTotal(month, year)
    }

    private fun fetchCategories() {
        _categories.value = repository.getCategories()
    }

    fun showDeleteCategoryDialog() {
        _isDeleteCategoryDialogVisible.value = true
    }

    fun hideDeleteCategoryDialog() {
        _isDeleteCategoryDialogVisible.value = false
    }





}