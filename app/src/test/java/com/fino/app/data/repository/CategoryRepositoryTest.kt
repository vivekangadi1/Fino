package com.fino.app.data.repository

import com.fino.app.data.local.dao.CategoryDao
import com.fino.app.data.local.entity.CategoryEntity
import com.fino.app.domain.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class CategoryRepositoryTest {

    private lateinit var mockCategoryDao: CategoryDao
    private lateinit var repository: CategoryRepository

    @Before
    fun setup() {
        mockCategoryDao = mock()
        repository = CategoryRepository(mockCategoryDao)
    }

    // Test 1: getAllActive returns mapped domain objects
    @Test
    fun `getAllActive returns flow of mapped domain categories`(): Unit = runBlocking {
        // Given
        val entities = listOf(
            CategoryEntity(
                id = 1L,
                name = "Food",
                emoji = "🍔",
                parentId = null,
                isSystem = true,
                budgetLimit = 5000.0,
                sortOrder = 1,
                isActive = true
            ),
            CategoryEntity(
                id = 2L,
                name = "Transport",
                emoji = "🚗",
                parentId = null,
                isSystem = true,
                budgetLimit = 2000.0,
                sortOrder = 2,
                isActive = true
            )
        )
        whenever(mockCategoryDao.getAllActiveFlow()).thenReturn(flowOf(entities))

        // When
        val result = repository.getAllActive().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Food", result[0].name)
        assertEquals("🍔", result[0].emoji)
        assertEquals("Transport", result[1].name)
        verify(mockCategoryDao).getAllActiveFlow()
    }

    // Test 2: getAllActive returns empty list when no categories
    @Test
    fun `getAllActive returns empty list when no categories exist`(): Unit = runBlocking {
        // Given
        whenever(mockCategoryDao.getAllActiveFlow()).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.getAllActive().first()

        // Then
        assertTrue(result.isEmpty())
    }

    // Test 3: getById returns null when category doesn't exist
    @Test
    fun `getById returns null when category does not exist`(): Unit = runBlocking {
        // Given
        whenever(mockCategoryDao.getById(999L)).thenReturn(null)

        // When
        val result = repository.getById(999L)

        // Then
        assertNull(result)
        verify(mockCategoryDao).getById(999L)
    }

    // Test 4: getById returns mapped category when exists
    @Test
    fun `getById returns mapped category when exists`(): Unit = runBlocking {
        // Given
        val entity = CategoryEntity(
            id = 1L,
            name = "Food",
            emoji = "🍔",
            parentId = null,
            isSystem = true,
            budgetLimit = 5000.0,
            sortOrder = 1,
            isActive = true
        )
        whenever(mockCategoryDao.getById(1L)).thenReturn(entity)

        // When
        val result = repository.getById(1L)

        // Then
        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertEquals("Food", result?.name)
        assertEquals("🍔", result?.emoji)
        assertEquals(5000.0, result?.budgetLimit)
    }

    // Test 5: insert creates category and returns id
    @Test
    fun `insert creates category and returns generated id`(): Unit = runBlocking {
        // Given
        val category = Category(
            id = 0L,
            name = "Shopping",
            emoji = "🛍️",
            parentId = null,
            isSystem = false,
            budgetLimit = 3000.0,
            sortOrder = 3,
            isActive = true
        )
        whenever(mockCategoryDao.insert(any())).thenReturn(5L)

        // When
        val result = repository.insert(category)

        // Then
        assertEquals(5L, result)
        verify(mockCategoryDao).insert(argThat { entity ->
            entity.name == "Shopping" && entity.emoji == "🛍️"
        })
    }

    // Test 6: update calls dao update
    @Test
    fun `update calls dao with mapped entity`(): Unit = runBlocking {
        // Given
        val category = Category(
            id = 1L,
            name = "Updated Food",
            emoji = "🍕",
            parentId = null,
            isSystem = true,
            budgetLimit = 6000.0,
            sortOrder = 1,
            isActive = true
        )

        // When
        repository.update(category)

        // Then
        verify(mockCategoryDao).update(argThat { entity ->
            entity.id == 1L && entity.name == "Updated Food" && entity.emoji == "🍕"
        })
    }

    // Test 7: delete calls dao delete
    @Test
    fun `delete calls dao with mapped entity`(): Unit = runBlocking {
        // Given
        val category = Category(
            id = 1L,
            name = "Food",
            emoji = "🍔",
            parentId = null,
            isSystem = true,
            budgetLimit = 5000.0,
            sortOrder = 1,
            isActive = true
        )

        // When
        repository.delete(category)

        // Then
        verify(mockCategoryDao).delete(argThat { entity ->
            entity.id == 1L
        })
    }

    // Test 8: Entity to domain mapping preserves all fields
    @Test
    fun `entity to domain mapping preserves all fields correctly`(): Unit = runBlocking {
        // Given
        val entity = CategoryEntity(
            id = 42L,
            name = "Test Category",
            emoji = "🎯",
            parentId = 10L,
            isSystem = false,
            budgetLimit = 1500.0,
            sortOrder = 5,
            isActive = false
        )
        whenever(mockCategoryDao.getById(42L)).thenReturn(entity)

        // When
        val result = repository.getById(42L)

        // Then
        assertNotNull(result)
        assertEquals(42L, result?.id)
        assertEquals("Test Category", result?.name)
        assertEquals("🎯", result?.emoji)
        assertEquals(10L, result?.parentId)
        assertEquals(false, result?.isSystem)
        assertEquals(1500.0, result?.budgetLimit)
        assertEquals(5, result?.sortOrder)
        assertEquals(false, result?.isActive)
    }
}
