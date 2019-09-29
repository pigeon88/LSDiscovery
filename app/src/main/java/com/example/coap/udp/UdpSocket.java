package com.example.coap.udp;

import java.io.IOException;
import java.net.SocketException;

public abstract class UdpSocket {

    public abstract void bind(int inetPort) throws IOException;

    public abstract void send(byte[] buff) throws IOException;

    public abstract void recv(byte[] buff) throws IOException;
}
