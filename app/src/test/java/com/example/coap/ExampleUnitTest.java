package com.example.coap;

import com.example.coap.client.NettyClient;
import com.example.coap.server.NettyServer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);

        //new NettyServer().start();

        /*new NettyClient("10.180.6.33", Env.DEFAULT_PORT).start();

        while (true) {

        }*/
    }

    @Test
    public void startNettyServer() {
        new NettyServer().start();
        while (true) {

        }
    }

    @Test
    public void startNettyClient() {
        new NettyClient(/*"10.180.6.33"*/"127.0.0.1", Env.TCP_PORT).start();

        while (true) {

        }
    }
}