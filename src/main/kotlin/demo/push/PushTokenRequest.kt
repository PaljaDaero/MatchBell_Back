package demo.push

data class PushTokenRequest(
    val token: String,
    val platform: PushPlatform? = null
)
