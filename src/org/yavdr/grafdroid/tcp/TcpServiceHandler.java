package org.yavdr.grafdroid.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.yavdr.grafdroid.dao.pojo.Vdr;

import android.app.Activity;

public class TcpServiceHandler implements Runnable {
	private GraphTFTListener _listener;
	private Activity _act;
	private boolean running = true;

	private InetAddress serverAddr;
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	private String server;
	private int port;

	private static final long checkTimeout = 2000l;

	public TcpServiceHandler(GraphTFTListener listener, Activity act, Vdr vdr)
			throws IOException {
		_listener = listener;
		_act = act;
		this.server = vdr.getAddress();
		this.port = vdr.getPort();
		
		try {
			connect();
		} catch (IOException e) {}
	}

	public synchronized void run() {
		// TODO Auto-generated method stub
		// if(socket==null){
		long lastCheck = System.currentTimeMillis();

		while (running) {
			try {
				if (socket != null && socket.isConnected()) {
					try {
						long current = System.currentTimeMillis();
						if (lastCheck + checkTimeout < current) {
							out.writeInt(GraphTFTHeader.CHECK);
							out.writeInt(0);
							lastCheck = current;
						}
						if (in.available() > 8) {
							final GraphTFTHeader header = new GraphTFTHeader();

							header.command = in.readInt();
							header.size = in.readInt();
							int total = 0;

							final byte[] blob;
							if (header.size > 0) {
								blob = new byte[header.size];

								while (total < header.size) {
									int read = in.read(blob, total, header.size
											- total);
									if (read > 0) {
										total += read;
									} else {
										break;
									}
								}
							} else
								blob = null;

							if (total == header.size) {
								this._act.runOnUiThread(new Runnable() {
									public void run() {
										_listener.callCompleted(header, blob);
									}
								});
							} else {
								reconnect();
							}
						}
					} catch (IOException e) {
						reconnect();
					}
				} else
					reconnect();
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
		}

		try {
			disconnect();
		} catch (IOException e) {
		}
	}

	public void stop() {
		running = false;
	}

	public void sendMouseEvent(int x, int y, int button, int flag, int data) {

		try {
			if (socket != null && socket.isConnected()) {
				out.writeInt(GraphTFTHeader.MOUSEEVENT);
				out.writeInt(5 * 4);
				out.writeInt(swapInteger(x));
				out.writeInt(swapInteger(y));
				out.writeInt(swapInteger(button));
				out.writeInt(swapInteger(flag));
				out.writeInt(swapInteger(data));
				/*
				 * out.writeInt(x); out.writeInt(y); out.writeInt(button);
				 * out.writeInt(flag); out.writeInt(data);
				 */
				out.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendSVDRP(String msg) {
		try {
			Socket svdrSocket = new Socket(serverAddr, 6419);
			OutputStream svdrOut = svdrSocket.getOutputStream();
			svdrOut.write(msg.getBytes());
			svdrOut.write('\n');
			svdrOut.write("quit".getBytes());
			svdrOut.write('\n');
			svdrOut.flush();
			svdrOut.close();
			svdrSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void disconnect() throws IOException {
		if (socket != null && !socket.isClosed()) {
			try {
				out.writeInt(GraphTFTHeader.LOGOUT);
				out.writeInt(0);
			} finally {
				out.close();
				in.close();
				socket.close();
			}
		}
	}

	private void connect() throws IOException {
		serverAddr = InetAddress.getByName(this.server);
		socket = new Socket(serverAddr, this.port);
		out = new DataOutputStream(socket.getOutputStream());
		in = new DataInputStream(socket.getInputStream());

		out.writeInt(GraphTFTHeader.WELCOME);
		out.writeInt(0);
	}

	private void reconnect() throws IOException {
		disconnect();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		connect();
	}

	private static int swapInteger(int value) {
		return ((value & 0xFF000000) >> 24) | ((value & 0x00FF0000) >> 8)
				| ((value & 0x0000FF00) << 8) | ((value & 0x000000FF) << 24);
	}


}
