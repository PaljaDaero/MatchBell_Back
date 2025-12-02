package demo.file

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.exists

@Service
class FileStorageService(
    @Value("\${file.upload-dir}") private val uploadDir: String
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    /**
     * 프로필 이미지 저장
     * - userId + timestamp 기반 파일명 생성
     * - /images/profile/{filename} 형태의 URL 리턴
     */
    fun saveProfileImage(userId: Long, file: MultipartFile): String {
        if (file.isEmpty) {
            throw IllegalArgumentException("빈 파일입니다.")
        }

        // MIME 타입 체크 (간단 버전)
        val contentType = file.contentType ?: ""
        if (!contentType.startsWith("image/")) {
            throw IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.")
        }

        // 확장자 추출 (jpg, png 등)
        val originalName = file.originalFilename ?: "profile"
        val ext = originalName.substringAfterLast('.', "")

        // 저장 디렉토리 생성
        val dirPath: Path = Paths.get(uploadDir).toAbsolutePath().normalize()
        if (!dirPath.exists()) {
            Files.createDirectories(dirPath)
        }

        // 파일명: userId_yyyyMMddHHmmss.ext
        val timestamp = LocalDateTime.now().format(dateFormatter)
        val safeExt = if (ext.isBlank()) "jpg" else ext
        val filename = "u${userId}_${timestamp}.$safeExt"

        val targetPath = dirPath.resolve(filename)
        file.inputStream.use { input ->
            Files.copy(input, targetPath)
        }

        // 브라우저에서 접근할 수 있는 URL 리턴
        // WebConfig에서 /images/profile/** -> uploadDir 매핑할 예정
        return "/images/profile/$filename"
    }
}
