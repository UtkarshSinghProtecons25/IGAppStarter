package firstRoute.processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import firstRoute.dto.LoginDTO;
import firstRoute.exceptions.IgUnauthorizedException;
import firstRoute.exceptions.UserNotFoundException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;


public class FirstProcess implements Processor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(Exchange exchange) throws Exception {
        String requestBody = exchange.getIn().getBody(String.class);

              if (requestBody.contains("abcd"))
                throw new IgUnauthorizedException("Invalid credentials");
              else if(requestBody.contains("kaka"))
                throw new UserNotFoundException("Invalid request");
              else
                exchange.getIn().setBody(requestBody);

        //Storing the action property to use at the end
        JsonNode json = objectMapper.readTree(requestBody);

        String action = json.has("action")
                ? json.get("action").asText()
                : null;

        exchange.setProperty("action", action);
        }
}
