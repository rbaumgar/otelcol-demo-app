package org.acme.opentelemetry;

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.quarkus.logging.Log;

@Path("/")
public class TracedResource {

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("sayHello/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello(@PathParam("name") String name) {
        Log.info("sayhello: " + name);

        Span span = Span.current();

        span.setAttribute("event", name);
        span.setAttribute("message", "this is a log message for name " + name);

        String response = formatGreeting(name);
        span.setAttribute("response", response);
        
        return response;
    }

    @GET
    @Path("sayRemote/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String sayRemote(@PathParam("name") String name,
            @Context final Request request,
            @Context final UriInfo ui,
            @Context UriInfo uriInfo) {
        Log.info("sayRemote: " + name);

        Span span = Span.current();

        span.setAttribute("event", name);
        span.setAttribute("message", "this is a log message for name " + name);

        String serviceName = System.getenv("SERVICE_NAME");
        if (serviceName == null) {
            serviceName = uriInfo.getBaseUri().toString();
        }
        
        Log.info("Uri: " + uriInfo.getBaseUri());
        uriInfo.getRequestUri().getHost();
                
        //Log.info(serviceName);
        URL myURL = null;
        try {
            myURL = new URL(serviceName);
            
        } catch (MalformedURLException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }

        ResourceClient resourceClient = RestClientBuilder.newBuilder()
                .baseUrl(myURL)
                //.baseUri(uriInfo.getBaseUri())
                .build(ResourceClient.class);

        String response;
        try {
            response = resourceClient.hello(name) + " from " + serviceName;
            
        } catch (Exception e) {
            response=e.getMessage();
            response = response + "\n \nYou can set SERVICE_NAME in your environment with the correct URL ";
            response = response + "e.g. https://"+ uriInfo.getRequestUri().getHost();
        }                
         
        span.setAttribute("response", response);
        
        return response;
    }

    private String formatGreeting(String name) {
        Span span = Span.current();
        span.addEvent("formatGreeting", Attributes.of(AttributeKey.stringKey("text"),name));

        String response = "hello: " + name;
        span.addEvent("done", Attributes.of(AttributeKey.stringKey("respone"),response));

        return response;
    }
    
    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        //Log.info(System.getenv("OTELCOL_SERVER"));
        Log.info("hello");
        return "hello";
    }
 
    @GET
    @Path("hello/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@PathParam("name") String name) {
        Log.info("hello: " + name);
        Span span = Span.current();
        span.setAttribute("name", name);
        return "hello: " + name;
    }

    @GET
    @Path("chain")
    @Produces(MediaType.TEXT_PLAIN)
    public String chain() {
        Log.info("Uri: " + uriInfo.getBaseUri());
        ResourceClient resourceClient = RestClientBuilder.newBuilder()
                .baseUri(uriInfo.getBaseUri())
                .build(ResourceClient.class);
        return "chain -> " + resourceClient.hello("test");
    }

    @GET
    @Path("2xx")
    @Produces(MediaType.TEXT_PLAIN)
    public String simulate2xxResponse() {
        Log.info("2xx received");
        return "Got 2xx Response";
    }
    
    @GET
    @Path("5xx")
    @Produces(MediaType.TEXT_PLAIN)
	public String simulate5xxResponse() throws Exception {
        Log.info("5xx received");

        // create error 500
        throw new Exception("Exception message");
    }
}
