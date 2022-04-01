package pt.isel.sample

import pt.isel.JsonConvert
import pt.isel.JsonProperty

data class StudentAlternative (
    @property:JsonProperty("student_number") var nr: Int = 0,
    @property:JsonProperty("student_name") var name: String? = null,
    @property:JsonConvert(ConverterDate::class) var birth: Date? = null
)