package com.example.coap.udp;

import com.example.coap.Env;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastUdpSocket extends UdpSocket {

    private MulticastSocket socket;

    public MulticastUdpSocket() {
    }

    @Override
    public void bind(int inetPort) throws IOException {
        socket = new MulticastSocket(inetPort);
        socket.setTimeToLive(32);
        socket.setLoopbackMode(false);
        InetAddress receiveAddress = InetAddress.getByName(Env.UDP_HOST);
        socket.joinGroup(receiveAddress);
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
