package firstRoute.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RestProducerRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        restConfiguration().component("servlet");

        rest("/ig-restdemo")
                .get()
                .to("direct:ig-simple-transfrom");


        from("direct:ig-simple-transfrom")
                .setHeader(Exchange.HTTP_METHOD,simple("GET"))
                .to("http://127.0.0.1:8080/api/v1/apps?bridgeEndpoint=true")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String output = exchange.getIn().getBody(String.class);
                        System.out.println(output);
                    }
                });
//
//        rest("/ig-restdemo")
//                .post();
    }
}
