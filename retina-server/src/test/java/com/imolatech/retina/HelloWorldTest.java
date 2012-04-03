
package com.imolatech.retina;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

import com.imolatech.retina.HelloWorld;

public class HelloWorldTest {
	private static final Logger logger = LoggerFactory.getLogger(HelloWorldTest.class);
    final HelloWorld helloWorld = new HelloWorld();

    @Test
    public void saySomethingShouldRespondWithHelloWorldUppercased() {
    	logger.debug("this is a test");
    	LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);    	
        assertThat(helloWorld.saySomethingToUpperCase("ivy rocks!"), is(equalTo("IVY ROCKS!")));
    }
}
