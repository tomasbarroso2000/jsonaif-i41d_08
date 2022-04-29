package pt.isel

import org.junit.Test
import pt.isel.sample.*
import kotlin.test.assertEquals

class JsonParserDynamicTest {
    @Test fun parseSimpleObjectViaProperties() {
        val json = "{ name: \"Ze Manel\", nr: 7353}"
        val student = JsonParserDynamic.parse(json, Student::class) as Student
        assertEquals("Ze Manel", student.name)
        assertEquals(7353, student.nr)
    }

    @Test fun parseSimpleObjectViaPropertiesWithAdditionalNonExistingProperty() {
        val json = "{ name: \"Ze Manel\", nr: 7353, age: 18}"
        val student = JsonParserDynamic.parse(json, Student::class) as Student
        assertEquals("Ze Manel", student.name)
        assertEquals(7353, student.nr)
    }

    @Test fun parseSimpleObjectViaPropertiesWithUnorderedProperties() {
        val json = "{nr: 7353, name: \"Ze Manel\"}"
        val student = JsonParserDynamic.parse(json, Student::class) as Student
        assertEquals("Ze Manel", student.name)
        assertEquals(7353, student.nr)
    }

    @Test fun parseArrayOfStudentUsingSetters() {
        val json = "[{nr: 7353, name: \"Ze Manel\"}, {nr: 7354, name: \"Ze Shrek\"}, {nr: 7355, name: \"Ze Toni\"}]"
        val ps = JsonParserDynamic.parse(json, Student::class) as List<Student>
        assertEquals(3, ps.size)
        assertEquals("Ze Manel", ps[0].name)
        assertEquals("Ze Shrek", ps[1].name)
        assertEquals("Ze Toni", ps[2].name)
    }

    @Test fun parseClassroomWithArrayOfStudents() {
        val json = "{ name: \"Class H\", students: [{ nr: 9677, name: \"Ze Shrek\"}, { nr: 9642, name: \"Lord Farquaad\"}]}"
        val ps = JsonParserDynamic.parse(json, Classroom::class) as Classroom
        assertEquals(2, ps.students.size)
        assertEquals("Class H", ps.name)
        assertEquals("Lord Farquaad", ps.students[1].name)
    }

    @Test fun parseListOfClassroom() {
        val json =
            "[{name: \"Class H\", students: [{ nr: 9677, name: \"Ze Shrek\"}, { nr: 9642, name: \"Lord Farquaad\"}]}, " +
                    "{students: [{nr: 1234, name: \"Fiona\"}]}]"
        val ps = JsonParserDynamic.parse(json, Classroom::class) as List<Classroom>
        assertEquals("Class H", ps[0].name)
        assertEquals("Class G", ps[1].name)
        assertEquals("Lord Farquaad", ps[0].students[1].name)
        assertEquals(listOf(Student(1234, "Fiona")), ps[1].students)
    }

    @Test fun parseListOfStudentsWithDifferentPropertyNames() {
        val json = "[{student_number: 1234, student_name: \"Tomas Barroco\"}, {student_number: 4321, student_name: \"Alexander Woods\"}]"
        val ps = JsonParserDynamic.parse(json, StudentAlternative::class) as List<StudentAlternative>
        assertEquals(1234, ps[0].nr)
        assertEquals("Tomas Barroco", ps[0].name)
        assertEquals(4321, ps[1].nr)
        assertEquals("Alexander Woods", ps[1].name)
    }

    @Test fun parseStudentWithDatePropertyUsingJsonConvert(){
        val json = "{ student_name: \"Maria Papoila\", student_number: 73753, birth: \"1998-11-17\" }"
        val ps = JsonParserDynamic.parse(json, StudentAlternative::class) as StudentAlternative
        assertEquals("Maria Papoila", ps.name)
        assertEquals(73753, ps.nr)
        assertEquals(1998, ps.birth?.year)
        assertEquals(11, ps.birth?.month)
        assertEquals(17, ps.birth?.day)
    }
}