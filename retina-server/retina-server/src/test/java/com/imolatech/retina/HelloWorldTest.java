/**
 *   HelloWorldTest.java
 *   (c) David Harrigan, 2010.
 *   
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
