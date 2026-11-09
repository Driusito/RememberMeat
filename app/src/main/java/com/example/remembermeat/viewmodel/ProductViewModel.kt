package com.example.remembermeat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.remembermeat.database.AppDatabase
import com.example.remembermeat.entity.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProductViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).productDao()

    val products: StateFlow<List<Product>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(product: Product) {
        viewModelScope.launch { dao.insert(product) }
    }

    fun delete(product: Product) {
        viewModelScope.launch {
            dao.delete(product)
            product.photoPath?.let { path ->
                withContext(Dispatchers.IO) { File(path).delete() }
            }
        }
    }
}