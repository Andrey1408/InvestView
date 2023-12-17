package ivbackend.common.model


data class HyperParameterInfo(
    val name: String,
    val description: String,
    val type: Type,
    val min: Double? = null,
    val max: Double? = null,
) {
    enum class Type {
        INT,
        FLOAT,
        STRING,
    }
}