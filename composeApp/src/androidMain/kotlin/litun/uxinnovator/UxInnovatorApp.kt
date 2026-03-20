package litun.uxinnovator

import android.app.Application
import litun.uxinnovator.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class UxInnovatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(AppConfig(goRestToken = BuildConfig.GO_REST_TOKEN)) {
            androidLogger()
            androidContext(this@UxInnovatorApp)
        }
    }
}
