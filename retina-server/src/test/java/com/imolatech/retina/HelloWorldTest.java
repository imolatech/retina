
package com.imolatech.retina;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.imolatech.retina.HelloWorld;

public class HelloWorldTest {

    final HelloWorld helloWorld = new HelloWorld();

    @Test
    public void saySomethingShouldRespondWithHelloWorldUppercased() {
        assertThat(helloWorld.saySomethingToUpperCase("ivy rocks!"), is(equalTo("IVY ROCKS!")));
    }
}
