package pt.isel

import pt.isel.sample.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonParserReflectTest {

    @Test fun parseSimpleObjectViaProperties() {
        val json = "{ name: \"Ze Manel\", nr: 7353}"
        val s = JsonParserReflect.parse<Student>(json)
        assertEquals("Ze Manel", s?.name)
        assertEquals(7353, s?.nr)
    }

    @Test fun parseSimpleObjectViaPropertiesWithAdditionalNonExistingProperty() {
        val json = "{ name: \"Ze Manel\", nr: 7353, age: 18}"
        val s = JsonParserReflect.parse<Student>(json)
        assertEquals("Ze Manel", s?.name)
        assertEquals(7353, s?.nr)
    }

    @Test fun parseSimpleObjectViaPropertiesWithUnorderedProperties() {
        val json = "{nr: 7353, name: \"Ze Manel\"}"
        val s = JsonParserReflect.parse<Student>(json)
        assertEquals("Ze Manel", s?.name)
        assertEquals(7353, s?.nr)
    }

    @Test fun parseArrayOfStudentUsingSetters() {
        val json = "[{nr: 7353, name: \"Ze Manel\"}, {nr: 7354, name: \"Ze Shrek\"}, {nr: 7355, name: \"Ze Toni\"}]"
        val ss = JsonParserReflect.parseArray<Student>(json)
        assertEquals(3, ss.size)
        assertEquals("Ze Manel", ss[0]?.name)
        assertEquals("Ze Shrek", ss[1]?.name)
        assertEquals("Ze Toni", ss[2]?.name)
    }

    @Test fun parseSimpleObjectViaConstructor() {
        val json = "{ id: 94646, name: \"Ze Manel\"}"
        val p = JsonParserReflect.parse<Person>(json)
        assertEquals(94646, p?.id)
        assertEquals("Ze Manel", p?.name)
    }

    @Test fun parseComposeObject() {
        val json = "{ id: 94646, name: \"Ze Manel\", birth: { year: 1999, month: 9, day: 19}, sibling: {id: 94648, name: \"Kata Badala\"}}"
        val p = JsonParserReflect.parse<Person>(json)
        assertEquals(94646, p?.id)
        assertEquals("Ze Manel", p?.name)
        assertEquals(19, p?.birth?.day)
        assertEquals(9, p?.birth?.month)
        assertEquals(1999, p?.birth?.year)
    }

    @Test fun parseArray() {
        val json = "[{ id: 94646, name: \"Ze Manel\"}, { id: 94647, name: \"Candida Raimunda\"}, {id: 94648, name: \"Kata Mandala\"}]"
        val ps = JsonParserReflect.parseArray<Person>(json)
        assertEquals(3, ps.size)
        assertEquals("Ze Manel", ps[0]?.name)
        assertEquals("Candida Raimunda", ps[1]?.name)
        assertEquals("Kata Mandala", ps[2]?.name)
    }

    @Test fun parseClassroomWithArrayOfStudents() {
        val json = "{ name: \"Class H\", students: [{ nr: 9677, name: \"Ze Shrek\"}, { nr: 9642, name: \"Lord Farquaad\"}]}"
        val ss = JsonParserReflect.parse<Classroom>(json)
        assertEquals(2, ss?.students?.size)
        assertEquals("Class H", ss?.name)
        assertEquals("Lord Farquaad", ss?.students?.get(1)?.name)
    }

    @Test fun parseAccountWithArrayOfStrings() {
        val json = "{ balance: 20.0, transactions: [\"1 to Shrek\", \"2 to Farquaad\", \"3 to Ze\"]}"
        val ac = JsonParserReflect.parse<Account>(json)
        assertEquals("1 to Shrek", ac?.transactions?.get(0))
        assertEquals("2 to Farquaad", ac?.transactions?.get(1))
        assertEquals("3 to Ze", ac?.transactions?.get(2))
    }

    @Test fun parseListOfClassroom() {
        val json =
            "[{name: \"Class H\", students: [{ nr: 9677, name: \"Ze Shrek\"}, { nr: 9642, name: \"Lord Farquaad\"}]}, " +
                    "{students: [{nr: 1234, name: \"Fiona\"}]}]"
        val cs = JsonParserReflect.parseArray<Classroom>(json)
        assertEquals("Class H", cs[0]?.name)
        assertEquals("Class G", cs[1]?.name)
        assertEquals("Lord Farquaad", cs[0]?.students?.get(1)?.name)
        assertEquals(listOf(Student(1234, "Fiona")), cs[1]?.students)
    }

    @Test fun parseListOfStudentsWithDifferentPropertyNames() {
        val json = "[{student_number: 1234, student_name: \"Tomas Barroco\"}, {student_number: 4321, student_name: \"Alexander Woods\"}]"
        val ss = JsonParserReflect.parseArray<StudentAlternative>(json)
        assertEquals(1234, ss[0]?.nr)
        assertEquals("Tomas Barroco", ss[0]?.name)
        assertEquals(4321, ss[1]?.nr)
        assertEquals("Alexander Woods", ss[1]?.name)
    }

    @Test fun parsePersonWithDifferentPropertyNames(){
        val json = "{person_id: 410, person_name: \"Coelho Pascoa\"}"
        val p = JsonParserReflect.parse<PersonAlternative>(json)
        assertEquals(410, p?.id)
        assertEquals("Coelho Pascoa", p?.name)
    }

    @Test fun parseStudentWithDatePropertyUsingJsonConvert(){
        val json = "{ student_name: \"Maria Papoila\", student_number: 73753, birth: \"1998-11-17\" }"
        val p = JsonParserReflect.parse<StudentAlternative>(json)
        assertEquals("Maria Papoila", p?.name)
        assertEquals(73753, p?.nr)
        assertEquals(1998, p?.birth?.year)
        assertEquals(11, p?.birth?.month)
        assertEquals(17, p?.birth?.day)
    }

    @Test fun parseTeacherWithDatePropertyUsingJsonConverter(){
        val json = "{ name: \"Tomas Carvalho\", nr: 42060, birth: \"1999-01-22\" }"
        val t = JsonParserReflect.parse<Teacher>(json)
        assertEquals("Tomas Carvalho", t?.name)
        assertEquals(42060, t?.nr)
        assertEquals(1999, t?.birth?.year)
        assertEquals(1, t?.birth?.month)
        assertEquals(22, t?.birth?.day)
    }

    @Test fun parseSequenceOfLazyStudent() {
        i = 0
        var expectedI = 0
        val json = "[{nr: \"7353\", name: \"Ze Manel\"}, {nr: \"7354\", name: \"Ze Shrek\"}, {nr: \"7355\", name: \"Ze Toni\"}]"
        val students = listOf(LazyStudent(7353, "Ze Manel"), LazyStudent(7354, "Ze Shrek"), LazyStudent(7355, "Ze Toni"))
        val ss = JsonParserReflect.parseSequence<LazyStudent>(json)
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
        val ss = JsonParserReflect.parseFolderEager<Student>(path)
        val expected = listOf(Student(48000, "Student 0"), Student(48001, "Student 1"), Student(48002, "Student 2"))
        assertEquals(expected, ss)
    }

    @Test fun parseFolderLazy() {
        setupFiles()
        val path = "src/test/resources"
        val ss = JsonParserReflect.parseFolderLazy<Student>(path)
        val expected = sequenceOf(Student(48000, "Student 0"), Student(48001, "Student 1"), Student(48002, "Student 2"))
        assertContentEquals(expected, ss)
    }

    @Test fun parseFolderEagerWithFileChange() {
        setupFiles()
        val path = "src/test/resources"
        val s = JsonParserReflect.parseFolderEager<Student>(path)
        changeFile("src/test/resources/Student1.txt", "{nr: 48300, name: \"Alexander German Woods\"}")
        val expected = listOf(Student(48000, "Student 0"), Student(48001, "Student 1"), Student(48002, "Student 2"))
        assertEquals(expected, s)
    }

    @Test fun parseFolderLazyWithFileChange() {
        setupFiles()
        val path = "src/test/resources"
        val s = JsonParserReflect.parseFolderLazy<Student>(path)
        changeFile("src/test/resources/Student1.txt", "{nr: 48300, name: \"Alexander German Woods\"}")
        val expected = sequenceOf(Student(48000, "Student 0"), Student(48300, "Alexander German Woods"), Student(48002, "Student 2"))
        assertContentEquals(expected, s)
    }

    @Test fun parseIncompatibleFileLazy() {
        val exception = assertFailsWith<Exception> {
            setupFiles()
            val path = "src/test/resources"
            JsonParserReflect.parseFolderLazy<Person>(path).forEach { _ -> }
        }
        assertEquals("Object is not compatible", exception.message)
    }

    @Test fun parseIncompatibleFileEager() {
        assertFailsWith<Exception> {
            setupFiles()
            val path = "src/test/resources"
            JsonParserReflect.parseFolderEager<Person>(path)
        }
    }

}
