package org.yavdr.grafdroid.dao;

import java.sql.SQLException;

import org.yavdr.grafdroid.dao.pojo.Vdr;
import org.yavdr.grafdroid.dao.pojo.VdrAddress;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 * 
 * @author kevingalligan
 */
public class GrafDroidDBHelper extends OrmLiteSqliteOpenHelper {

	private static final String TAG = GrafDroidDBHelper.class.toString();
	private static final String DATABASE_NAME = "grafDroid.db";
	private static final int DATABASE_VERSION = 2;

	private Dao<Vdr, String> vdrDao;
	private Dao<VdrAddress, String> vdrAddress;

	public GrafDroidDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Vdr.class);
			TableUtils.createTable(connectionSource, VdrAddress.class);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to create datbases", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, Vdr.class, true);
			TableUtils.dropTable(connectionSource, VdrAddress.class, true);
			onCreate(sqliteDatabase, connectionSource);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to upgrade database from version " + oldVer + " to new "
					+ newVer, e);
		}
	}

	public Dao<Vdr, String> getVdrDao() throws SQLException {
		if (vdrDao == null) {
			vdrDao = getDao(Vdr.class);
		}
		return vdrDao;
	}
	
	public Dao<VdrAddress, String> getVdrAddressDao() throws SQLException {
		if (vdrAddress == null) {
			vdrAddress = getDao(VdrAddress.class);
		}
		return vdrAddress;
	}
	
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		vdrDao = null;
	}
}