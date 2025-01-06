package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

public class SenderWindow {
    private Client client;
    private int size = 5; //发送方窗口大小
    private int base = 0; //发送窗口左边界
    private int nextpacket = 0; //下一个包要放的位置
    private TCP_PACKET[] packets = new TCP_PACKET[this.size];
    private UDT_Timer[] timer = new UDT_Timer[this.size]; //为窗口内的每一个数据包都创建一个计时器

    public SenderWindow(Client client) 
    {
        this.client = client;
    }

    public boolean isFull() 
    {
        return this.size <= this.nextpacket;
    }

    public void PutPacket(TCP_PACKET packet) 
    {
        this.packets[this.nextpacket] = packet;
        this.timer[this.nextpacket] = new UDT_Timer();
        this.timer[this.nextpacket].schedule(new UDT_RetransTask(this.client, packet), 1000, 1000);//每1秒钟重传一次
        this.nextpacket++;
    }

    public void recvACK(int currentsequence) 
    {
        if (this.base <= currentsequence && currentsequence < this.base + this.size) //该ACK在发送窗口内
        { 
            if (this.timer[currentsequence - this.base] == null)  //currentsequence-base:当前ACK在窗口中的位置，判断是否为重复的ACK报文
            {
            	return;
            }
            
            this.timer[currentsequence - this.base].cancel();  //不重复就停止该包的计时器
            this.timer[currentsequence - this.base] = null;
            
            if (currentsequence == this.base) //如果收到的ACK是左边界的，则需要滑动窗口
            {  
                int LastACKSeq = 0;  //连续确认的最后一个数据包序号
                while (LastACKSeq + 1 < this.nextpacket && this.timer[LastACKSeq + 1] == null)//下一个在窗口范围内且数据包已被确认
                {
                	LastACKSeq++;
                }
                
                for (int i = 0; LastACKSeq + 1 + i < this.size; i++) //LastACKSeq + 1 到size的值移到窗口左界
                {
                    this.packets[i] = this.packets[LastACKSeq + 1 + i];
                    this.timer[i] = this.timer[LastACKSeq + 1 + i];
                }
                
                for (int i = this.size - (LastACKSeq + 1); i < this.size; i++) //剩余的置零
                {
                    this.packets[i] = null;
                    this.timer[i] = null;
                }
                
                this.base += LastACKSeq + 1;  //更新左边界
                this.nextpacket -= LastACKSeq + 1;  //更新发送窗口中下一个包方入的位置
            }
        }
        this.PrintPackets();
    }
    
    public void PrintPackets() 
    {
        System.out.print("发送窗口数据包： ");
        for (int i = 0; i < this.nextpacket; i++)  // 遍历当前窗口中实际放置的包
        {
            if (this.packets[i] != null) 
            {
                System.out.print(this.packets[i].getTcpH().getTh_seq() + "  ");
            }
            else 
            {
                System.out.print("null  ");
            }
        }
        System.out.println();

        System.out.print("发送窗口确认情况： ");
        for (int i = 0; i < this.nextpacket; i++) // 遍历当前窗口中实际放置的包
        { 
            if (this.timer[i] == null) // 判断定时器是否为 null 表示是否已确认
            { 
                System.out.print("1 ");
            } 
            else 
            {
                System.out.print("0 ");
            }
        }
        System.out.println();
    }
}