package ivbackend.tinkoff.model

import java.math.BigDecimal

data class Account(
    val id: Long,
    val username: String,
    val password: String,
    val balance: BigDecimal,
    val token: String
)

data class UserForm(val fullName: String)

data class TopUpBalanceRequest(val amount: BigDecimal)