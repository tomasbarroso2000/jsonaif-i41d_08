package pt.isel;

import kotlin.reflect.KClass;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class JsonParserBenchmark {
    static final String json = "{ name: \"Ze Manel\", sibling: {name: \"Ze Shrek\"} }";
    static final JsonParserReflect jsonParserReflect = JsonParserReflect.INSTANCE;

    static final JsonParserDynamic jsonParserDynamic = JsonParserDynamic.INSTANCE;

    static final KClass<Person> personKlass = kotlin.jvm.JvmClassMappingKt.getKotlinClass(Person.class);

    @Benchmark
    public void benchJsonParserStudentViaReflection() {
        jsonParserReflect.parse(json, personKlass);
    }

    @Benchmark
    public void benchJsonParserStudentViaDynamic() {
        jsonParserDynamic.parse(json, personKlass);
    }
}
