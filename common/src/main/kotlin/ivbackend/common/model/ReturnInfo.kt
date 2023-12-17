package ivbackend.common.model

import java.time.LocalDateTime


data class ReturnInfo(
    val timestamp: LocalDateTime,
    val returnValue: Double,
)
