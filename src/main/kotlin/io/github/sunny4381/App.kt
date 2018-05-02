package io.github.sunny4381

import org.jooby.Kooby

class App : Kooby({
    get {
        val name = param("name").value("Kotlin")
        "Hello $name!"
    }
})
