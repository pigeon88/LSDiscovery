package com.example.coap.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UnicastUdpSocket extends UdpSocket {

    private DatagramSocket socket;

    public UnicastUdpSocket() {

    }

    @Override
    public void bind(int inetPort) throws SocketException {
        socket = new DatagramSocket(inetPort);
    }

    @Override
    public void send(byte[] buff) throws IOException {
        DatagramPacket dataPacket = new DatagramPacket(buff, buff.length, socket.getInetAddress(), socket.getPort());
        socket.send(dataPacket);
    }

    @Override
    public void recv(byte[] buff) throws IOException {
        //byte[] buf = new byte[Env.UDP_BUFF_SIZE];
        DatagramPacket dp = new DatagramPacket(buff, buff.length);
        socket.receive(dp);
    }
}
