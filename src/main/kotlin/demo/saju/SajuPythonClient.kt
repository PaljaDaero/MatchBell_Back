package demo.saju

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SajuPythonClient(
    @Value("\${saju.python.executable}") private val pythonExecutable: String,
    @Value("\${saju.python.script-path}") private val scriptPath: String,
    private val objectMapper: ObjectMapper,
) {

    fun match(req: SajuMatchRequest): SajuMatchResult {
        val process = ProcessBuilder(pythonExecutable, scriptPath)
            //  환경변수 설정 가능
            .start()

        //  입력 쓰기
        process.outputStream.bufferedWriter().use { writer ->
            val json = objectMapper.writeValueAsString(req)
            writer.write(json)
            writer.flush()
        }

        // 결과 읽기
        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        println(">>> [SAJU] pythonExecutable = $pythonExecutable")
        println(">>> [SAJU] scriptPath      = $scriptPath")
        println(">>> [SAJU] exitCode        = $exitCode")
        println(">>> [SAJU] stdout:\n$stdout")
        println(">>> [SAJU] stderr:\n$stderr")

        // Python 실패
        if (exitCode != 0) {
            throw IllegalStateException("Python saju script failed: $stderr\n$stdout")
        }

        // stdout 의 마지막 줄이 JSON 결과라고 가정
        val lastLine = stdout
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .lastOrNull()
            ?: throw IllegalStateException("Python saju returned empty output")

        // JSON 이 error 필드를 가지면 예외 처리
        val node = objectMapper.readTree(lastLine)
        if (node.has("error")) {
            val msg = node.get("error").asText()
            throw IllegalStateException("Python saju error: $msg")
        }

        // JSON -> SajuMatchResult
        return objectMapper.treeToValue(node, SajuMatchResult::class.java)
    }
}
