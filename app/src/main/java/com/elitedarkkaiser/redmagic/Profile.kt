
package com.elitedarkkaiser.redmagic

data class Profile(
    val name: String,
    val fanEnabled: Boolean,
    val fanLevel: Int,
    val pumpEnabled: Boolean,
    val pumpProfile: String,
    val autoFan: Boolean
)
