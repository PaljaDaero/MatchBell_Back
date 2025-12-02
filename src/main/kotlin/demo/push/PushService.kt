package demo.push

import org.springframework.stereotype.Service

@Service
class PushService {
    /**
     * 단일 토큰으로 푸시 알림 전송
     */
    fun sendToToken(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>? = null
    ) {
        // FCM 푸시 알림 전송 로직 구현
    }

}
