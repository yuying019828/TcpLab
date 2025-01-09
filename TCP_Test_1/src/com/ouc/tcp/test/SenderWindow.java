package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.LinkedList;
import java.util.Timer;

public class SenderWindow 
{
    private Client client;  // 客户端
    private int size = 16;  // 窗口大小
    private int base = 0;  // 窗口左值
    private int nextpacket = 0; //下一个包要放的位置
    private TCP_PACKET[] packets = new TCP_PACKET[this.size];
    private UDT_Timer timer;  // 计时器
    private TaskPacketsRetrans task;  // 重传任务

   /*构造函数*/
    public SenderWindow(Client client) 
    {
        this.client = client;
    }

    /*判断窗口是否已满*/
    public boolean isFull() 
    {
    	return this.size <= this.nextpacket;
    }

    /*向窗口中加入包*/
    public void PutPacket(TCP_PACKET packet) 
    {
    	
        if (!isFull()) 
        {
        	this.packets[this.nextpacket] = packet;  // 在窗口末尾插入包
            if (this.base == this.nextpacket) // 如果窗口中仅有一个包，则要开启计时器
            {  
                this.timer = new UDT_Timer();
                this.task = new TaskPacketsRetrans(client, packets);
                this.timer.schedule(task, 1000, 1000);
            }
        }
        this.nextpacket++;
    }

    /*接收到ACK*/
    public void receiveACK(int currentSequence) 
    {
    	System.out.println("currentSequence:"+currentSequence);
    	System.out.println("base:"+base);
        if (currentSequence >= base && currentSequence < base + size) // 如果收到的ACK在窗口范围内
        {  
            int NoAckPackets = currentSequence - base + 1;  // 第一个未确认的包

            for (int i = 0; NoAckPackets + i < size; i++) //清楚已确认的包
            {
            	this.packets[i] = this.packets[NoAckPackets + i];
                this.packets[NoAckPackets + i] = null;
            }

            base = currentSequence + 1;  // 更新窗口左沿指示的seq
            nextpacket -= NoAckPackets;
            
            if (timer != null) // 停止计时器
            {
                timer.cancel();  
            }

            if (this.nextpacket != 0) // 窗口中仍有包，则重开计时器
            {  
                timer = new UDT_Timer();
                task = new TaskPacketsRetrans(client, packets);
                timer.schedule(task, 1000, 1000);
            }
        }
        
    }
}
