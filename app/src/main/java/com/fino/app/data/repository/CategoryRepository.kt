package com.fino.app.data.repository

import com.fino.app.data.local.dao.CategoryDao
import com.fino.app.data.local.entity.CategoryEntity
import com.fino.app.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    /**
     * Get all active categories as a Flow for reactive updates
     */
    fun getAllActive(): Flow<List<Category>> {
        return categoryDao.getAllActiveFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get a category by its ID
     */
    suspend fun getById(id: Long): Category? {
        return categoryDao.getById(id)?.toDomain()
    }

    /**
     * Insert a new category
     * @return the generated ID of the inserted category
     */
    suspend fun insert(category: Category): Long {
        return categoryDao.insert(category.toEntity())
    }

    /**
     * Update an existing category
     */
    suspend fun update(category: Category) {
        categoryDao.update(category.toEntity())
    }

    /**
     * Delete a category
     */
    suspend fun delete(category: Category) {
        categoryDao.delete(category.toEntity())
    }

    // Mapping functions

    private fun CategoryEntity.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            emoji = emoji,
            parentId = parentId,
            isSystem = isSystem,
            budgetLimit = budgetLimit,
            sortOrder = sortOrder,
            isActive = isActive
        )
    }

    private fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            emoji = emoji,
            parentId = parentId,
            isSystem = isSystem,
            budgetLimit = budgetLimit,
            sortOrder = sortOrder,
            isActive = isActive
        )
    }
}
