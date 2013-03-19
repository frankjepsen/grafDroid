package org.yavdr.grafdroid;

import java.sql.SQLException;
import java.util.List;

import org.yavdr.grafdroid.adapter.VdrAdapter;
import org.yavdr.grafdroid.core.GrafDroidApplication;
import org.yavdr.grafdroid.dao.GrafDroidDBHelper;
import org.yavdr.grafdroid.dao.pojo.Vdr;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

public class ManageVdr extends ListActivity {
	private static final int CONTEXTMENU_DELETEITEM = 0;
	private static final int CONTEXTMENU_EDITITEM = 1;

	private Dao<Vdr, String> dao;
	private ListView lv;
	private List<Vdr> knownVdr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		GrafDroidDBHelper dbHelper = new GrafDroidDBHelper(
				getApplicationContext());

		lv = getListView();
		lv.setTextFilterEnabled(true);

		try {
			dao = dbHelper.getVdrDao();
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

		try {
			if (dao.countOf() == 0) {
				Toast.makeText(getApplicationContext(),
						"keine VDR konfiguriert.", Toast.LENGTH_LONG).show();
			} else {
				final Bundle extras = getIntent().getExtras();

				if (extras != null && extras.getBoolean("offline", false)) {
					Toast.makeText(getApplicationContext(),
							"VDR nicht online.", Toast.LENGTH_LONG).show();
				}
			}
		} catch (SQLException e) {
		}

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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position < knownVdr.size()) {
			Vdr vdr = knownVdr.get(position);
			if (vdr != null && vdr.isOnline()) {
				((GrafDroidApplication) getApplication()).setCurrentVdr(vdr);
				setResult(0);
				finish();
			} else {
				refreshListItems();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.managevdr, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.add_vdr:
			Intent intent = new Intent(
					"org.yavdr.grafdroid.intent.action.ADDVDR");
			intent.putExtra("add", true);
			startActivity(intent);
			return true;
		case R.id.exit:
			((GrafDroidApplication) getApplication()).setFinish(true);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refreshListItems() {
		try {
			knownVdr = dao.queryForAll();

			setListAdapter(new VdrAdapter(getApplicationContext(),
					R.layout.managevdr_item, knownVdr, this));
		} catch (SQLException e) {
		}

	}

	private void initView() {
		/* Loads the items to the ListView. */
		refreshListItems();

		/* Add Context-Menu listener to the ListView. */
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				Vdr vdr = ((VdrAdapter.ViewHolder) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView
						.getTag()).vdr;
				menu.setHeaderTitle(vdr.getName());
				menu.add(0, CONTEXTMENU_EDITITEM, 0, "Eintrag bearbeiten");
				menu.add(0, CONTEXTMENU_DELETEITEM, 0, "Eintrag löschen");

			}
		});
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenuInfo menuInfo = (ContextMenuInfo) item.getMenuInfo();
		Vdr vdr;
		/* Switch on the ID of the item, to get what the user selected. */
		switch (item.getItemId()) {
		case CONTEXTMENU_DELETEITEM:
			vdr = ((VdrAdapter.ViewHolder) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView
					.getTag()).vdr;
			try {
				if (vdr != null && vdr.equals(((GrafDroidApplication) getApplication()).getCurrentVdr()))
					((GrafDroidApplication) getApplication()).setCurrentVdr(null);
				dao.delete(vdr);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			refreshListItems();
			return true; /* true means: "we handled the event". */
		case CONTEXTMENU_EDITITEM:
			vdr = ((VdrAdapter.ViewHolder) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView
					.getTag()).vdr;
			Intent intent = new Intent(
					"org.yavdr.grafdroid.intent.action.EDITVDR");
			intent.putExtra("add", false);
			intent.putExtra("vdr", vdr.getAddress());
			startActivity(intent);
			return true; /* true means: "we handled the event". */
		}
		return super.onContextItemSelected(item);
	}

/*	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Vdr vdr = ((GrafDroidApplication) getApplication()).getCurrentVdr();
		if (vdr == null || !vdr.isOnline()) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				// build exit Dialog!
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Möchten Sie die App wirklich beenden?")
						.setCancelable(false)
						.setPositiveButton("Ja",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										
										((GrafDroidApplication) getApplication()).setFinish(true);
										ManageVdr.this.finish();
										return;
									}
								})
						.setNegativeButton("Nein",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										((GrafDroidApplication) getApplication()).setFinish(false);
										return;
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
*/
}
