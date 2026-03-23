package litun.uxinnovator.di

import litun.uxinnovator.AppConfig
import litun.uxinnovator.data.api.GoRestApiService
import litun.uxinnovator.data.api.UserApiService
import litun.uxinnovator.data.db.AppDatabase
import litun.uxinnovator.data.db.DatabaseDriverFactory
import litun.uxinnovator.data.repository.UserRepositoryImpl
import litun.uxinnovator.domain.repository.UserRepository
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect fun platformModule(): Module

fun initKoin(config: AppConfig, appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(
            platformModule(),
            module {
                single { config }
                single<UserApiService> { GoRestApiService(token = get<AppConfig>().goRestToken) }
                single { AppDatabase(get<DatabaseDriverFactory>().createDriver()) }
                single<UserRepository> { UserRepositoryImpl(get(), get()) }
            },
        )
    }
