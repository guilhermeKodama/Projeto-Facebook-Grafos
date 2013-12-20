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
	List<String> posts = null;
	List<String> usersWhoComments = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fqlcrawler);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		textview = (TextView) findViewById(R.id.textView1);
		startCrawling();

	}

	public void startCrawling() {
		textview.setText("Criando base de dados....");
		initializeBD();
		textview.setText("Pegando lista de amigos......");
		getFriendsList();
		textview.setText("Pegando likes de amigos..........");
		getLikes();
		/*textview.setText("Pegando amigos que comentaram..........");
		getComments();*/
	}
	private void initializeBD() {
		/* Inicializa o BD e cria as tabelas */
		SQLiteManager.open(this);
	}
	/*Depois que tudo estiver carregado o ultimo metodo chama esse para ir para a proxima activity*/
	private void goToConsultasActivity(){
		Intent intent = new Intent(getBaseContext(),ConsultaSQLiteActivity.class);
		startActivity(intent);
	}

	private void getComments(){
		usersWhoComments = new ArrayList<String>(200);
		for (int i = 0; i < posts.size(); i++) {
			String fqlQuery = new String(
					"select fromid from comment where post_id = " + "'"
							+ posts.get(i) + "'");
			Bundle params = new Bundle();
			params.putString("q", fqlQuery);
			Session session = Session.getActiveSession();
			Request request = new Request(session, "/fql", params,
					HttpMethod.GET, new Request.Callback() {
						public void onCompleted(Response response) {
							/*
							 * Extrai informacoes da resposta do FQL e retorna
							 * um vetor com informacoes para serem inseridos no
							 * BD
							 */
							JSONArray array = extractJSONInformationFromFQL(response);
							for(int j=0;j<array.length();j++){
								
								try {
									JSONObject user  = (JSONObject) array.get(j);
									usersWhoComments.add(user.getString("fromid"));
									System.out.println("ID COMMENT:"+user.getString("fromid"));
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
						}
					});
			Request.executeBatchAsync(request);
		}
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
							posts = new ArrayList<String>(data.length());

							for (int i = 0; i < data.length(); i++) {
								JSONObject o1 = data.getJSONObject(i);
								posts.add(o1.getString("post_id"));
							}
							for (int j = 0; j < data2.length(); j++) {
								JSONObject o2 = data2.getJSONObject(j);
								likesOfFriends.add(o2.getString("user_id"));
							}
							extractHowManyTimesFriendsLikedAPost(likesOfFriends);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						goToConsultasActivity();
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

	/* Pega lista de curtidas e insere no BD */
	private void extractHowManyTimesFriendsLikedAPost(List<String> list) {
		for (int i = 0; i < list.size(); i++) {
			String user = list.get(i);
			SQLiteManager.insertInteration(user, "0",
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
}
