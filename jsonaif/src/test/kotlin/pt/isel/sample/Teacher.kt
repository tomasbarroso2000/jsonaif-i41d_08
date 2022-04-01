package pt.isel.sample

import pt.isel.JsonConvert

data class Teacher(
    val nr: Int,
    val name: String,
    @param:JsonConvert(ConverterDate::class) val birth: Date
)