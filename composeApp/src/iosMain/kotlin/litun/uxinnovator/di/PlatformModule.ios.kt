package litun.uxinnovator.di

import litun.uxinnovator.data.db.DatabaseDriverFactory
import org.koin.dsl.module

actual fun platformModule() = module {
    single { DatabaseDriverFactory() }
}
