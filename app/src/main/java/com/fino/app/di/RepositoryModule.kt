package com.fino.app.di

import com.fino.app.data.local.dao.*
import com.fino.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository {
        return TransactionRepository(dao)
    }

    @Provides
    @Singleton
    fun provideMerchantMappingRepository(dao: MerchantMappingDao): MerchantMappingRepository {
        return MerchantMappingRepository(dao)
    }

    @Provides
    @Singleton
    fun provideCreditCardRepository(dao: CreditCardDao): CreditCardRepository {
        return CreditCardRepository(dao)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(dao: BudgetDao): BudgetRepository {
        return BudgetRepository(dao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(dao: CategoryDao): CategoryRepository {
        return CategoryRepository(dao)
    }

    @Provides
    @Singleton
    fun provideUserStatsRepository(dao: UserStatsDao): UserStatsRepository {
        return UserStatsRepository(dao)
    }

    @Provides
    @Singleton
    fun provideAchievementRepository(dao: AchievementDao): AchievementRepository {
        return AchievementRepository(dao)
    }
}
