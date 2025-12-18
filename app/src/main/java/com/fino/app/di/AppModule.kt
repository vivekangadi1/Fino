package com.fino.app.di

import android.content.Context
import com.fino.app.gamification.LevelCalculator
import com.fino.app.gamification.XpCalculator
import com.fino.app.ml.matcher.FuzzyMatcher
import com.fino.app.service.parser.ParserFactory
import com.fino.app.service.parser.SmsParser
import com.fino.app.service.sms.AndroidSmsReader
import com.fino.app.service.sms.SmsReader
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideParserFactory(): ParserFactory {
        return ParserFactory()
    }

    @Provides
    @Singleton
    fun provideFuzzyMatcher(): FuzzyMatcher {
        return FuzzyMatcher()
    }

    @Provides
    @Singleton
    fun provideLevelCalculator(): LevelCalculator {
        return LevelCalculator()
    }

    @Provides
    @Singleton
    fun provideXpCalculator(): XpCalculator {
        return XpCalculator()
    }

    @Provides
    @Singleton
    fun provideSmsParser(): SmsParser {
        return SmsParser()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SmsModule {

    @Binds
    @Singleton
    abstract fun bindSmsReader(impl: AndroidSmsReader): SmsReader
}
