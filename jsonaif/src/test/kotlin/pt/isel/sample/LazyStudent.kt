package pt.isel.sample

import pt.isel.JsonConvert

data class LazyStudent (
    @property:JsonConvert(ConverterNr::class) var nr: Int = 0,
    var name: String? = null,
    var birth: Date? = null
)


