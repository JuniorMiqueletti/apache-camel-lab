package com.juniormiqueletti.camel.request;



import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.juniormiqueletti.camel.domain.Negociacao;
import com.thoughtworks.xstream.XStream;

public class HttpToFileTransferTest {

	private CamelContext context;
	
	private final XStream xstream = new XStream();
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
	public void setUp() {
		context = new DefaultCamelContext();
		xstream.alias("negociacao", Negociacao.class);
	}
	
	@Test
	public void basicTest() throws Exception {
		
		String absolutePath = tempFolder.getRoot().getAbsolutePath();
		String uriOut = "file:" + absolutePath;
		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				
				from("timer://negociacoes?fixedRate=true&delay=1s&period=360s")
				.to("http4://argentumws.caelum.com.br/negociacoes")
					.convertBodyTo(String.class)
					.log("${body}")
					.setHeader(Exchange.FILE_NAME, constant("negotiations.xml"))
				.to(uriOut);
			}
		});
		
		context.start();
		Thread.sleep(2000);
		
		long numberFiles = Arrays.asList(tempFolder.getRoot().listFiles())
			.stream()
			.count();
		
		assertTrue(numberFiles > 0);
	}
	
	@Test
	public void basicUnmarshalTest() throws Exception {
		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				
				from("timer://negociacoes?fixedRate=true&delay=1s&period=360s")
				.to("http4://argentumws.caelum.com.br/negociacoes")
					.convertBodyTo(String.class)
					.unmarshal(new XStreamDataFormat(xstream))
					.split(body())
					.log("${body}")
				.end();
			}
		});
	}
}
