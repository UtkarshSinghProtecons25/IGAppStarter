package firstRoute.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class KafkaSenderRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
//        from("timer://test-kafka?period=10000")
//                .setBody(simple("Hi Utkarsh, Message sent from Sender route"))
//                .to("kafka:myKafkaTopic");
    }
}
