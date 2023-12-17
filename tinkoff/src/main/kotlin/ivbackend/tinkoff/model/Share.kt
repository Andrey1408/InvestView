package ivbackend.tinkoff.model

import java.math.BigDecimal

data class Share(
    val figi: String,
    val name: String,
    val price: BigDecimal,
    val available: Long,
)

data class ShareQuantity(
    val figi: String,
    val quantity: Long
)