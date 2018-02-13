package com.juniormiqueletti.camel.request;

import static org.junit.Assert.assertTrue;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import scala.actors.threadpool.Arrays;

public class RouteRequestTest {

	private CamelContext context;
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
	public void setUp() {
		context = new DefaultCamelContext();
	}
	
	@Test
	public void basicTest() throws Exception {
		String absolutePath = tempFolder.getRoot().getAbsolutePath();
		String uriOut = "file:" + absolutePath;
		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				
				from("file:requests?noop=true")
					.marshal()
						.xmljson()
					.log("${exchange.pattern}")
					.log("${id} - ${body}")
				.to(uriOut);
			}
		});
		
		context.start();
		Thread.sleep(5000);
		
		long numberFiles = Arrays.asList(tempFolder.getRoot().listFiles())
			.stream()
			.count();
		
		assertTrue(numberFiles > 0);
	}
	
	@Test
	public void splitTest() throws Exception {
		String absolutePath = tempFolder.getRoot().getAbsolutePath();
		String uriOut = "file:" + absolutePath;
		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				
				from("file:requests?noop=true")
					.split()
						.xpath("request/items/item")
					.marshal()
						.xmljson()
					.log("${exchange.pattern}")
					.log("${id} - ${body}")
					.setHeader("CamelFileName", simple("${file:name.noext}_${id}.json"))
				.to(uriOut);
			}
		});
		
		context.start();
		Thread.sleep(5000);
		
		long numberFiles = Arrays.asList(tempFolder.getRoot().listFiles())
				.stream()
				.count();
		
		assertTrue(numberFiles == 6);
	}
}
