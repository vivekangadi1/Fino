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

    @Provides
    @Singleton
    fun provideRecurringRuleRepository(dao: RecurringRuleDao): RecurringRuleRepository {
        return RecurringRuleRepository(dao)
    }

    @Provides
    @Singleton
    fun provideUpcomingBillsRepository(
        recurringRuleRepository: RecurringRuleRepository,
        creditCardRepository: CreditCardRepository,
        patternDetectionService: com.fino.app.service.pattern.PatternDetectionService,
        transactionRepository: TransactionRepository
    ): UpcomingBillsRepository {
        return UpcomingBillsRepository(
            recurringRuleRepository = recurringRuleRepository,
            creditCardRepository = creditCardRepository,
            patternDetectionService = patternDetectionService,
            transactionRepository = transactionRepository
        )
    }

    @Provides
    @Singleton
    fun provideEventTypeRepository(dao: EventTypeDao): EventTypeRepository {
        return EventTypeRepository(dao)
    }

    @Provides
    @Singleton
    fun provideEventRepository(
        eventDao: EventDao,
        transactionRepository: TransactionRepository,
        eventTypeRepository: EventTypeRepository
    ): EventRepository {
        return EventRepository(eventDao, transactionRepository, eventTypeRepository)
    }
}
