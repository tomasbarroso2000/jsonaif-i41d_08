# jsonaif

Json Parser with Kotlin Reflection API and bytecodes generation for JVM.

## Assignments

1. Published 16-3-2022, DEADLINE: 11-4-2022, [jsonaif-part1-reflection](assignments/jsonaif-part1-reflection.md)
2. Published 04-4-2022, DEADLINE: 16-5-2022, [jsonaif-part2-dynamic](assignments/jsonaif-part2-dynamic.md)
3. Published 09-5-2022, DEADLINE: 06-6-2022

### Assignment 2 - Observations

![image](https://user-images.githubusercontent.com/72862681/168314999-3d61aaa3-b678-41a1-b86f-57e8cc0386d9.png)

- The Date object only has properties of Primitive Type;
- The Person object only has properties of Reference Type;
- The Student object has properties of both types.

By observing these results, we detected that there is not that much of a difference between the Dynamic implementation and the Reflection implementation. We estimate the cause of this conclusion to be the fact that the Reflection parser achieved better performance than expected. Before executing this benchmark, we were expecting to see very different results. We figured that the Dynamic implementation would be significantly superior to the Reflection implementation when there are no Reference types involved (like in the Date type), which wasn't really the case. However, it was expected that the benchmark of the Person type would be similar in both implementation, which was the case. Regarding the Student type (both Reference and Primitive types), the difference between implementations was close to the Date one even though we were expecting a smaller difference.
