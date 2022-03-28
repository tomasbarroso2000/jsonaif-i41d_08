package pt.isel.sample

import pt.isel.JsonProperty

data class Person (
    @JsonProperty("person_id") val id: Int,
    @JsonProperty("person_name") val name: String,
    val birth: Date? = null,
    var sibling: Person? = null
)
