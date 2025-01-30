package foo.bar

import DebugLog


@DebugLog
fun box() {
    while (true) {
        if (true) break
    }
}

interface Foo {
    object Boo
}