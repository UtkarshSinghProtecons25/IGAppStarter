package firstRoute.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ActiveMQReceiverRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

//        from("activemq:test-mq").log("log:receiving messages from ActiveMQ : ${body}");
    }
}
