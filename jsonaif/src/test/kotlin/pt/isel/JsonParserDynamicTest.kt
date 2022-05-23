package pt.isel

import org.junit.Test
import pt.isel.sample.*
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class JsonParserDynamicTest {
    @Test fun parseSimpleObjectViaProperties() {
        val json = "{ name: \"Ze Manel\", nr: 7353}"
        val student = JsonParserDynamic.parse<Student>(json)
        assertEquals("Ze Manel", student?.name)
        assertEquals(7353, student?.nr)
    }

    @Test fun parseSimpleObjectViaPropertiesWithAdditionalNonExistingProperty() {
        val json = "{ name: \"Ze Manel\", nr: 7353, age: 18}"
        val student = JsonParserDynamic.parse<Student>(json)
        assertEquals("Ze Manel", student?.name)
        assertEquals(7353, student?.nr)
    }

    @Test fun parseSimpleObjectViaPropertiesWithUnorderedProperties() {
        val json = "{nr: 7353, name: \"Ze Manel\"}"
        val student = JsonParserDynamic.parse<Student>(json)
        assertEquals("Ze Manel", student?.name)
        assertEquals(7353, student?.nr)
    }

    @Test fun parseArrayOfStudentUsingSetters() {
        val json = "[{nr: 7353, name: \"Ze Manel\"}, {nr: 7354, name: \"Ze Shrek\"}, {nr: 7355, name: \"Ze Toni\"}]"
        val ps = JsonParserDynamic.parseArray<Student>(json)
        assertEquals(3, ps.size)
        assertEquals("Ze Manel", ps[0]?.name)
        assertEquals("Ze Shrek", ps[1]?.name)
        assertEquals("Ze Toni", ps[2]?.name)
    }

    @Test fun parseClassroomWithArrayOfStudents() {
        val json = "{ name: \"Class H\", students: [{ nr: 9677, name: \"Ze Shrek\"}, { nr: 9642, name: \"Lord Farquaad\"}]}"
        val ps = JsonParserDynamic.parse<Classroom>(json)
        assertEquals(2, ps?.students?.size)
        assertEquals("Class H", ps?.name)
        assertEquals("Lord Farquaad", ps?.students?.get(1)?.name)
    }

    @Test fun parseListOfClassroom() {
        val json =
            "[{name: \"Class H\", students: [{ nr: 9677, name: \"Ze Shrek\"}, { nr: 9642, name: \"Lord Farquaad\"}]}, " +
                    "{students: [{nr: 1234, name: \"Fiona\"}]}]"
        val ps = JsonParserDynamic.parseArray<Classroom>(json)
        assertEquals("Class H", ps[0]?.name)
        assertEquals("Class G", ps[1]?.name)
        assertEquals("Lord Farquaad", ps[0]?.students?.get(1)?.name)
        assertEquals(listOf(Student(1234, "Fiona")), ps[1]?.students)
    }

    @Test fun parseListOfStudentsWithDifferentPropertyNames() {
        val json = "[{student_number: 1234, student_name: \"Tomas Barroco\"}, {student_number: 4321, student_name: \"Alexander Woods\"}]"
        val ps = JsonParserDynamic.parseArray<StudentAlternative>(json)
        assertEquals(1234, ps[0]?.nr)
        assertEquals("Tomas Barroco", ps[0]?.name)
        assertEquals(4321, ps[1]?.nr)
        assertEquals("Alexander Woods", ps[1]?.name)
    }

    @Test fun parseStudentWithDatePropertyUsingJsonConvert(){
        val json = "{ student_name: \"Maria Papoila\", student_number: 73753, birth: \"1998-11-17\" }"
        val ps = JsonParserDynamic.parse<StudentAlternative>(json)
        assertEquals("Maria Papoila", ps?.name)
        assertEquals(73753, ps?.nr)
        assertEquals(1998, ps?.birth?.year)
        assertEquals(11, ps?.birth?.month)
        assertEquals(17, ps?.birth?.day)
    }

    @Test fun parseDateAlternative(){
        val json = "{day: 12, month: 12, year: 2012}"
        val ps = JsonParserDynamic.parse<DateAlternative>(json)
        assertEquals(12, ps?.day)
        assertEquals(12, ps?.month)
        assertEquals(2012, ps?.year)
    }

    @Test fun parseSequenceOfStudentUsingSetters() {
        val json = "[{nr: 7353, name: \"Ze Manel\"}, {nr: 7354, name: \"Ze Shrek\"}, {nr: 7355, name: \"Ze Toni\"}]"
        val ps = JsonParserDynamic.parseSequence<Student>(json)
        val expected = sequenceOf(Student(7353, "Ze Manel"), Student(7354, "Ze Shrek"), Student(7355, "Ze Toni"))
        assertContentEquals(expected, ps)
    }
}