package pt.isel

fun parsePerson(json: String, parser: JsonParser) : Person? {
    return parser.parse(json, Person::class)
}

fun parseDate(json: String, parser: JsonParser) : Date? {
    return parser.parse(json)
}

fun parseStudent(json: String, parser: JsonParser) : Student? {
    return parser.parse(json)
}

