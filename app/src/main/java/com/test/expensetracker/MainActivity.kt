package com.test.expensetracker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = ExpenseDbHelper(this)
        val repository = ExpenseRepository(dbHelper)
        val viewModel: ExpenseViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExpenseViewModel(repository) as T
                }
            }
        }

        setContent {
            ExpenseTrackerApp(viewModel)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(viewModel: ExpenseViewModel) {
    val expenses by viewModel.expenses
    val monthlyTotal by viewModel.monthlyTotal
    val categories by viewModel.categories

    Scaffold(
        topBar = {
            TopAppBar(
                title = {

                    Column{
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Expense Tracker",modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.showAddCategoryDialog() }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Expense")
                            }
                            IconButton(
                                    onClick = { viewModel.showDeleteCategoryDialog() }
                                    ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Category")
                            }
                        }
                        Text(text = "Monthly Expense: $monthlyTotal")
                    }
                }

            )

        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddExpenseDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Monthly Total: $monthlyTotal",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(expenses) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onDeleteClick = { viewModel.deleteExpense(expense.id) }
                    )
                }
            }
        }
    }

    if (viewModel.isAddExpenseDialogVisible.value) {
        AddExpenseDialog(
            onDismiss = { viewModel.hideAddExpenseDialog() },
            onAddClick = { amount, category ->
                viewModel.addExpense(
                    amount,
                    category,
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                )
            },
            categories = categories
        )
    } else if (viewModel.isAddCategoryDialogVisible.value) {
        AddCategory(onDismiss = { viewModel.hideAddCategoryDialog()}, onAddClick = {
            it -> viewModel.addCategory(it)
        }, viewModel  = viewModel
            )
    }
    else if(viewModel.isDeleteCategoryDialogVisible.value) {
        DeleteCategoryDialog(viewModel = viewModel) {
            viewModel.hideDeleteCategoryDialog()
        }
    }

}


@Composable
fun AddCategory(onDismiss: () -> Unit, onAddClick: (String) -> Unit, viewModel: ExpenseViewModel) {
    var category by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf(false) }

    val categoryError by viewModel.categoryError

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = { Text("Add Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        inputError = it.isBlank()
                        viewModel.resetCategoryError()
                    },
                    label = { Text("Category Name") },
                    singleLine = true,
                    isError = inputError || categoryError,
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Words)
                )
                if (inputError) {
                    Text("Please enter a category name.", color = MaterialTheme.colorScheme.error)
                } else if (categoryError) {
                    Text("Category already exists.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (category.isNotBlank()) {
                        onAddClick(category.trim())
                        if (!categoryError) {
                            onDismiss()
                        }
                    } else {
                        inputError = true
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismiss()
                viewModel.resetCategoryError()
            }) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun DeleteCategoryDialog(viewModel: ExpenseViewModel, onDismiss: () -> Unit) {
    val categories by viewModel.categories
    var selectedCategory by remember { mutableStateOf<Category>("Select Category") }
    var isExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Category") },
        text = {
            Spacer(modifier = Modifier.height(20.dp))
            Column {
                Button(
                    onClick = { isExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedCategory)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand dropdown"
                    )
                }
                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(text = category) },
                            onClick = {
                                selectedCategory = category
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategory?.let {
                        viewModel.deleteCategory(it)
                        onDismiss()
                    }
                }
            ) { Text("Delete") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Composable
fun ExpenseItem(expense: Expense, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    text = expense.date,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.width(150.dp))
            Text(
                text = "$${expense.amount}",
                style = MaterialTheme.typography.titleSmall
            )
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Expense")
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAddClick: (Double, Category) -> Unit,
    categories: List<Category>
) {
    if (categories.isEmpty()) {
        Text("No categories available")
        return
    }

    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = it.toDoubleOrNull() == null && it.isNotEmpty()
                    },
                    isError = amountError,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    trailingIcon = {
                        if (amountError) {
                            Icon(Icons.Filled.Clear, contentDescription = "Error")
                        }
                    }
                )
                if (amountError) {
                    Text("Please enter a valid amount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                // Category dropdown button
                Button(onClick = { expanded = true }) {
                    Text(text = selectedCategory)
                    Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Expand dropdown")
                }

                // Dropdown menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            },
                            text = { Text(text = category) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null) {
                        onAddClick(amountValue, selectedCategory)
                        onDismiss()
                    } else {
                        amountError = true
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


