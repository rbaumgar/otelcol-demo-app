package org.acme.opentelemetry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import io.opentelemetry.api.trace.Span;

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
            return "Only natural numbers can be prime numbers.";
        }
        if (number == 1) {
            return "1 is not prime.";
        }
        if (number == 2) {
                    return "2 is prime.";
        }
        if (number % 2 == 0) {
            return number + " is not prime, it is divisible by 2.";
        }
        for (int i = 3; i < Math.floor(Math.sqrt(number)) + 1; i = i + 2) {
            if (number % i == 0) {
                return number + " is not prime, is divisible by " + i + ".";
            }
        }
        if (number > highestPrimeNumberSoFar) {
            highestPrimeNumberSoFar = number;
        }
        span.setAttribute("isPrime", true);
        return number + " is prime.\n";
    }

    // @Gauge(name = "highestPrimeNumberSoFar", unit = MetricUnits.NONE, description = "Highest prime number so far.")
    public Long highestPrimeNumberSoFar() {
        return highestPrimeNumberSoFar;
    }

}