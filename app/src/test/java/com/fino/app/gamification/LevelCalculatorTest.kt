package com.fino.app.gamification

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LevelCalculatorTest {

    private lateinit var calculator: LevelCalculator

    @Before
    fun setup() {
        calculator = LevelCalculator()
    }

    @Test
    fun `0 XP is level 1`() {
        assertEquals(1, calculator.calculateLevel(0))
    }

    @Test
    fun `99 XP is still level 1`() {
        assertEquals(1, calculator.calculateLevel(99))
    }

    @Test
    fun `100 XP is level 2`() {
        assertEquals(2, calculator.calculateLevel(100))
    }

    @Test
    fun `299 XP is still level 2`() {
        assertEquals(2, calculator.calculateLevel(299))
    }

    @Test
    fun `300 XP is level 3`() {
        assertEquals(3, calculator.calculateLevel(300))
    }

    @Test
    fun `3000 XP is max level 8`() {
        assertEquals(8, calculator.calculateLevel(3000))
    }

    @Test
    fun `10000 XP is still max level 8`() {
        assertEquals(8, calculator.calculateLevel(10000))
    }

    @Test
    fun `progress to next level - from level 1 at 50 XP`() {
        val progress = calculator.getProgressToNextLevel(50)

        assertEquals(1, progress.currentLevel)
        assertEquals(2, progress.nextLevel)
        assertEquals(50, progress.currentXp)
        assertEquals(100, progress.xpForNextLevel)
        assertEquals(0.5f, progress.progressPercent, 0.01f)
    }

    @Test
    fun `progress at max level - shows 100 percent`() {
        val progress = calculator.getProgressToNextLevel(5000)

        assertEquals(8, progress.currentLevel)
        assertNull(progress.nextLevel)
        assertEquals(1.0f, progress.progressPercent, 0.01f)
    }

    @Test
    fun `get level name for level 1`() {
        assertEquals("Budding Saver", calculator.getLevelName(1))
    }

    @Test
    fun `get level name for level 8`() {
        assertEquals("Fino Legend", calculator.getLevelName(8))
    }
}
