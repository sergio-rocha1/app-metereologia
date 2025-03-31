package com.ufu.trabalho.model

import com.google.gson.annotations.SerializedName

data class ReverseGeocodeResponse(
    val address: Address?
)

data class Address(
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("town")
    val town: String? = null,
    @SerializedName("village")
    val village: String? = null
)
