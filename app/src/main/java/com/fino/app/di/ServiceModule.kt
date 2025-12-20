package com.fino.app.di

import com.fino.app.data.repository.RecurringRuleRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.ml.matcher.MerchantMatcher
import com.fino.app.service.pattern.PatternDetectionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun providePatternDetectionService(
        transactionRepository: TransactionRepository,
        recurringRuleRepository: RecurringRuleRepository,
        merchantMatcher: MerchantMatcher
    ): PatternDetectionService {
        return PatternDetectionService(
            transactionRepository = transactionRepository,
            recurringRuleRepository = recurringRuleRepository,
            merchantMatcher = merchantMatcher
        )
    }
}
