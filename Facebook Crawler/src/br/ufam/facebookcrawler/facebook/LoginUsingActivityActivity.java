package br.ufam.facebookcrawler.facebook;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import br.ufam.facebookcrawler.consultas.ConsultaSQLiteActivity;
import br.ufam.facebookcrawler.crawler.FQLCrawler;
import br.ufam.facebookcrawler.internalstorage.InternalStorageManager;
import br.ufam.facebookcrawler.sqlite.MySQLiteHelper;
import br.ufam.facebookcrawler.sqlite.SQLiteManager;

import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.samples.sessionlogin.R;

/**
 * ACTIVITY QUE VERIFICA SE O USUARIO ESTÁ LOGADO E PEGA INFORMACOES DELE POR
 * FQL
 */
public class LoginUsingActivityActivity extends Activity {
	private static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";

	private TextView textInstructionsOrLink;
	private Button buttonLoginLogout;
	private Button queryButton;
	private Button multiQueryButton;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity);

		buttonLoginLogout = (Button) findViewById(R.id.buttonLoginLogout);
		textInstructionsOrLink = (TextView) findViewById(R.id.instructionsOrLink);
		queryButton = (Button) findViewById(R.id.queryButton);
		multiQueryButton = (Button) findViewById(R.id.multiQueryButton);

		/* Inicializa o BD e cria as tabelas */
		SQLiteManager.open(this);

		/* Show BD STATUS */
		StringBuffer buffer = InternalStorageManager
				.readFileFromInternalStorage(InternalStorageManager.FILE_NAME);
		Toast.makeText(this, "STATUS:" + buffer.toString(), 1).show();

		checkIfBDExist();
		/* Botao da query */
		queryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/* Vai para outra activity */
				Intent intent = new Intent(getBaseContext(), FQLCrawler.class);
				startActivity(intent);
			}
		});

		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(statusCallback));
			}
		}

		updateView();
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		/* Desaloca o helper que permite fazer as queries */
		SQLiteManager.close();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private void checkIfBDExist() {
		try {
			if (InternalStorageManager.checkIfHaveBDInAssets(this)) {
				System.out.println("TO AQUI DENTRO");
				MySQLiteHelper.copyDatabase(this);
				System.out.println("TO AQUI DENTRO2");
				goToConsultas();
				System.out.println("TO AQUI DENTRO3");
				
			} else if (InternalStorageManager.readFileFromInternalStorage(InternalStorageManager.FILE_NAME)
					.toString().equals(InternalStorageManager.BD_FILLED)) {
				goToConsultas();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void goToConsultas(){
		System.out.println("PQPPPP!!!");
		/* BD já esta cheio */
		Intent intent = new Intent(getBaseContext(),ConsultaSQLiteActivity.class);
		startActivity(intent);
	}

	private void updateView() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			textInstructionsOrLink.setText(URL_PREFIX_FRIENDS
					+ session.getAccessToken());
			buttonLoginLogout.setText(R.string.logout);
			buttonLoginLogout.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogout();
				}
			});
		} else {
			textInstructionsOrLink.setText(R.string.instructions);
			buttonLoginLogout.setText(R.string.login);
			buttonLoginLogout.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogin();
				}
			});
		}
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
					.setCallback(statusCallback));
			queryButton.setVisibility(View.VISIBLE);
			multiQueryButton.setVisibility(View.VISIBLE);
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}
	}

	private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
		queryButton.setVisibility(View.INVISIBLE);
		multiQueryButton.setVisibility(View.INVISIBLE);
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			updateView();
		}
	}
}
