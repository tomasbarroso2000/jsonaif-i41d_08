package pt.isel.sample

import pt.isel.Converter

var i = 0

class ConverterNr : Converter {
    override fun convert(str: String): Int {
        i++
        return str.toInt()
    }
}