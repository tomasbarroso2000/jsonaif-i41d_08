package pt.isel.sample

import pt.isel.Converter

class ConverterNr : Converter {
    override fun convert(str: String): Int {
        println("Parsing Nr: $str")
        return str.toInt()
    }
}