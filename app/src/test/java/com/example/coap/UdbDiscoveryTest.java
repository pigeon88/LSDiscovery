package com.example.coap;

import org.junit.Before;
import org.junit.Test;

public class UdbDiscoveryTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void start() throws Exception {
        new UdbDiscovery().start();
        Thread.sleep(10 * 60 * 1000);
    }
}