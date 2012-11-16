package org.yavdr.grafdroid.core;

import java.sql.SQLException;

import org.yavdr.grafdroid.dao.GrafDroidDBHelper;
import org.yavdr.grafdroid.dao.pojo.Vdr;
import org.yavdr.grafdroid.dao.pojo.VdrAddress;

import com.j256.ormlite.dao.Dao;

import android.app.Application;

public class GrafDroidApplication extends Application {

	private Vdr currentVdr;
	private Dao<VdrAddress, String> vdrAddressDao;
	private Dao<Vdr, String> vdrDao;
	private boolean finish = false;
	
	public void setCurrentVdr(Vdr vdr) {
		this.setFinish(false);
		if (!vdr.equals(this.currentVdr)) {
			try {
				vdrAddressDao.delete(vdrAddressDao.queryForAll());
				vdrAddressDao.create(new VdrAddress(vdr.getAddress()));
				this.currentVdr = vdr;
			} catch (SQLException e) {}
		}
	}

	public Vdr getCurrentVdr() {
		try {
			if (currentVdr == null && vdrAddressDao.countOf() > 0) {
				currentVdr = vdrDao.queryForId(vdrAddressDao.queryForAll().get(0).getAddress());
			}
		} catch (SQLException e) {}
		return currentVdr;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		GrafDroidDBHelper dbHelper = new GrafDroidDBHelper(getApplicationContext());

		try {
			vdrAddressDao = dbHelper.getVdrAddressDao();
			vdrDao = dbHelper.getVdrDao();
		} catch (SQLException e) {
			vdrAddressDao = null;
			vdrDao = null;
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		
	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}
}
