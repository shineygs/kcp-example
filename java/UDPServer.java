package org.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class UDPServer {

    public static void main(String[] args) throws Exception {

        UDPServer udpServer = new UDPServer();
        udpServer.udp();
    }

    int port = 8383;
    KCP kcp;
    DatagramSocket socket;
    DatagramPacket receviePacket;
    AtomicInteger integer = new AtomicInteger();
    public void udp() throws Exception {
        socket = new DatagramSocket(port);
        kcp = new KCP(0x01) {
            @Override
            protected void output(byte[] buffer, int size) {
                InetAddress address = receviePacket.getAddress();
                int port = receviePacket.getPort();
                DatagramPacket packet2=new DatagramPacket(buffer, buffer.length, address, port);
                try {
                    socket.send(packet2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        kcp.snd_wnd = 128;
        kcp.rcv_wnd = 128;

        System.out.println("服务端启动 port: " + port);
        loop();
    }

    public void loop() throws Exception {
        while (true) {
            kcp.Update(System.currentTimeMillis());
            byte[] receiveByte = new byte[1024];
            receviePacket = new DatagramPacket(receiveByte, receiveByte.length);
            socket.receive(receviePacket);
            byte[] data = receviePacket.getData();

            kcp.Input(data);

            if (kcp.PeekSize() < 0) {
                continue;
            }
            byte[] bufData = new byte[kcp.PeekSize()];
            while (true) {
                int ret = kcp.Recv(bufData);
                if (ret < 0) {
                    break;
                }
            }
            for (int i = 0 ; i < bufData.length ; i++) {
                System.out.print(bufData[i] + " , ");
            }
            System.out.println();
            String receiveData = new String(bufData);
            if (receiveData.equals("Conn")) {
                kcp.Send("Conn-OK".getBytes());
            } else if (receiveData.startsWith("client data")) {
                kcp.Send(("server data" + integer.getAndIncrement()).getBytes());
            }
            System.out.println("Client Message: " + receiveData);
        }
    }
}
