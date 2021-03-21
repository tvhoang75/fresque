package main.collector.indextemplate.distributing3.dispatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerNodeConnection extends Thread{
	protected Socket serverNodeConn;
	protected PrintWriter out;
	protected BufferedReader in;

	public ServerNodeConnection(Socket serverNodeConn) {
        this.serverNodeConn = serverNodeConn;
        try {
            this.out = new PrintWriter(serverNodeConn.getOutputStream());
            in = new BufferedReader(new InputStreamReader(serverNodeConn.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized PrintWriter getOut() {
		return out;
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}
	
	public BufferedReader getIn() {
		return in;
	}

	public void setIn(BufferedReader in) {
		this.in = in;
	}

    public Socket getServerNodeConn() {
		return serverNodeConn;
	}

	public void setServerNodeConn(Socket serverNodeConn) {
		this.serverNodeConn = serverNodeConn;
	}
	
    @Override
    public void run() {
    	
    }
}
