package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.LinkedList;
import java.util.TimerTask;

public class WindowRetransmit extends TimerTask 
{
    private Client senderClient;  // 客户端
    private LinkedList<TCP_PACKET> packets;  // 窗口内的包

    /*构造函数*/
    public WindowRetransmit(Client client, LinkedList<TCP_PACKET> packets) 
    {
        super();
        this.senderClient = client;
        this.packets = packets;
    }

    @Override
    public void run() {
        synchronized (packets) // 确保线程安全
        {  
            for (TCP_PACKET packet : packets) 
            {
                if (packet != null) // 如果存在包
                {  
                    senderClient.send(packet);  // 逐一递交包
                }
            }
        }
    }
}
