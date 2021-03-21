package main.collector.indextemplate.distributing3.checker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import base.Constants;

public class ServerNodeHandler extends Thread{
	private Socket connServerNode = null;
	private Checker checker = null;
	private PrintWriter outToMerger = null;
	private PrintWriter outToCloud = null;
	private int upperBound = 0;
	protected PrintWriter outToServerNode;
	protected BufferedReader inFromServerNode;

	public ServerNodeHandler(Checker checker, Socket connServerNode, PrintWriter outToMerger, 
							PrintWriter outToCloud, int upperBound) throws IOException {
		this.checker = checker;
		this.connServerNode = connServerNode;
		this.outToCloud = outToCloud;
		this.outToMerger = outToMerger;
		this.upperBound = upperBound;
		this.outToServerNode = new PrintWriter(connServerNode.getOutputStream());
		this.inFromServerNode = 
				new BufferedReader(new InputStreamReader(connServerNode.getInputStream()));
	}
	
	 @Override
	 public void run() {
		 String receivedLine = "";
		 while (true) {
			 try {
				 if ((receivedLine = inFromServerNode.readLine()) != null) {
					 if (receivedLine.contains(Constants.Com2.PUBLISH_BEGIN)) {
						 this.checker.increaseNumPubMessageReceived();
					 }else {
						 this.checker.updateRandomer(receivedLine);
					 }
				 }
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
		 }
	 }
	 
	 public PrintWriter getOutToServerNode() {
		 return outToServerNode;
	 }

	 public void setOutToServerNode(PrintWriter outToServerNode) {
		 this.outToServerNode = outToServerNode;
	 }

	 public BufferedReader getInFromServerNode() {
		 return inFromServerNode;
	 }

	 public void setInFromServerNode(BufferedReader inFromServerNode) {
		 this.inFromServerNode = inFromServerNode;
	 }
}
