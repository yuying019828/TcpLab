package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.LinkedList;
import java.util.Timer;

public class SenderWindow 
{
    private Client client;  // 客户端
    private int size = 16;  // 窗口大小
    private int base = 0;  // 窗口左值
    private LinkedList<TCP_PACKET> packets = new LinkedList<TCP_PACKET>();  // 用 LinkedList 表示窗口中的包

    private Timer timer;  // 计时器
    private WindowRetransmit task;  // 重传任务

   /*构造函数*/
    public SenderWindow(Client client) 
    {
        this.client = client;
    }

    /*判断窗口是否已满*/
    public boolean isFull() 
    {
        return packets.size() >= size;
    }

    /*向窗口中加入包*/
    public void putPacket(TCP_PACKET packet) 
    {
    	
        if (!isFull()) 
        {
            packets.add(packet);  // 在窗口末尾插入包
            if (packets.size() == 1) 
            {  // 如果窗口中仅有一个包，则要开启计时器
                timer = new Timer();
                task = new WindowRetransmit(client, packets);
                timer.schedule(task, 1000, 1000);
            }
        }
    }

    /*接收到ACK*/
    public void receiveACK(int currentSequence) 
    {
    	System.out.println("currentSequence:"+currentSequence);
    	System.out.println("base:"+base);
        if (currentSequence >= base && currentSequence < base + size) // 如果收到的ACK在窗口范围内
        {  
            int acknowledgedPackets = currentSequence - base + 1;  // 计算已确认的包数量

            for (int i = 0; i < acknowledgedPackets; i++) 
            {
                if (!packets.isEmpty()) 
                {
                    packets.removeFirst();  // 移除窗口左边的已确认包
                }
            }

            base = currentSequence + 1;  // 更新窗口左沿指示的seq
            if (timer != null) {
                timer.cancel();  // 停止计时器
            }

            if (!packets.isEmpty()) {  // 窗口中仍有包，则重开计时器
                timer = new Timer();
                task = new WindowRetransmit(client, packets);
                timer.schedule(task, 1000, 1000);
            }
        }
        System.out.println("packets.size:"+ packets.size());
        System.out.println("New base:"+base);
    }
}
