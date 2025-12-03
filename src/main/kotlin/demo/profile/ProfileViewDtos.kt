package demo.profile

import demo.profile.Gender
import demo.saju.CompatResponse
import java.time.LocalDate


data class BasicProfileInfo(
    val userId: Long,
    val nickname: String,
    val age: Int,
    val region: String?,
    val avatarUrl: String?,
    val shortIntro: String?,   
    val tendency: String?     
)


data class DetailProfileInfo(
    val gender: Gender,
    val birth: LocalDate,
    val job: String?,
    val intro: String?,   
    val compat: CompatResponse?   
)

data class ProfileViewResponse(
    val basic: BasicProfileInfo,
    val detail: DetailProfileInfo?, 

    val isSelf: Boolean,     
    val isMatched: Boolean,  
    val hasUnlocked: Boolean,

    val canChat: Boolean,    
    val canUnlock: Boolean  
)
