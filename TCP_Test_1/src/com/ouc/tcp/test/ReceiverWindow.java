package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceiverWindow {
    private Client client;
    private int size = 5;
    private int base = 0;
    private TCP_PACKET[] packets = new TCP_PACKET[this.size];
    Queue<int[]> dataQueue = new LinkedBlockingQueue<int[]>();
    

    public ReceiverWindow(Client client) 
    {
        this.client = client;
    }

    public int recvPacket(TCP_PACKET packet) 
    {
        int currentsequence = (packet.getTcpH().getTh_seq() - 1) / 100; //得到当前包的序号
        
        if (currentsequence < this.base) //该包的序号不在接受窗口内
        {  
            int lastwindow = this.base - this.size;//表示上一个窗口大小的左界
            //int right = this.base - 1;
            if (lastwindow <= 0) 
            {
            	lastwindow = 1;
            }
                
            if (lastwindow <= currentsequence && currentsequence <= (this.base - 1))//[base-size, base-1]
            {  
            	this.PrintPackets();
            	return currentsequence;
            }
            
        } 
        else if (this.base <= currentsequence && currentsequence < this.base + this.size) //该包的序号在接收窗口内[base, base+size]
        { 
            this.packets[currentsequence - this.base] = packet;  //放入接收窗口
            if (currentsequence == this.base)  //当前包的序号位于左边界
            {
            	this.slide();
            }
            this.PrintPackets();
            return currentsequence;
        }
        this.PrintPackets();
        return -1;
    }

    private void slide() 
    { 
        int LastPktSeq = 0;//连续收到的最后一个数据包序号
        
        while (LastPktSeq + 1 < this.size && this.packets[LastPktSeq + 1] != null)
        {
        	LastPktSeq++;  
        }

        for (int i = 0; i < LastPktSeq + 1; i++) //将接收到的包加入数据队列
        {    
        	this.dataQueue.add(this.packets[i].getTcpS().getData());
        }

        for (int i = 0; LastPktSeq + 1 + i < this.size; i++) //LastACKSeq + 1 到size的值移到窗口左界
        {
        	this.packets[i] = this.packets[LastPktSeq + 1 + i];
        }

        for (int i = this.size - (LastPktSeq + 1); i < this.size; i++) //剩余的置零
            this.packets[i] = null; 

        this.base += LastPktSeq + 1;  //更新左边界
        if (this.dataQueue.size() >= 20 || this.base == 1000) 
        {
            this.deliver_data();
        }
        
    }

    public void deliver_data() {
        // 检查 this.dataQueue，将数据写入文件
        try {
            File file = new File("recvData.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

            while (!this.dataQueue.isEmpty()) {
                int[] data = this.dataQueue.poll();

                // 将数据写入文件
                for (int i = 0; i < data.length; i++) {
                    writer.write(data[i] + "\n");
                }

                writer.flush();  // 清空输出缓存
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void PrintPackets() {
        System.out.println("当前接收窗口范围：[  " + this.base + ",  " + (this.base + this.size - 1) + "]");
        System.out.print("接收窗口数据包序号： ");

        for (int i = 0; i < this.size; i++) 
        {
            if (this.packets[i] != null) 
            {
                System.out.print(this.packets[i].getTcpH().getTh_seq() + "  "); // 显示数据包序号
            }
            else 
            {
                System.out.print("null  "); 
            }
        }
        System.out.println();

        System.out.print("接收窗口状态：       ");
        for (int i = 0; i < this.size; i++) 
        {
            if (this.packets[i] != null) 
            {
                System.out.print("[已收到]  ");
            } 
            else 
            {
                System.out.print("[未收到] ");
            }
        }
        System.out.println();
    }

}