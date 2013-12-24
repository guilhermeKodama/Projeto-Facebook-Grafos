package br.ufam.facebookcrawler.sqlite;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import br.ufam.facebookcrawler.internalstorage.InternalStorageManager;

public class MySQLiteHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "crawler.db";
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE_USUARIO = "usuario";
	public static final String TABLE_AMIZADE = "amizade";
	public static final String TABLE_INTERACAO = "interacao";
	public static final String TABLE_TIPO_INTERACAO = "tipo_interacao";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_QUANTIDADE = "quantidade";
	public static final String COLUMN_ID_AMIGO = "id_amigo";
	public static final String COLUMN_TIPO_INTERACAO = "tipo";
	public static final String COLUMN_NOME = "nome";
	public static final String COLUMN_DESCRICAO = "descricao";
	/**/
	public static final String[] allColumnsUsuario = { COLUMN_ID, COLUMN_NOME };
	/* SQL SENTENCES */
	private static final String CREATE_TABLE_USUARIO = "create table "
			+ TABLE_USUARIO + "(" + COLUMN_ID + " integer primary key, "
			+ COLUMN_NOME + " text not null);";

	private static final String CREATE_TABLE_AMIZADE = "create table "
			+ TABLE_AMIZADE + "(" + COLUMN_ID + " integer not null, "
			+ COLUMN_ID_AMIGO + " integer not null," + "foreign key ("
			+ COLUMN_ID + ") references " + TABLE_USUARIO + " (" + COLUMN_ID
			+ ")," + "foreign key (" + COLUMN_ID_AMIGO + ") references "
			+ TABLE_USUARIO + " (" + COLUMN_ID + ");";

	private static final String CREATE_TABLE_INTERACAO = "create table "
			+ TABLE_INTERACAO + "(" + COLUMN_ID + " integer not null, "
			+ COLUMN_TIPO_INTERACAO + " integer not null," + COLUMN_QUANTIDADE
			+ " integer not null," + COLUMN_ID_AMIGO + " integer not null,"
			+ "foreign key (" + COLUMN_ID + ") references " + TABLE_USUARIO
			+ " (" + COLUMN_ID + ")," + "foreign key (" + COLUMN_ID_AMIGO
			+ ") references " + TABLE_USUARIO + " (" + COLUMN_ID + "),"
			+ "foreign key (" + COLUMN_TIPO_INTERACAO + ") references "
			+ TABLE_TIPO_INTERACAO + " (" + COLUMN_ID + "));";

	private static final String CREATE_TABLE_TIPO_INTERACAO = "create table "
			+ TABLE_TIPO_INTERACAO + "(" + COLUMN_ID
			+ " integer primary key , " + COLUMN_DESCRICAO + " text not null);";

	private static final String INSERT_TIPO_INTERACAO = "INSERT INTO "
			+ TABLE_TIPO_INTERACAO + "" + " SELECT 0 AS " + COLUMN_ID
			+ ", 'curtir' AS " + COLUMN_DESCRICAO + ""
			+ " UNION SELECT 1, 'comentar'" + " UNION SELECT 2, 'mensagem'"
			+ "UNION SELECT '3', 'compartilhar'";

	public static final String[] allColums = { COLUMN_ID, COLUMN_NOME };

	/*
	 * Cria um "objeto ajudante" para criar , abrir e gerenciar o banco de
	 * dados. Esse metodo sempre retorna muito r�pido. O banco de dados s� �
	 * criado ou aberto realmente quando o getWritableDatabase() ou
	 * getReadableDatabase() � chamado.
	 */
	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	/*
	 * Chamado quando o banco de dados � criado pela primeira vez. Aqui � aonde
	 * a cria��o das tabelas e a popula��o inicial das tabelas deve acontecer.
	 */

	@Override
	public void onCreate(SQLiteDatabase db) {
		/* Crate BD log */
		InternalStorageManager.writeFileToInternalStorage(
				InternalStorageManager.FILE_NAME,
				InternalStorageManager.BD_EMPTY);

		// Criar� um BD com um nova tabela+colunas vazias.
		db.execSQL(CREATE_TABLE_USUARIO);
		db.execSQL(CREATE_TABLE_TIPO_INTERACAO);
		db.execSQL(INSERT_TIPO_INTERACAO);
		db.execSQL(CREATE_TABLE_INTERACAO);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	/**
	 * Esse m�todo � respons�vel por copiar o banco do diret�rio assets para o
	 * diret�rio padr�o do android.
	 */
	public static void copyDatabase(Context context) throws IOException {
		String DBPATH = "/data/data/" + context.getPackageName()
                + "/databases/" + "crawler.db";

		// Abre o arquivo o destino para copiar o banco de dados
		OutputStream dbStream = new FileOutputStream(DBPATH);

		// Abre Stream do nosso arquivo que esta no assets
		InputStream dbInputStream = context.getAssets().open(DATABASE_NAME);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = dbInputStream.read(buffer)) > 0) {
			dbStream.write(buffer, 0, length);
		}

		dbInputStream.close();

		dbStream.flush();
		dbStream.close();

	}
	
	
}
