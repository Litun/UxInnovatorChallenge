package litun.uxinnovator

actual fun getTestEnv(key: String): String? =
    System.getProperty(key) ?: System.getenv(key)
