package br.ufam.facebookcrawler.sqlite;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class MySQLiteHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "vigor.db";
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
	public static final String[] allColumnsUsuario = {COLUMN_ID , COLUMN_NOME};
	/*SQL SENTENCES*/
	private static final String CREATE_TABLE_USUARIO = "create table "+TABLE_USUARIO+
													"("+COLUMN_ID+" integer primary key, "
													+COLUMN_NOME+" text not null);";
	
	private static final String CREATE_TABLE_AMIZADE = "create table "+TABLE_AMIZADE+
			"("+COLUMN_ID+" integer not null, "
			+COLUMN_ID_AMIGO+" integer not null,"
			+ "foreign key ("+COLUMN_ID+") references "+TABLE_USUARIO+" ("+COLUMN_ID+"),"
			+ "foreign key ("+COLUMN_ID_AMIGO+") references "+TABLE_USUARIO+" ("+COLUMN_ID+");";
	
	private static final String CREATE_TABLE_INTERACAO = "create table "+TABLE_INTERACAO+
			"("+COLUMN_ID+" integer not null, "
			+COLUMN_TIPO_INTERACAO+" integer not null,"
			+COLUMN_QUANTIDADE+ " integer not null,"
			+COLUMN_ID_AMIGO+" integer not null,"
			+ "foreign key ("+COLUMN_ID+") references "+TABLE_USUARIO+" ("+COLUMN_ID+"),"
			+ "foreign key ("+COLUMN_ID_AMIGO+") references "+TABLE_USUARIO+" ("+COLUMN_ID+"),"
		    + "foreign key ("+COLUMN_TIPO_INTERACAO+") references "+TABLE_TIPO_INTERACAO+" ("+COLUMN_ID+"));";
	
	private static final String CREATE_TABLE_TIPO_INTERACAO = "create table "+TABLE_TIPO_INTERACAO+
			"("+COLUMN_ID+" integer primary key , "
			+COLUMN_DESCRICAO+" text not null);";
	
	private static final String INSERT_TIPO_INTERACAO = "INSERT INTO "+TABLE_TIPO_INTERACAO+""
			+ " SELECT 0 AS "+COLUMN_ID+", 'curtir' AS "+COLUMN_DESCRICAO+""
			+ " UNION SELECT 1, 'comentar'"
			+ " UNION SELECT 2, 'mensagem'"
			+ "UNION SELECT '3', 'compartilhar'";
	
	
	public static final String[] allColums = {COLUMN_ID,
		      COLUMN_NOME };
	/* Cria um "objeto ajudante" para criar , abrir e gerenciar o banco de dados. 
	 *  Esse metodo sempre retorna muito r�pido. 
	 *  O banco de dados s� � criado ou aberto realmente quando o getWritableDatabase() ou
	 *  getReadableDatabase() � chamado.
	 */
	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME,null,DATABASE_VERSION);
		
	}
	/*Chamado quando o banco de dados � criado pela primeira vez. 
	 * Aqui � aonde a cria��o das tabelas e a popula��o inicial das tabelas deve acontecer.
	 */

	@Override
	public void onCreate(SQLiteDatabase db) {
		//Criar� um BD com um nova tabela+colunas vazias.
		db.execSQL(CREATE_TABLE_USUARIO);
		db.execSQL(CREATE_TABLE_TIPO_INTERACAO);
		db.execSQL(INSERT_TIPO_INTERACAO);
		db.execSQL(CREATE_TABLE_INTERACAO);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
