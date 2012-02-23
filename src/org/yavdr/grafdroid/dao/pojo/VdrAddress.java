package org.yavdr.grafdroid.dao.pojo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "vdraddress")
public class VdrAddress {
	@DatabaseField(id = true)
	private String address;
	public VdrAddress() {
	}

	public VdrAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}
}
