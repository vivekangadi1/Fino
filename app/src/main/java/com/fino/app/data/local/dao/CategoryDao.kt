package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder")
    fun getAllActiveFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder")
    suspend fun getAllActive(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE parentId IS NULL AND isActive = 1 ORDER BY sortOrder")
    suspend fun getTopLevelCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE parentId = :parentId AND isActive = 1 ORDER BY sortOrder")
    suspend fun getSubcategories(parentId: Long): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isSystem = 1 ORDER BY sortOrder")
    suspend fun getSystemCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isSystem = 0 AND isActive = 1 ORDER BY sortOrder")
    suspend fun getUserCategories(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("UPDATE categories SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)
}
