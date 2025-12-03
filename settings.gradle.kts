rootProject.name = "fabrikt"

include(
    "end2end-tests:okhttp",
    "end2end-tests:openfeign",
    "end2end-tests:ktor",
    "end2end-tests:ktor-client-kotlinx",
    "end2end-tests:ktor-client-jackson",
    "end2end-tests:spring-http-interface",
    "end2end-tests:models-jackson",
    "end2end-tests:models-kotlinx",
    "playground",
)
