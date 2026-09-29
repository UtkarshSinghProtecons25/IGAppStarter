package firstRoute.processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import firstRoute.exceptions.IgUnauthorizedException;
import firstRoute.exceptions.UserNotFoundException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class LoginResponseProcessor implements Processor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(Exchange exchange) throws Exception {

        Integer statusCode = exchange.getMessage()
                .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

        String responseBody = exchange.getMessage()
                .getBody(String.class);

        System.out.println("IG Response Status: " + statusCode);
        System.out.println("IG Response Body: " + responseBody);

        if (statusCode == null) {
            throw new RuntimeException("No response status received from login API");
        }

        if (statusCode >= 200 && statusCode < 300) {
            exchange.getMessage().setBody(responseBody);
            JsonNode json = objectMapper.readTree(responseBody);
            String token = json.path("token").asText();

            exchange.setProperty("jwtToken", token);
            return;
        }

        // Read message from IG API response
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        String message = jsonNode.path("message").asText("Invalid request");

        switch (statusCode) {

            case 401:
                throw new IgUnauthorizedException(message);

            case 404:
                throw new UserNotFoundException(message);

            default:
                throw new RuntimeException(message);
        }
    }
}
