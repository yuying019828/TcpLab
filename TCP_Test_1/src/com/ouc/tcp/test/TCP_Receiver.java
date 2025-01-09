/***************************2.1: ACK/NACK*****************/
/***** Feng Hong; 2015-12-09******************************/
package com.ouc.tcp.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.ouc.tcp.client.TCP_Receiver_ADT;
import com.ouc.tcp.message.TCP_PACKET;

public class TCP_Receiver extends TCP_Receiver_ADT {
	
	private TCP_PACKET ackPack;	//回复的ACK报文段
	int except_sequence=1;//用于记录当前待接收的包序号，注意包序号不完全是
		
	//2.1用于避免结束重复包
	int last_except_sequence = 0;//用于记录上一次待接收的包序号
	
	/*构造函数*/
	public TCP_Receiver() {
		super();	//调用超类构造函数
		super.initTCP_Receiver(this);	//初始化TCP接收端
	}

	@Override
	//接收到数据报：检查校验和，设置回复的ACK报文段
	public void rdt_recv(TCP_PACKET recvPack) {
		//检查校验码，生成ACK
		if(CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) 
		{
			int currentSequence = recvPack.getTcpH().getTh_seq();//当前包序号
			if (currentSequence == this.except_sequence)
			{
				//生成ACK报文段（设置确认号）
				tcpH.setTh_ack(recvPack.getTcpH().getTh_seq());
				ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
				tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
				//回复ACK报文段
				reply(ackPack);
				//将接收到的正确有序的数据插入data队列，准备交付
				dataQueue.add(recvPack.getTcpS().getData());				
				except_sequence += 100;
				
			}
			else
			{
				tcpH.setTh_ack(except_sequence - 100);
				ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
				tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
				//回复ACK报文段
				reply(ackPack);
			}
		}
		//数据包错误
//		else
//		{
//			System.out.println("Recieve Computed: "+CheckSum.computeChkSum(recvPack));
//			System.out.println("Recieved Packet"+recvPack.getTcpH().getTh_sum());
//			System.out.println("Problem: Packet Number: "+recvPack.getTcpH().getTh_seq()+" + InnerSeq:  "+except_sequence);
//			tcpH.setTh_ack(last_except_sequence);//2.2取消NACK
//			ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
//			tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
//			//回复ACK报文段
//			reply(ackPack);
//		}
		//3.0:对于出错的数据包，接收方只需等待发送方启动重传任务再次发来数据包。
		else 
		{
			
			System.out.println("Recieve Computed: "+CheckSum.computeChkSum(recvPack));
			System.out.println("Recieved Packet"+recvPack.getTcpH().getTh_sum());
			System.out.println("Problem: Packet Number: "+recvPack.getTcpH().getTh_seq());
			
		}
		
		System.out.println();
		//交付数据（每20组数据交付一次）
		if(dataQueue.size() == 20) 
			deliver_data();	
	}

	@Override
	//交付数据（将数据写入文件）；不需要修改
	public void deliver_data() {
		//检查dataQueue，将数据写入文件
		File fw = new File("recvData.txt");
		BufferedWriter writer;
		
		try {
			writer = new BufferedWriter(new FileWriter(fw, true));
			
			//循环检查data队列中是否有新交付数据
			while(!dataQueue.isEmpty()) {
				int[] data = dataQueue.poll();
				
				//将数据写入文件
				for(int i = 0; i < data.length; i++) {
					writer.write(data[i] + "\n");
				}
				
				writer.flush();		//清空输出缓存
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	//回复ACK报文段
	public void reply(TCP_PACKET replyPack) {
		//设置错误控制标志
		tcpH.setTh_eflag((byte)7);	//eFlag=0，信道无错误
				
		//发送数据报
		client.send(replyPack);
	}
	
}
