package foo.bar.baz

import DebugLog


@DebugLog
fun foo(name: String = "World"): String =
    "Hello, $name!"