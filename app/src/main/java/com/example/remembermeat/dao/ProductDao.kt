package com.example.remembermeat.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.remembermeat.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    fun observeAll(): Flow<List<Product>>

    @Insert
    suspend fun insert(product: Product): Long

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products WHERE expiryDate <= :limitEpochDay ORDER BY expiryDate ASC")
    suspend fun expiringUntil(limitEpochDay: Long): List<Product>

    @Query("SELECT * FROM products WHERE expiryDate = :epochDay ORDER BY name ASC")
    suspend fun expiringOn(epochDay: Long): List<Product>
}