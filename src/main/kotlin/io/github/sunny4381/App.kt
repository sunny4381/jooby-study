package io.github.sunny4381

import org.jooby.Kooby
import org.jooby.Results
import org.jooby.hbs.Hbs

class App : Kooby({
    use(Hbs("/", ".hbs"))

    get("/") {
        Results.html("index").put("model", "world")
    }
})
