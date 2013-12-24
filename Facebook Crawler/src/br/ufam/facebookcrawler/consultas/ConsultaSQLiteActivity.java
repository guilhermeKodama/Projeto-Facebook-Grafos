package br.ufam.facebookcrawler.consultas;

import br.ufam.facebookcrawler.internalstorage.InternalStorageManager;
import br.ufam.facebookcrawler.sqlite.SQLiteManager;

import com.facebook.samples.sessionlogin.R;
import com.facebook.samples.sessionlogin.R.layout;
import com.facebook.samples.sessionlogin.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;

public class ConsultaSQLiteActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consulta_sqlite);
		/*Show BD STATUS*/
		StringBuffer buffer = InternalStorageManager.
		readFileFromInternalStorage(InternalStorageManager.FILE_NAME);
		Toast.makeText(this,"STATUS:"+buffer.toString(),1).show();
		InternalStorageManager.exportDB(getBaseContext());
		SQLiteManager.queryUser(this);
		/*Comecar a desenvolver aqui*/
	}
	@Override
	protected void onStart(){
		super.onStart();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.consulta_sqlite, menu);
		return true;
	}

}
