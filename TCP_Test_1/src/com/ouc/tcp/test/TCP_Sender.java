/***************************2.1: ACK/NACK
**************************** Feng Hong; 2015-12-09*/

package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.message.TCP_PACKET;

public class TCP_Sender extends TCP_Sender_ADT {
	
	private TCP_PACKET tcpPack;	//待发送的TCP数据报
	
	//GBN中flag应初始化为1，表示发送窗口空
	private volatile int flag = 1;
	
	private SenderWindow window = new SenderWindow(this.client);
	
	//RDT 3.0:处理包丢失/出错的情况，加入计时器和重传任务
//	private UDT_Timer timer;
//	private UDT_RetransTask retransTask;
	
	/*构造函数*/
	public TCP_Sender() {
		super();	//调用超类构造函数
		super.initTCP_Sender(this);		//初始化TCP发送端
	}
	
	@Override
	//可靠发送（应用层调用）：封装应用层数据，产生TCP数据报；需要修改
	public void rdt_send(int dataIndex, int[] appData) {
		
		//生成TCP数据报（设置序号和数据字段/校验和),注意打包的顺序
		tcpH.setTh_seq(dataIndex * appData.length + 1);//包序号设置为字节流号：
		tcpS.setData(appData);
		tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);		
		//更新带有checksum的TCP 报文头		
		tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
		tcpPack.setTcpH(tcpH);
		
		//RDT 3.0
//		timer = new UDT_Timer();
//		retransTask = new UDT_RetransTask(client, tcpPack);
//		
//		//将计时器加入重传任务中，设置开始时间为1s之后，之后每隔1s执行一次
//		timer.schedule(retransTask, 1000, 1000);
		
		//GBK
		// 对于应用层的数据，需先判断发送窗口是否已满
		//窗口已满则等待确认，否则将数据包加入窗口
		if (this.window.isFull()) 
		{
			System.out.println();
			System.out.println("Sliding Window is full");
			System.out.println();
			this.flag = 0;
		}
		
		//flag==0表示窗口满，等待ACK报文
		while (flag==0);
		
		/*向窗口中加入包*/
		try 
		{
			this.window.PutPacket(this.tcpPack.clone());
		} 
		catch (CloneNotSupportedException e) 
		{
			e.printStackTrace();
		}
		System.out.println();
		System.out.println("Sending sequence:"+tcpPack.getTcpH().getTh_seq());
		//发送TCP数据报
		udt_send(tcpPack);
		
	}
	
	@Override
	//不可靠发送：将打包好的TCP数据报通过不可靠传输信道发送；仅需修改错误标志
	public void udt_send(TCP_PACKET stcpPack) {
		//设置错误控制标志
		//0:可靠信道
		//1：出错
		//2：丢包
		//3：延迟
		//4：出错+丢包
		//5：出错+延迟
		//6：丢包+延迟
		//7：出错+丢包+延迟
		
		tcpH.setTh_eflag((byte)7);		
		//System.out.println("to send: "+stcpPack.getTcpH().getTh_seq());				
		//发送数据报
		client.send(stcpPack);
	}
	
	@Override
	//需要修改
	public void waitACK() //GBK的ACK处理在窗口类中
	{
		//循环检查ackQueue
		//循环检查确认号对列中是否有新收到的ACK		
//		if(!ackQueue.isEmpty())
//		{
//			int currentAck=ackQueue.poll();
			// System.out.println("CurrentAck: "+currentAck);
			//2.1
//			if (currentAck == -1)
//			{
//				System.out.println("NACK Retransmit: "+tcpPack.getTcpH().getTh_seq());
//				udt_send(tcpPack);
//				flag = 0;//继续等待ACK
//			}
//			else if (currentAck == -2)
//			{
//				System.out.println("WRONG ACK/NACK Retransmit: "+tcpPack.getTcpH().getTh_seq());
//				System.out.println();	
//				udt_send(tcpPack);
//				flag = 0;//继续等待ACK
//				
//			}
			//2.2
//			if (currentAck != tcpPack.getTcpH().getTh_seq())
//			{
//				System.out.println("Retransmit: "+tcpPack.getTcpH().getTh_seq());
//				udt_send(tcpPack);  // 重新发包
//				flag = 0; // 仍然是waitACK状态
//			}
//			else
//			{
//				System.out.println("Clear: "+tcpPack.getTcpH().getTh_seq());
//				flag = 1;//状态切换为等待应用层调用
//				//break;
//			}
			//3.0 收到正确ACK，该包计时器取消；否则重传任务自动重传该包
//			if (currentAck == tcpPack.getTcpH().getTh_seq())
//			{
//				System.out.println("Clear: "+tcpPack.getTcpH().getTh_seq());
//				timer.cancel();
//				flag = 1;//状态切换为等待应用层调用
//				//break;
//			}
			
//		}
	}

	@Override
	//接收到ACK报文：检查校验和，将确认号插入ack队列; NACK的确认号为－1；不需要修改
	public void recv(TCP_PACKET recvPack) 
	{
		//2.1检查ACK/NACK
//		if(CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) 
//		{
//			System.out.println("Receive ACK Number： "+ recvPack.getTcpH().getTh_ack());
//			ackQueue.add(recvPack.getTcpH().getTh_ack());
//			System.out.println();	
//		}
//		else
//		{
//			System.out.println("Receive ACK Number： "+ recvPack.getTcpH().getTh_ack());
//			ackQueue.add(-2);//ACK或NACK传输错误，在ACK队列插入-2
//		}
//			
//		//处理ACK报文
//	    waitACK();
		
		//GBK
		if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) 
		{  
			System.out.println("Receive ACK Number： "+ recvPack.getTcpH().getTh_ack());
			window.receiveACK((recvPack.getTcpH().getTh_ack()-1)/100);
			if (!window.isFull()) 
			{
				this.flag = 1;
			}
		}
	}
	
}
