package ivbackend.common.model

object Info {
    val figiHyperParameterInfo = HyperParameterInfo(
        "figi",
        "figi",
        HyperParameterInfo.Type.STRING,
    )

    val balanceHyperParameterInfo = HyperParameterInfo(
        "balance",
        "initial balance",
        HyperParameterInfo.Type.FLOAT,
        min = 0.0,
    )

}