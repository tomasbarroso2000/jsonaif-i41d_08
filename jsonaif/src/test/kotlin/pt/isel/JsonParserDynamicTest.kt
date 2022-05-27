package pt.isel

import org.junit.Test
import pt.isel.sample.*
import java.io.File
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonParserDynamicTest {
    @Test fun parseSimpleObjectViaProperties() {
        val json = "{ name: \"Ze Manel\", nr: 7353}"
        val s = JsonParserDynamic.parse<Student>(json)
        assertEquals("Ze Manel", s?.name)
        assertEquals(7353, s?.nr)
    }

    @Test fun parseSimpleObjectViaPropertiesWithAdditionalNonExistingProperty() {
        val json = "{ name: \"Ze Manel\", nr: 7353, age: 18}"
        val s = JsonParserDynamic.parse<Student>(json)
        assertEquals("Ze Manel", s?.name)
        assertEquals(7353, s?.nr)
    }

    @Test fun parseSimpleObjectViaPropertiesWithUnorderedProperties() {
        val json = "{nr: 7353, name: \"Ze Manel\"}"
        val s = JsonParserDynamic.parse<Student>(json)
        assertEquals("Ze Manel", s?.name)
        assertEquals(7353, s?.nr)
    }

    @Test fun parseObjectWithImmutableProperties() {
        val json = "{ id: 2, name: \"Lord Farquaad\"}"
        val exception = assertFailsWith<Exception> {
            JsonParserDynamic.parse<Person>(json)
        }
        assertEquals("The constructor parameters are not optional", exception.message)
    }

    @Test fun parseArrayOfStudentUsingSetters() {
        val json = "[{nr: 7353, name: \"Ze Manel\"}, {nr: 7354, name: \"Ze Shrek\"}, {nr: 7355, name: \"Ze Toni\"}]"
        val ss = JsonParserDynamic.parseArray<Student>(json)
        assertEquals(3, ss.size)
        assertEquals("Ze Manel", ss[0]?.name)
        assertEquals("Ze Shrek", ss[1]?.name)
        assertEquals("Ze Toni", ss[2]?.name)
    }

    @Test fun parseClassroomWithArrayOfStudents() {
        val json = "{ name: \"Class H\", students: [{ nr: 9677, name: \"Ze Shrek\"}, { nr: 9642, name: \"Lord Farquaad\"}]}"
        val c = JsonParserDynamic.parse<Classroom>(json)
        assertEquals(2, c?.students?.size)
        assertEquals("Class H", c?.name)
        assertEquals("Lord Farquaad", c?.students?.get(1)?.name)
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
        val ss = JsonParserDynamic.parseArray<StudentAlternative>(json)
        assertEquals(1234, ss[0]?.nr)
        assertEquals("Tomas Barroco", ss[0]?.name)
        assertEquals(4321, ss[1]?.nr)
        assertEquals("Alexander Woods", ss[1]?.name)
    }

    @Test fun parseStudentWithDatePropertyUsingJsonConvert(){
        val json = "{ student_name: \"Maria Papoila\", student_number: 73753, birth: \"1998-11-17\" }"
        val s = JsonParserDynamic.parse<StudentAlternative>(json)
        assertEquals("Maria Papoila", s?.name)
        assertEquals(73753, s?.nr)
        assertEquals(1998, s?.birth?.year)
        assertEquals(11, s?.birth?.month)
        assertEquals(17, s?.birth?.day)
    }

    @Test fun parseDateAlternative(){
        val json = "{day: 12, month: 12, year: 2012}"
        val dt = JsonParserDynamic.parse<DateAlternative>(json)
        assertEquals(12, dt?.day)
        assertEquals(12, dt?.month)
        assertEquals(2012, dt?.year)
    }

    @Test fun parseSequenceOfLazyStudent() {
        i = 0
        var expectedI = 0
        val json = "[{nr: \"7353\", name: \"Ze Manel\"}, {nr: \"7354\", name: \"Ze Shrek\"}, {nr: \"7355\", name: \"Ze Toni\"}]"
        val students = listOf(LazyStudent(7353, "Ze Manel"), LazyStudent(7354, "Ze Shrek"), LazyStudent(7355, "Ze Toni"))
        val ss = JsonParserDynamic.parseSequence<LazyStudent>(json)
        val iterator = ss.iterator()
        while (iterator.hasNext()) {
            assertEquals(students[expectedI], iterator.next())
            expectedI++
            assertEquals(expectedI, i)
        }
    }

    @Test fun parseFolderEager() {
        setupFiles()
        val path = "src/test/resources"
        val s = JsonParserDynamic.parseFolderEager<Student>(path)
        val expected = listOf(Student(48000, "Student 0"), Student(48001, "Student 1"), Student(48002, "Student 2"))
        assertEquals(expected, s)
    }

    @Test fun parseFolderLazy() {
        setupFiles()
        val path = "src/test/resources"
        val s = JsonParserDynamic.parseFolderLazy<Student>(path)
        val expected = sequenceOf(Student(48000, "Student 0"), Student(48001, "Student 1"), Student(48002, "Student 2"))
        assertContentEquals(expected, s)
    }

    @Test fun parseFolderEagerWithFileChange() {
        setupFiles()
        val path = "src/test/resources"
        val s = JsonParserDynamic.parseFolderEager<Student>(path)
        changeFile("src/test/resources/Student1.txt", "{nr: 48300, name: \"Alexander German Woods\"}")
        val expected = listOf(Student(48000, "Student 0"), Student(48001, "Student 1"), Student(48002, "Student 2"))
        assertEquals(expected, s)
    }

    @Test fun parseFolderLazyWithFileChange() {
        setupFiles()
        val path = "src/test/resources"
        val s = JsonParserDynamic.parseFolderLazy<Student>(path)
        changeFile("src/test/resources/Student1.txt", "{nr: 48300, name: \"Alexander German Woods\"}")
        val expected = sequenceOf(
            Student(48000, "Student 0"),
            Student(48300, "Alexander German Woods"),
            Student(48002, "Student 2")
        )
        assertContentEquals(expected, s)
    }
}