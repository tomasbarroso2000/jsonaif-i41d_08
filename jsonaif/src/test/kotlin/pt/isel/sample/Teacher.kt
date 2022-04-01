package pt.isel.sample

import pt.isel.JsonConvert

data class Teacher(
    val nr: Int,
    val name: String,
    @param:JsonConvert(JsonToDate::class) val birth: Date
)