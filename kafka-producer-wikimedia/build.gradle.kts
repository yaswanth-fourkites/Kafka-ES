plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("org.apache.kafka:kafka-clients:3.6.0")
    implementation ("org.slf4j:slf4j-api:2.0.9")
    implementation ("org.slf4j:slf4j-simple:2.0.9")
    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    // https://mvnrepository.com/artifact/com.launchdarkly/okhttp-eventsource
    implementation ("com.launchdarkly:okhttp-eventsource:2.5.0")

}

tasks.test {
    useJUnitPlatform()
}