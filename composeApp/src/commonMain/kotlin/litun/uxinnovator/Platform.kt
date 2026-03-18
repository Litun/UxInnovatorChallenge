package litun.uxinnovator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform