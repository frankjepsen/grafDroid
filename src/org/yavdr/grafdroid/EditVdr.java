package org.yavdr.grafdroid;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

import org.yavdr.grafdroid.dao.GrafDroidDBHelper;
import org.yavdr.grafdroid.dao.pojo.Vdr;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.j256.ormlite.dao.Dao;

public class EditVdr extends Activity {

	private Dao<Vdr, String> dao;
	private EditText ip;
	private EditText name;
	private EditText port;

	private Vdr vdr;
	private boolean isNew = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		GrafDroidDBHelper dbHelper = new GrafDroidDBHelper(
				getApplicationContext());

		try {
			dao = dbHelper.getVdrDao();

			setContentView(R.layout.editvdr);

			ip = ((EditText) findViewById(R.id.ipOfVdr));
			name = (EditText) findViewById(R.id.nameOfVdr);
			port = (EditText) findViewById(R.id.portOfVdr);

			InputFilter[] filters = new InputFilter[1];
			filters[0] = new InputFilter() {
				public CharSequence filter(CharSequence source, int start,
						int end, Spanned dest, int dstart, int dend) {
					if (end > start) {
						String destTxt = dest.toString();
						String resultingTxt = destTxt.substring(0, dstart)
								+ source.subSequence(start, end)
								+ destTxt.substring(dend);
						if (!resultingTxt
								.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
							return "";
						} else {
							String[] splits = resultingTxt.split("\\.");
							for (int i = 0; i < splits.length; i++) {
								if (Integer.valueOf(splits[i]) > 255) {
									return "";
								}
							}
						}
					}
					return null;
				}

			};
			ip.setFilters(filters);

			name.setOnFocusChangeListener(new OnFocusChangeListener() {

				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus && "".equals(ip.getText().toString())) {
						try {
							InetAddress serverAddr = InetAddress
									.getByName(((EditText) v).getText()
											.toString());
							ip.setText(serverAddr.getHostAddress());
						} catch (UnknownHostException e) {
						}
					}
				}
			});

		} catch (SQLException e) {
			dao = null;
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initView();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void saveVdr(View button) {
		try {
			vdr.setName(name.getText().toString());
			vdr.setPort(Integer.parseInt(port.getText().toString()));
			if (isNew) {
				vdr.setAddress(ip.getText().toString());
				dao.create(vdr);
			} else {
				dao.update(vdr);
				if (!vdr.getAddress().equals(ip.getText().toString())) {
					dao.updateId(vdr, ip.getText().toString());
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finish();
	}

	private void initView() {
		final Bundle extras = getIntent().getExtras();

		if (extras != null && extras.getBoolean("add", false)) {
			isNew = true;
			name.setText("");
			ip.setText("");
			port.setText("2039");

			vdr = new Vdr();
		} else if (extras != null && !extras.getBoolean("add", false)) {
			try {
				isNew = false;
				vdr = dao.queryForId(extras.getString("vdr"));
				if (vdr != null) {
					name.setText(vdr.getName());
					ip.setText(vdr.getAddress());
					port.setText(String.valueOf(vdr.getPort()));
				} else {
					finish();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
