package demo.profile


data class ProfileUnlockResult(
    val unlocked: Boolean,       // 이번 요청으로 잠금 해제되었는지
    val alreadyUnlocked: Boolean, // 이미 잠금 해제된 상태였는지
    val cost: Long,            // 잠금 해제에 사용된 코스트
    val balanceAfter: Long?  
)
