package com.juniormiqueletti.camel.request;



import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class HttpToFileTransferSortingOutSubRoutesTest {

private CamelContext context;
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
	public void setUp() {
		context = new DefaultCamelContext();
	}
	
	@Test
	public void basicTest() throws Exception {
		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				
				from("file:requests?noop=true")
			    .routeId("route-requests")
			    	.multicast()
		        .to("direct:http")
		        .to("direct:soap");
				
				from("direct:soap")
					.routeId("route-soap")
					.log("calling soap service")
				.to("mock:soap");
				
				from("direct:http")
					.routeId("route-http")
					.setProperty("requestId", xpath("/request/id/text()"))
				    .setProperty("clientId", xpath("/request/payment/email-holder/text()"))
					.split()
						.xpath("request/items/item")
					.filter()
					.xpath("/item/formato[text()='EBOOK']")
					.setProperty("ebookId", xpath("/item/book/codigo/text()"))
					.marshal()
						.xmljson()
					.log("${id} - ${body}")
					.setHeader(Exchange.HTTP_METHOD, HttpMethods.GET)
					.setHeader(Exchange.HTTP_QUERY,
							simple("clientId=${property.ebookId}&requestId=${property.requestId}&ebookId=${property.clientId}"))
					.to("http4://localhost:8080/webservices/ebook/item");
				
			}
		});
		
		context.start();
		Thread.sleep(20000);
		context.stop();
	}
}
