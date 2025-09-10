package com.example.leofindit.http.Data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResultDto(
    @SerialName("id") val id : String,
    @SerialName("name") val name : String

)