package com.juniormiqueletti.camel.request;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;

public class RouteRequestTest {

	private CamelContext context;
	
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
				.marshal()
					.xmljson()
				.log("${exchange.pattern}")
				.log("${id} - ${body}")
				.to("file:out");
			}
		});
		
		context.start();
		Thread.sleep(5000);
	}
}
