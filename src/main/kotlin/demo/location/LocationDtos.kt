package demo.location

/**
 * 클라이언트에서 보내는 위치 업데이트 요청
 * - lat, lng: 단말 Fused Location 에서 얻은 위도/경도
 * - region: (선택) 단말에서 역지오코딩한 지역명 (예: "서울시 마포구")
 */
data class LocationUpdateRequest(
    val lat: Double,
    val lng: Double,
    val region: String? = null
)
