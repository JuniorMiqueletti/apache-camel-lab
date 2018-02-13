package com.juniormiqueletti.camel.request;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;

public class HttpTransferRouteTest {

	private CamelContext context;
	
	@Before
	public void setUp() {
		context = new DefaultCamelContext();
	}
	
	@Test
	public void basicHttpPostTest() throws Exception {
		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				
				from("file:requests?noop=true")
					.split()
						.xpath("request/items/item")
					.filter()
						.xpath("/item/format[text()='EBOOK']")
					.marshal()
						.xmljson()
					.log("${id} - ${body}")
				.to("http4://localhost:8080/webservices/ebook/item");
			}
		});
		
		context.start();
		Thread.sleep(2000);
	}
	
	@Test
	public void basicHttpGetTest() throws Exception {
		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				
				from("file:requests?noop=true")
					.split()
						.xpath("request/items/item")
					.filter()
						.xpath("/item/format[text()='EBOOK']")
					.marshal()
						.xmljson()
					.log("${id} - ${body}")
					.setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
				.to("http4://localhost:8080/webservices/ebook/item");
			}
		});
		
		context.start();
		Thread.sleep(2000);
	}
	
	@Test
	public void basicHttpQueryTest() throws Exception {
		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				
				from("file:requests?noop=true")
					.setProperty("requestId", xpath("/request/id/text()"))
				    .setProperty("clientId", xpath("/request/payment/email-holder/text()"))
					.split()
						.xpath("request/items/item")
					.filter()
						.xpath("/item/format[text()='EBOOK']")
					.setProperty("ebookId", xpath("/item/book/codigo/text()"))
					.marshal()
						.xmljson()
					.log("${id} - ${body}")
					.setHeader(Exchange.HTTP_QUERY,
							constant("clientId=breno@abc.com&requestId=123&ebookId=ARQ"))
				.to("http4://localhost:8080/webservices/ebook/item");
			}
		});
		
		context.start();
		Thread.sleep(2000);
	}
}
