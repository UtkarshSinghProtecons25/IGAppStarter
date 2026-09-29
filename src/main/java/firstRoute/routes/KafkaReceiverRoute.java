package firstRoute.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
@Component
public class KafkaReceiverRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
//        from("kafka:myKafkaTopic").log("log:Receiving messages from Kafka Topic :  ${body}");
    }
}
