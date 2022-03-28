package pt.isel.sample

import pt.isel.JsonProperty

data class Student (
    @JsonProperty("student_number") var nr: Int = 0,
    @JsonProperty("student_name") var name: String? = null
)
