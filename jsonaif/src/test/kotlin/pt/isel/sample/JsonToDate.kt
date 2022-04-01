package pt.isel.sample

import pt.isel.JsonTo

class JsonToDate {

    fun convert(str: String): Date {
        val splitStr = str.split('-')
        return Date(splitStr[2].toInt(), splitStr[1].toInt(), splitStr[0].toInt())
    }

}
