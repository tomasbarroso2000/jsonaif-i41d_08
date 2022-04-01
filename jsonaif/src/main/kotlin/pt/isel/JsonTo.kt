package pt.isel

interface JsonTo<T> {
    fun convert(str: String): T
}