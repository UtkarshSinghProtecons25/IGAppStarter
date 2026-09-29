package firstRoute.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import firstRoute.exceptions.IgUnauthorizedException;
import firstRoute.exceptions.UserNotFoundException;
import firstRoute.model.ScrapRequest;
import firstRoute.processors.FirstProcess;
import firstRoute.processors.LoginResponseProcessor;
import firstRoute.processors.VehicleResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ScrapVehicleRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        restConfiguration().component("servlet");

        rest("/scrap")
                .post()
                .routeId("login-or-signup")
                .description("Scrap a vehicle")
                .consumes("application/json")
                .produces("application/json")
                .type(ScrapRequest.class)
                .to("direct:login_or_signup");

        //Exception Handling
        onException(IgUnauthorizedException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(401))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("""
            {
                "message": "${exception.message}"
            }
        """));

        onException(UserNotFoundException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("""
            {
                "message": "${exception.message}"
            }
        """));

        from("direct:login_or_signup")
                .routeId("call-login-api")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .process(new FirstProcess())
                .choice()
                .when().jsonpath("$.usertype", true)
                .to("{{sampleapi_port}}/api/v1/auth/signup?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .otherwise()
                .to("{{sampleapi_port}}/api/v1/auth/login?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .process(new LoginResponseProcessor())
                .end().to("direct:get_vehicle_details");

        from("direct:get_vehicle_details")
                .routeId("call-getvehicle")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(
                        "Authorization",
                        simple("Bearer ${exchangeProperty.jwtToken}")
                )
                .to("{{sampleapi_port}}/api/v1/vehicles?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .process(new VehicleResponseProcessor())
                .choice()
                .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200))
                .to("direct:payment_details_api")
                .otherwise()
                .stop()
                .end();

        from("direct:payment_details_api")
                .routeId("get-payment-details")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("{{sampleapi_port}}/api/v1/payments/${exchangeProperty.lenderId}?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .choice()
                .when(exchange -> {
                    Integer status = exchange.getIn()
                            .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

                    String action = exchange.getProperty("action", String.class);

                    return status != null
                            && status == 200
                            && "scrap".equals(action);
                })
                .to("direct:scrap_vehicle_api")
                .end();

        from("direct:scrap_vehicle_api")
                .routeId("scrap_vehicle_api")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(
                        "Authorization",
                        simple("Bearer ${exchangeProperty.jwtToken}")
                )
                .setBody(simple("""
                {"actionOnVehicle":"scrap"}
                """))
                .to("{{sampleapi_port}}/api/v1/scraps?bridgeEndpoint=true&throwExceptionOnFailure=false");
                }
}
