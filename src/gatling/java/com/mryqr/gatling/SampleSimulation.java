package com.mryqr.gatling;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.stressPeakUsers;
import static io.gatling.javaapi.http.HttpDsl.http;

public class SampleSimulation extends Simulation {
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080");

    ScenarioBuilder scn = scenario("Scenario Name") // A scenario is a chain of requests and pauses
            .exec(http("request_1")
                    .get("/about"));

    {
        setUp(
                scn.injectOpen(
                        nothingFor(4), // 1
                        atOnceUsers(200), // 2
                        rampUsers(1000).during(5), // 3
                        constantUsersPerSec(200).during(15), // 4
                        constantUsersPerSec(200).during(15).randomized(), // 5
                        rampUsersPerSec(10).to(200).during(10), // 6
                        rampUsersPerSec(10).to(200).during(10).randomized(), // 7
                        stressPeakUsers(5000).during(20) // 8
                ).protocols(httpProtocol)
        );
    }
}