package pt.isel

import pt.isel.sample.*
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonParserReflectTest {

    @Test fun parseSimpleObjectViaProperties() {
        val json = "{ name: \"Ze Manel\", nr: 7353}"
        val student = JsonParserReflect.parse(json, Student::class) as Student
        assertEquals("Ze Manel", student.name)
        assertEquals(7353, student.nr)
    }

    @Test fun parseSimpleObjectViaPropertiesWithAdditionalNonExistingProperty() {
        val json = "{ name: \"Ze Manel\", nr: 7353, age: 18}"
        val student = JsonParserReflect.parse(json, Student::class) as Student
        assertEquals("Ze Manel", student.name)
        assertEquals(7353, student.nr)
    }

    @Test fun parseSimpleObjectViaPropertiesWithUnorderedProperties() {
        val json = "{nr: 7353, name: \"Ze Manel\"}"
        val student = JsonParserReflect.parse(json, Student::class) as Student
        assertEquals("Ze Manel", student.name)
        assertEquals(7353, student.nr)
    }

    @Test fun parseArrayOfStudentUsingSetters() {
        val json = "[{nr: 7353, name: \"Ze Manel\"}, {nr: 7354, name: \"Ze Shrek\"}, {nr: 7355, name: \"Ze Toni\"}]"
        val ps = JsonParserReflect.parse(json, Student::class) as List<Student>
        assertEquals(3, ps.size)
        assertEquals("Ze Manel", ps[0].name)
        assertEquals("Ze Shrek", ps[1].name)
        assertEquals("Ze Toni", ps[2].name)
    }

    @Test fun parseSimpleObjectViaConstructor() {
        val json = "{ id: 94646, name: \"Ze Manel\"}"
        val p = JsonParserReflect.parse(json, Person::class) as Person
        assertEquals(94646, p.id)
        assertEquals("Ze Manel", p.name)
    }

    @Test fun parseComposeObject() {
        val json = "{ id: 94646, name: \"Ze Manel\", birth: { year: 1999, month: 9, day: 19}, sibling: {id: 94648, name: \"Kata Badala\"}}"
        val p = JsonParserReflect.parse(json, Person::class) as Person
        assertEquals(94646, p.id)
        assertEquals("Ze Manel", p.name)
        assertEquals(19, p.birth?.day)
        assertEquals(9, p.birth?.month)
        assertEquals(1999, p.birth?.year)
    }

    @Test fun parseArray() {
        val json = "[{ id: 94646, name: \"Ze Manel\"}, { id: 94647, name: \"Candida Raimunda\"}, {id: 94648, name: \"Kata Mandala\"}]"
        val ps = JsonParserReflect.parse(json, Person::class) as List<Person>
        assertEquals(3, ps.size)
        assertEquals("Ze Manel", ps[0].name)
        assertEquals("Candida Raimunda", ps[1].name)
        assertEquals("Kata Mandala", ps[2].name)
    }

    @Test fun parseClassroomWithArrayOfStudents() {
        val json = "{ name: \"Class H\", students: [{ nr: 9677, name: \"Ze Shrek\"}, { nr: 9642, name: \"Lord Farquaad\"}]}"
        val ps = JsonParserReflect.parse(json, Classroom::class) as Classroom
        assertEquals(2, ps.students.size)
        assertEquals("Class H", ps.name)
        assertEquals("Lord Farquaad", ps.students[1].name)
    }

    @Test fun parseAccountWithArrayOfStrings() {
        val json = "{ balance: 20.0, transactions: [\"1 to Shrek\", \"2 to Farquaad\", \"3 to Ze\"]}"
        val ps = JsonParserReflect.parse(json, Account::class) as Account
        assertEquals("1 to Shrek", ps.transactions[0])
        assertEquals("2 to Farquaad", ps.transactions[1])
        assertEquals("3 to Ze", ps.transactions[2])
    }

    @Test fun parseListOfClassroom() {
        val json =
            "[{name: \"Class H\", students: [{ nr: 9677, name: \"Ze Shrek\"}, { nr: 9642, name: \"Lord Farquaad\"}]}, " +
                    "{students: [{nr: 1234, name: \"Fiona\"}]}]"
        val ps = JsonParserReflect.parse(json, Classroom::class) as List<Classroom>
        assertEquals("Class H", ps[0].name)
        assertEquals("Class G", ps[1].name)
        assertEquals("Lord Farquaad", ps[0].students[1].name)
        assertEquals(listOf(Student(1234, "Fiona")), ps[1].students)
    }

    @Test fun parseListOfStudentsWithDifferentPropertyNames() {
        val json = "[{student_number: 1234, student_name: \"Tomas Barroco\"}, {student_number: 4321, student_name: \"Alexander Woods\"}]"
        val ps = JsonParserReflect.parse(json, StudentAlternative::class) as List<StudentAlternative>
        assertEquals(1234, ps[0].nr)
        assertEquals("Tomas Barroco", ps[0].name)
        assertEquals(4321, ps[1].nr)
        assertEquals("Alexander Woods", ps[1].name)
    }

    @Test fun parsePersonWithDifferentPropertyNames(){
        val json = "{person_id: 410, person_name: \"Coelho Pascoa\"}"
        val ps = JsonParserReflect.parse(json, PersonAlternative::class) as PersonAlternative
        assertEquals(410, ps.id)
        assertEquals("Coelho Pascoa", ps.name)
    }

    @Test fun parseStudentWithDatePropertyUsingJsonConvert(){
        val json = "{ student_name: \"Maria Papoila\", student_number: 73753, birth: \"1998-11-17\" }"
        val ps = JsonParserReflect.parse(json, StudentAlternative::class) as StudentAlternative
        assertEquals("Maria Papoila", ps.name)
        assertEquals(73753, ps.nr)
        assertEquals(1998, ps.birth?.year)
        assertEquals(11, ps.birth?.month)
        assertEquals(17, ps.birth?.day)
    }

    @Test fun parseTeacherWithDatePropertyUsingJsonConverter(){
        val json = "{ name: \"Tomas Carvalho\", nr: 42060, birth: \"1999-01-22\" }"
        val ps = JsonParserReflect.parse(json, Teacher::class) as Teacher
        assertEquals("Tomas Carvalho", ps.name)
        assertEquals(42060, ps.nr)
        assertEquals(1999, ps.birth.year)
        assertEquals(1, ps.birth.month)
        assertEquals(22, ps.birth.day)
    }
}
