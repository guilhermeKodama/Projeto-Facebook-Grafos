package br.ufam.facebookcrawler.internalstorage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class InternalStorageManager {
	public static final String FILE_NAME = "db_status.txt";
	public static final String BD_EMPTY = "db_empty";
	public static final String BD_FILLED = "db_filled";
	public static Context context;

	public static void setContext(Context c) {
		context = c;
	}

	public static void writeFileToInternalStorage(String fileName,
			String message) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					context.openFileOutput(fileName, Context.MODE_PRIVATE)));
			writer.write(message);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static StringBuffer readFileFromInternalStorage(String fileName) {
		BufferedReader input = null;
		StringBuffer buffer = null;
		try {
			input = new BufferedReader(new InputStreamReader(
					context.openFileInput(fileName)));
			String line;
			buffer = new StringBuffer();
			while ((line = input.readLine()) != null) {
				buffer.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buffer;
	}

	public static void extractBDFile(Context ctx) {
		File f = new File("/data/data/br.ufam.facebookcrawler/databases/crawler.db");
		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			fis = new FileInputStream(f);
			fos = new FileOutputStream("/mnt/sdcard/db_dump.db");
			while (true) {
				int i = fis.read();
				if (i != -1) {
					fos.write(i);
				} else {
					break;
				}
			}
			fos.flush();
			Toast.makeText(ctx, "DB dump OK", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(ctx, "DB dump ERROR", Toast.LENGTH_LONG).show();
		} finally {
			try {
				if(fos!=null){
					fos.close();
				}
				if(fis!=null){
					fis.close();
				}
				
			} catch (IOException ioe) {
			}
		}
	}
	//exporting database 
    public static  void exportDB(Context context) {
        // TODO Auto-generated method stub
    	/*Cria o diretorio*/
        File dir = new File(Environment.getExternalStorageDirectory()+"/FBCrawler/");
        if(!dir.exists()){
        	dir.mkdir();
        }
        
        Toast.makeText(context,dir.toString(),Toast.LENGTH_LONG).show();
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
            	
                
                String  currentDBPath= "//data//" + context.getPackageName()
                        + "//databases//" + "crawler.db";
                String backupDBPath  = "/FBCrawler/crawler.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
               Toast.makeText(context, backupDB.toString(),
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
                    .show();

        }
    }
    //importing database
    public static void importDB(Context context) {
        // TODO Auto-generated method stub

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data  = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String  currentDBPath= "//data//" + "PackageName"
                        + "//databases//" + "DatabaseName";
                String backupDBPath  = "/BackupFolder/DatabaseName";
                File  backupDB= new File(data, currentDBPath);
                File currentDB  = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(context, backupDB.toString(),
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
                    .show();

        }
    }
    
    public static boolean checkIfHaveBDInAssets(Context context){
    	try {
			InputStream dbInputStream = context.getAssets().open("crawler.db");
		} catch (IOException e) {
			return false;
		}
    	return true;
	}

}
