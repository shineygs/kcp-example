package org.example;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

public class UDPClient {

    public UDPClient() throws Exception {

    }

    public static void main(String[] args) throws Exception {
        UDPClient udpClient = new UDPClient();
        udpClient.udp();
    }

    DatagramSocket sendSocket = new DatagramSocket();
    InetAddress address = InetAddress.getByName("127.0.0.1");
    DatagramPacket receviePacket;
    int port = 8383;
    KCP kcp;
    AtomicInteger integer = new AtomicInteger();
    public void udp() throws Exception {

        kcp = new KCP(0x01) {
            @Override
            protected void output(byte[] buffer, int size) {
                if (size == 0) return;
                DatagramPacket sendPacket = new DatagramPacket(buffer, size, address, port);
                // 发送数据
                try {
                    System.out.println("size : " + size);
                    sendSocket.send(sendPacket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        kcp.snd_wnd = 128;
        kcp.rcv_wnd = 128;
        byte[] bytes = "Conn".getBytes();
        kcp.Send(bytes);
        update();
        loop();
    }

    public void update() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                kcp.Update(System.currentTimeMillis());
            }
        }).start();
    }

    public void loop() throws Exception {
        while (true) {
            kcp.Update(System.currentTimeMillis());
            byte[] receiveByte  = new byte[1024];
            receviePacket = new DatagramPacket(receiveByte , receiveByte .length);
            sendSocket.receive(receviePacket);
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

            String receiveData = new String(bufData);
            if (receiveData.equals("Conn-OK")) {
                kcp.Send(("client data: " + integer.getAndIncrement()).getBytes());
            } else if (receiveData.startsWith("server data")){
                kcp.Send(("client data: " + integer.getAndIncrement()).getBytes());
            }
            System.out.println("Server Message: " + receiveData);
        }
    }

    private byte[] createByte() {
        String str = "start XX医院作为该地区历史悠久、知名的三甲医院，在地区医疗服务事业上有不可或缺的作用。该院的门诊量、住院率一直位于地区医院排名前列，这对它的医疗服务水平和效率提出了较高的要求，为此XX医院早在十年前就开始信息化建设，" +
                "从早期的收费结算信息化到目前的电子病历，信息化的触角延生到了整个医院各个科室。但是由于医院业务的复杂性和历史原因，目前存在各种异构系统十几个，由于各个系统的厂家和标准不尽相同，信息难以兼容和共享，存在信息孤岛现象，" +
                "各个业务科室的数据通讯有时还需要使用手工签单的形式，不但效率低下，而且还浪费耗材资源，时常还有错误发生，这些影响了全院级的系统整合、集成和扩展，制约了该院向数字化医院、智慧型医院的发展。在此背景下，" +
                "XX医院投入400万预算进行公开招标，采购“临床信息集成平台”来改造、整合、集成和扩展现有信息化系统，我公司的解决方案得到客户的认可，并幸运的中标称为供应商。签订合同后，我被公司任命为该项目的项目经理，领导整个团队针对项目的实施和管理。end";

        return str.getBytes();
    }
}
