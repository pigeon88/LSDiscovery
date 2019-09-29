package com.example.coap;

public class Env {

    public static final int VERSION = 1;
    public static final String UDP_HOST = "224.224.99.60";
    public static final int UDP_PORT = 9960;
    public static final int UDP_BUFF_SIZE = 512; //UDP包的大小(因为internet的标准MTU是576)就应该是576-IP头(20)-UDP头(8)=548
    static final int UDP_HELLO_INTERVAL = 10;
    public static final int UDP_HELLO_DEAD_TIME = 40;
    public static final int TCP_PORT = 9970;
}
