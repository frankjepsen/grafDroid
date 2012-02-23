package org.yavdr.grafdroid.dao.pojo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "vdr")
public class Vdr {
	@DatabaseField
	private String name;

	@DatabaseField
	private int port;

	@DatabaseField(id = true)
	private String address;

	private transient InetAddress inetAddress = null;
	
	public Vdr() {
	}

	public Vdr(String hostName, String hostAddress, int port) {
		this.name = hostName;
		this.address = hostAddress;
		this.port = port;
		
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public boolean isOnline() {
		if (inetAddress == null) {
			try {
				inetAddress = InetAddress.getByName(address);
			} catch (UnknownHostException e) {
				return false;
			}
		}
		
		try {
			return inetAddress.isReachable(100);
		} catch (IOException e) {
			return false;
		}
	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public boolean equals(Object o) {
		Vdr v = (Vdr)o;
		
		if (v == null) return false;
		return this.address.equals(v.address);
	}
}
