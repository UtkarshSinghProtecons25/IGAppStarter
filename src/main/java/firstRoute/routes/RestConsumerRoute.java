package firstRoute.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RestConsumerRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
//            from("timer://testapi?period=1000")
//                    .setHeader(Exchange.HTTP_METHOD,simple("GET"))
//                    .to("http://127.0.0.1:8080/api/v1/apps")
//                    .process(new Processor() {
//                        @Override
//                        public void process(Exchange exchange) throws Exception {
//                            String output = exchange.getIn().getBody(String.class);
//                            System.out.println(output);
//                        }
//                    });
    }
}
