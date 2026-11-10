package com.example.remembermeat.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.remembermeat.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    fun observeAll(): Flow<List<Product>>

    @Insert
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query(
        "SELECT * FROM products " +
                "WHERE expiryDate BETWEEN :fromEpochDay AND :toEpochDay " +
                "ORDER BY expiryDate ASC, name ASC"
    )
    suspend fun expiringBetween(fromEpochDay: Long, toEpochDay: Long): List<Product>
}