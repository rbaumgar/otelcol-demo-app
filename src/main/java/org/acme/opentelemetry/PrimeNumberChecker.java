package org.acme.opentelemetry;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import io.opentelemetry.api.trace.Span;
import io.quarkus.logging.Log;

@Path("/prime")
public class PrimeNumberChecker {

    private long highestPrimeNumberSoFar = 2;
    private long counter = 0;

    @GET
    @Path("{number}")
    @Produces("text/plain")
    // @Counted(name = "performedChecks", description = "How many primality checks have been performed.")
    // @Timed(name = "checksTimer", description = "A measure of how long it takes to perform the primality test.", unit = MetricUnits.MILLISECONDS)
  
    public String checkIfPrime(@PathParam("number") long number) {
        Span span = Span.current();
        counter = counter + 1;
        span.setAttribute("performedChecks", counter);
        span.setAttribute("number", number);
        span.setAttribute("isPrime", false);

        if (number < 1) {
            Log.info("Only natural numbers can be prime numbers.");
            return "Only natural numbers can be prime numbers.";
        }
        if (number == 1) {
            Log.info("1 is not a prime.");
            return "1 is not a prime.";
        }
        if (number == 2) {
            Log.info("2 is a prime.");
            return "2 is a prime.";
        }
        if (number % 2 == 0) {
            Log.info(number + " is not a prime, it is divisible by 2.");
            return number + " is not a prime, it is divisible by 2.";
        }
        for (int i = 3; i < Math.floor(Math.sqrt(number)) + 1; i = i + 2) {
            if (number % i == 0) {
                Log.info(number + " is not a prime, is divisible by " + i + ".");
                return number + " is not a prime, is divisible by " + i + ".";
            }
        }
        if (number > highestPrimeNumberSoFar) {
            highestPrimeNumberSoFar = number;
        }
        span.setAttribute("isPrime", true);
        Log.info(number + " is a prime.");
        return number + " is a prime.";
    }

    // @Gauge(name = "highestPrimeNumberSoFar", unit = MetricUnits.NONE, description = "Highest prime number so far.")
    public Long highestPrimeNumberSoFar() {
        return highestPrimeNumberSoFar;
    }

}