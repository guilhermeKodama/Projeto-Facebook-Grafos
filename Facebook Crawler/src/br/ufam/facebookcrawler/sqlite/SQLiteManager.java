package br.ufam.facebookcrawler.sqlite;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SQLiteManager {
	private static SQLiteDatabase database;
	private static MySQLiteHelper dbhelper;
	private static SQLiteManager instance = null;
	private static Context context;
	
	public SQLiteManager() {
	}

	public static SQLiteManager getInstance(Context c) {
		if (instance == null) {

		}
		return instance;
	}

	public static void open(Context c) {
		instance = new SQLiteManager();
		context = c;
		dbhelper = new MySQLiteHelper(c);

		// Instanciar o objeto para manipula��o do banco de dados
		dbhelper = new MySQLiteHelper(c);
		// Cria e/ou abre o banco de dados que ser� usado para ler e escrever.
		database = dbhelper.getWritableDatabase();
	}

	public static void close() {
		dbhelper.close();
	}

	/* Manipula��o de Banco de dados */
	public static void insertUser(long id, String nome) {
		/* Criando pacotes de informa��o para adicionar no BD */
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_ID, id);
		values.put(MySQLiteHelper.COLUMN_NOME, nome);
		/* Adicionando Pacote ao BD */
		long insertId1 = database.insert(MySQLiteHelper.TABLE_USUARIO, null,
				values);
		/* Consultando para verificar se a inser��o est� no BD */
		Cursor cursor = database.query(MySQLiteHelper.TABLE_USUARIO,
				MySQLiteHelper.allColumnsUsuario, MySQLiteHelper.COLUMN_ID
						+ " = " + insertId1, null, null, null, null);
		/* Mostrando o LOG */
		cursor.moveToFirst();
		Log.w("Banco de Dados", "Foi inserido no id:" + cursor.getLong(0)
				+ "o nome:" + cursor.getString(1));
		cursor.close();
	}

	public static void insertMultipleUsersFromJSONArray(JSONArray arrayData) {
		/* Format para inserir varios elementos no SQLITE */
		/*
		 * INSERT INTO 'tablename' SELECT 'data1' AS 'column1', 'data2' AS
		 * 'column2' UNION SELECT 'data3', 'data4' UNION SELECT 'data5', 'data6'
		 * UNION SELECT 'data7', 'data8'
		 */

		StringBuilder query = new StringBuilder("INSERT INTO "+ MySQLiteHelper.TABLE_USUARIO +" ");
		try {

			for (int i = 0; i < arrayData.length(); i++) {
				JSONObject user = (JSONObject) arrayData.get(i);
				if (i == 0) {
					System.out.println("ENTREIIII 1");
					query.append("SELECT " + user.get("uid") + " AS "
							+ MySQLiteHelper.COLUMN_ID + " , '" + user.get("name")
							+ "' AS "+ MySQLiteHelper.COLUMN_NOME +" ");

				} else {
					System.out.println("ENTREIIII 2");
					query.append("UNION SELECT " + user.get("uid") + " , '"+ user.get("name") +"' ");
				}
			}
			query.append(';');
			Log.e("QUERY",query.toString());
			/* Insere tudo no BD */
			database.execSQL(query.toString());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printTableUSUARIO() {
		Cursor cursor = database.rawQuery("select * from "
				+ MySQLiteHelper.TABLE_USUARIO, null);
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			Log.e("SQLITE","ID:"+cursor.getString(0));
			Log.e("SQLITE","NAME:"+cursor.getString(1));
			cursor.moveToNext();
		}
		/*certificar sempre de fechar*/
		cursor.close();
	}
	
	public static void insertInteration(String id , String type,String quantidade,String id_friend ){
		String query = "INSERT INTO "+MySQLiteHelper.TABLE_INTERACAO+" VALUES"
				+ " ("+id+","+type+","+quantidade+","+id_friend+");";
		Log.e("QUERY",query.toString());
		/* Insere tudo no BD */
		database.execSQL(query);
	}
}