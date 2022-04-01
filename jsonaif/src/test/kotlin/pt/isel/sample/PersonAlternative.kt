package pt.isel.sample

import pt.isel.JsonProperty

data class PersonAlternative(
    @param:JsonProperty("person_id") val id: Int,
    @param:JsonProperty("person_name") val name: String,
    val birth: Date? = null,
    var sibling: Person? = null
)