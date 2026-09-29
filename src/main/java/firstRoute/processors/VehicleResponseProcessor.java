package firstRoute.processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class VehicleResponseProcessor implements Processor {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(Exchange exchange) throws Exception {
        String responseBody = exchange.getMessage()
                .getBody(String.class);

        Integer statusCode = exchange.getMessage()
                .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

        if (statusCode >= 200 && statusCode < 300) {
            exchange.getMessage().setBody(responseBody);
            JsonNode json = objectMapper.readTree(responseBody);
            String lenderId = json.get(0).path("lenderId").asText();

            exchange.setProperty("lenderId", lenderId);
        }
        else {
            exchange.getMessage().setBody(responseBody);
        }
    }
}
