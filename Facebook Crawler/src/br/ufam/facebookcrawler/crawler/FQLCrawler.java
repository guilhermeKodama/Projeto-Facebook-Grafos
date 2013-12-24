package br.ufam.facebookcrawler.crawler;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import br.ufam.facebookcrawler.consultas.ConsultaSQLiteActivity;
import br.ufam.facebookcrawler.internalstorage.InternalStorageManager;
import br.ufam.facebookcrawler.sqlite.SQLiteManager;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.samples.sessionlogin.R;

public class FQLCrawler extends Activity {
	ProgressBar progressBar;
	TextView textview;
	private static boolean GOT_FRIENDS;
	private static boolean GOT_LIKES;
	private static boolean GOT_COMMENTS;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fqlcrawler);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		textview = (TextView) findViewById(R.id.textView1);
		GOT_FRIENDS = false;
		GOT_LIKES = false;
		GOT_COMMENTS = false;
		startCrawling();
	}

	public void startCrawling() {
		textview.setText("Aguarde....");
		getFriendsList();
		getLikes();
		getComments();
		checkIfEverythingIsDone();
	}

	/*
	 * Depois que tudo estiver carregado o ultimo metodo chama esse para ir para
	 * a proxima activity
	 */
	private void goToConsultasActivity() {
		Intent intent = new Intent(getBaseContext(),
				ConsultaSQLiteActivity.class);
		startActivity(intent);
	}

	private void getComments() {
		String fqlQuery = "{\"posts\": \"select post_id, likes.count from stream where source_id = me()"
				+ " and likes.count > 0 limit 500\","
				+ "\"friends_who_comments_post\":\"select fromid from comment where post_id IN "
				+ "(SELECT post_id FROM #posts)\"}";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);
		Session session = Session.getActiveSession();
		Request request = new Request(session, "/fql", params, HttpMethod.GET,
				new Request.Callback() {
					public void onCompleted(Response response) {
						/*
						 * Extrai informacoes da resposta do FQL e retorna um
						 * vetor com informacoes para serem inseridos no BD
						 */
						try {
							List<String> commentsOfFriends;
							GraphObject go = response.getGraphObject();
							JSONObject jso = go.getInnerJSONObject();
							JSONArray data = jso.getJSONArray("data")
									.getJSONObject(0)
									.getJSONArray("fql_result_set");

							JSONArray data2 = jso.getJSONArray("data")
									.getJSONObject(1)
									.getJSONArray("fql_result_set");

							commentsOfFriends = new ArrayList<String>(data2
									.length());
							/*
							 * posts = new ArrayList<String>(data.length());
							 * 
							 * for (int i = 0; i < data.length(); i++) {
							 * JSONObject o1 = data.getJSONObject(i);
							 * posts.add(o1.getString("post_id")); }
							 */
							for (int j = 0; j < data2.length(); j++) {
								JSONObject o2 = data2.getJSONObject(j);
								commentsOfFriends.add(o2.getString("fromid"));
							}
							extractInterationAndInsertInBD(commentsOfFriends,"1");
							GOT_COMMENTS = true;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
		Request.executeBatchAsync(request);
	}

	private void getLikes() {
		String fqlQuery = "{\"posts\": \"select post_id, likes.count from "
				+ "stream where source_id = me() and likes.count > 0 limit 500\","
				+ "\"friends_who_like_posts\":\"select user_id FROM like WHERE post_id IN "
				+ "(SELECT post_id FROM #posts) AND user_id IN (SELECT uid2 FROM friend "
				+ "WHERE uid1 = me() )\"}";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);
		Session session = Session.getActiveSession();
		Request request = new Request(session, "/fql", params, HttpMethod.GET,
				new Request.Callback() {
					public void onCompleted(Response response) {
						/*
						 * Extrai informacoes da resposta do FQL e retorna um
						 * vetor com informacoes para serem inseridos no BD
						 */
						try {
							List<String> likesOfFriends;
							GraphObject go = response.getGraphObject();
							JSONObject jso = go.getInnerJSONObject();
							JSONArray data = jso.getJSONArray("data")
									.getJSONObject(0)
									.getJSONArray("fql_result_set");

							JSONArray data2 = jso.getJSONArray("data")
									.getJSONObject(1)
									.getJSONArray("fql_result_set");

							likesOfFriends = new ArrayList<String>(data2
									.length());
							/*
							 * posts = new ArrayList<String>(data.length());
							 * 
							 * for (int i = 0; i < data.length(); i++) {
							 * JSONObject o1 = data.getJSONObject(i);
							 * posts.add(o1.getString("post_id")); }
							 */
							for (int j = 0; j < data2.length(); j++) {
								JSONObject o2 = data2.getJSONObject(j);
								likesOfFriends.add(o2.getString("user_id"));
							}
							extractInterationAndInsertInBD(likesOfFriends,"0");
							GOT_LIKES = true;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
		Request.executeBatchAsync(request);
	}

	private void getFriendsList() {
		String fqlQuery = "SELECT uid, name FROM user WHERE uid IN "
				+ "(SELECT uid2 FROM friend WHERE uid1 = me() LIMIT 100)";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);
		Session session = Session.getActiveSession();
		Request request = new Request(session, "/fql", params, HttpMethod.GET,
				new Request.Callback() {
					public void onCompleted(Response response) {
						/*
						 * Extrai informacoes da resposta do FQL e retorna um
						 * vetor com informacoes para serem inseridos no BD
						 */
						SQLiteManager
								.insertMultipleUsersFromJSONArray(extractJSONInformationFromFQL(response));
						SQLiteManager.printTableUSUARIO();
						GOT_FRIENDS = true;
					}
				});
		Request.executeBatchAsync(request);
	}

	/**
	 * Extrai informacões da consulta FQL em objetos JSON e retorna um vetor com
	 * esses objetos
	 */
	private JSONArray extractJSONInformationFromFQL(Response response) {
		JSONObject jsonData;
		JSONArray arrayData = null;
		try {
			jsonData = response.getGraphObject().getInnerJSONObject();
			arrayData = jsonData.getJSONArray("data");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return arrayData;
	}

	/*Recebe lista de usuario e o tipo de interacao que eles realizaram e insere na tabela interacao*/
	private void extractInterationAndInsertInBD(List<String> list,String typeOfInteration) {
		for (int i = 0; i < list.size(); i++) {
			String user = list.get(i);
			SQLiteManager.insertInteration(user, typeOfInteration,
					String.valueOf(getReplications(user, list, i)), user);
		}
	}

	/* Pega a quantidade de vezes que uma string aparece em uma lista */
	private int getReplications(String user, List<String> list, int i) {
		int replications = 0;
		for (int j = i; j < list.size(); j++) {
			if (list.get(j).equals(user)) {
				list.remove(j);
				replications++;
			}
		}
		return replications;
	}
	private void checkIfEverythingIsDone(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				while(GOT_FRIENDS != true || GOT_FRIENDS != true || GOT_COMMENTS != true){
					//do something
				}
				InternalStorageManager.writeFileToInternalStorage(
						InternalStorageManager.FILE_NAME,InternalStorageManager.BD_FILLED);
				goToConsultasActivity();
			}
		}).start();

	}
}
