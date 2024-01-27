package com.nestapp.nest_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

open class UserInputExecution(
    val code: Int,
    val reason: String,
) : Exception(reason) {

    fun getBody(): Error {
        return Error(
            code,
            reason
        )
    }

    class NotFileSelectedException : UserInputExecution(
        1, "No file selected"
    )

    class CannotPlaceAllPartsIntoOneBin : UserInputExecution(
        2, "Cannot place all parts into one bin"
    )

    class SomethingWrongWithUserInput(reason: String) : UserInputExecution(
        9999, reason
    )
}

@Serializable
data class Error(
    @SerialName("code")
    val code: Int,
    @SerialName("reason")
    val reason: String,
)
