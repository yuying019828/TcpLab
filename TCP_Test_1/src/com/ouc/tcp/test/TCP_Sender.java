package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.message.TCP_PACKET;

public class TCP_Sender extends TCP_Sender_ADT {

    private TCP_PACKET tcpPack;  // 待发送的 TCP 数据报
    //SR:flag=1表示窗口未满
    private volatile int flag = 1;//
    private SenderWindow SeWindow = new SenderWindow(this.client);

    public TCP_Sender() {
        super();  // 调用超类构造函数
        super.initTCP_Sender(this);  // 初始化 TCP 发送端
    }

    @Override
    public void rdt_send(int dataIndex, int[] appData) {
        // 生成 TCP 数据报（设置序号、数据字段、校验和)，注意打包的顺序
        this.tcpH.setTh_seq(dataIndex * appData.length + 1);  // 包序号设置为字节流号
        this.tcpS.setData(appData);
        this.tcpPack = new TCP_PACKET(this.tcpH, this.tcpS, this.destinAddr);
        this.tcpH.setTh_sum(CheckSum.computeChkSum(this.tcpPack));
        this.tcpPack.setTcpH(this.tcpH);
        
        if (this.SeWindow.isFull()) 
        {
            System.out.println("Sender window is full!!!");
            System.out.println();
            this.flag = 0;
        }
        
        while (this.flag == 0);//窗口满，等待ACK
        
        try {
            this.SeWindow.PutPacket(this.tcpPack.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        //this.SeWindow.PrintPackets();
        // 发送 TCP 数据报
        udt_send(this.tcpPack);
    }

    @Override
    public void waitACK() { }

    @Override
    public void recv(TCP_PACKET recvPack) {
        if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) 
        {
            System.out.println("Receive ACK Number: " + recvPack.getTcpH().getTh_ack());
            System.out.println();
            this.SeWindow.recvACK((recvPack.getTcpH().getTh_ack() - 1) / 100);
            if (!this.SeWindow.isFull()) 
            {
                this.flag = 1;
            }
        }
    }

    @Override
    public void udt_send(TCP_PACKET stcpPack) {
        this.tcpH.setTh_eflag((byte) 7);
        // 发送数据报
        this.client.send(stcpPack);
    }

}