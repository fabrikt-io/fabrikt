rootProject.name = "fabrikt"

// Check if the end2end-tests folder exists (it won't in Docker)
if (file("end2end-tests").exists()) {
    include(
        "end2end-tests:okhttp",
        "end2end-tests:openfeign",
        "end2end-tests:ktor",
        "end2end-tests:ktor-client-kotlinx",
        "end2end-tests:ktor-client-jackson",
        "end2end-tests:spring-http-interface",
        "end2end-tests:models-jackson",
        "end2end-tests:models-kotlinx"
    )
}
// Check if the playground folder exists (it won't in Docker)
if (file("playground").exists()) {
    include("playground")
}

