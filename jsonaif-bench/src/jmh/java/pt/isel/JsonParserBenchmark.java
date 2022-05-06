package pt.isel;

import kotlin.reflect.KClass;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

import static pt.isel.JsonParserObjectsKt.*;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class JsonParserBenchmark {
    static final String person = "{ name: \"Ze Manel\", sibling: {name: \"Ze Shrek\"} }";
    static final String date = "{ day: 12, month: 12, year: 12}";
    static final String student = "{ nr: 12, name: \"Ze Fiona\"}";
    static final JsonParserReflect jsonParserReflect = JsonParserReflect.INSTANCE;
    static final JsonParserDynamic jsonParserDynamic = JsonParserDynamic.INSTANCE;

    @Benchmark
    public Person benchJsonParserPersonViaReflection() { return parsePerson(person, jsonParserReflect); }

    @Benchmark
    public Person benchJsonParserPersonViaDynamic() {
        return parsePerson(person, jsonParserDynamic);
    }

    @Benchmark
    public Date benchJsonParserDateViaReflection() { return parseDate(date, jsonParserReflect); }

    @Benchmark
    public Date benchJsonParserDateViaDynamic() { return parseDate(date, jsonParserDynamic); }

    @Benchmark
    public Student benchJsonParserStudentViaReflection() { return parseStudent(student, jsonParserReflect); }

    @Benchmark
    public Student benchJsonParserStudentViaDynamic() { return parseStudent(student, jsonParserDynamic); }

}
