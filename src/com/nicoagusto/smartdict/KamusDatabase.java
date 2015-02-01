package com.nicoagusto.smartdict;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

  
/** 
 * logika utk mengambil data dari database, dan 
 * memasang table, kolom, baris yang di butuhkan. 
 */  
public class KamusDatabase {  
    private static final String TAG = "KamusDatabase";  
  
    // kolom yang akan di pakai di table-nya database kamus  
    // yaitu kolom KATA dan kolom ARTI_NYA  
    public static final String KATA = SearchManager.SUGGEST_COLUMN_TEXT_1;  
    public static final String ARTI_NYA = SearchManager.SUGGEST_COLUMN_TEXT_2;  
  
    private static final String NAMA_DATABASE = "kamus";  
    private static final String TEMPAT_MUNCUL_KATA = "membentangKebawah";  
    private static final int VERSI_DATABASE = 7;  
    // perantara antara database dan aplikasi  
    private final DictionaryOpenHelper pembukaDatabase;  
    private static final HashMap<String,String> penghubungKolom = buatPenghubungKolom();  
  
      
     // berikut adalah Constructor-nya  
     // @parameter context maksudnya pada konteks apa class ini bekerja,   
     // dalam hal ini adalah utk membuat database  
       
    public KamusDatabase(Context context) {  
     pembukaDatabase = new DictionaryOpenHelper(context);  
    }  
  
      
     // membuat penghubung(map) utk semua kolom yg   
     // akan di butuhkan, yang di dalam-nya akan di pasang   
     // SQLiteQueryBuilder. Ini adalah suatu cara yang   
     // baik untuk mendefinisikan nama alias dari kolom2  
     // tapi hal itu berarti harus menyertakan   
     // semua kolom yg ada, termasuk kolom key   
     // dengan demikian peluang ContentProvider terbuka   
     // lebar untuk mencari kolom tanpa  
     // mengenal nama asli kolom sehingga   
     // bisa membuat nama kolom samaran(alias) on the go  
     // bila di butuhkan  
       
    private static HashMap<String,String> buatPenghubungKolom() {  
        HashMap<String,String> menghubungkan = new HashMap<String,String>();  
        menghubungkan.put(KATA, KATA);  
        menghubungkan.put(ARTI_NYA, ARTI_NYA);  
        menghubungkan.put(BaseColumns._ID, "rowid AS " +  
                BaseColumns._ID);  
        menghubungkan.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +  
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);  
        menghubungkan.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +  
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);  
        return menghubungkan;  
    }  
  
      
     // letak-kan cursor pada kata yang di   
     // identifikasikan dengan rowId  
     // @parameter barisId adalah Id dari kata yang mau di cari  
     // @parameter kolom adalah salah satu kolom yg termasuk dalam search,   
     // kalau kolom==null maka semua   
     // kolom berarti di ikut sertakan dalam search  
     // @return Cursor tempatkan cursor pada kata yang cocok, atau  
     // null kalau tak menemukan kata yang cocok  
       
    public Cursor getWord(String barisID, String[] kolom) {  
        String pilihKata = "rowId = ?";  
        String[] kolomTempatKata = new String[] {barisID};  
  
        return query(pilihKata, kolomTempatKata, kolom);  
  
          
         //  sama dengan:  
         //     SELECT <columns> FROM <table> WHERE barisID = <rowId>  
           
    }  
  
      
     // Cursor akan menemukan semua kata yang   
     // sesuai dengan search pencari  
       
     // @parameter query adalah string utk mendapatkan isi   
     // @parameter kolom agar kolom tetentu termasuk   
     // dalam pencarian, tapi kalau null maka  
     // semuanya kolom teramsuk dalam pencarian  
     // @return Cursor ambil semua kata yg cocok,   
     // atau null kalau tak satupun ygcocok.  
       
    public Cursor getWordMatches(String cariKata, String[] kolom) {  
        String pilihKata = KATA + " MATCH ?";  
        String[] tempatPenampungKata = new String[] {cariKata+"*"};  
  
        return query(pilihKata, tempatPenampungKata, kolom);  
  
         
         // sama seperti mysql  
         //     SELECT <columns> FROM <table> WHERE   
         //     <KEY_WORD> MATCH 'query*'  
         // yang adalah bentuk search text utk   
         // FTS3 (tambah sebuah wildcard) didalam kolom kata2  
         // fts3(full text search versi 3).  
           
         // - "rowid" adalah unik utk semua baris tapi   
         // kita butuh nilai utk kolom "_id"  agar  
         // adapter dapat bekerja, jadi kolom alias adalah   
         // "_id" untuk "rowid"  
         // - "rowid" juga perlu di gunakan pada   
         // SUGGEST_COLUMN_INTENT_DATA alias sebagai  
         //   pertimbangan dalam melakukan   
         //   cek intent data dengan benar.  
         //   Semua alias yg di maksud di jelaskan dalam   
         //   class 'PenghubungData' pada pembuatan query.  
         // - hal tsb juga dpt di rubah kalau mau search melalui   
         // ARTI_NYA daripada men-search lewat KATA  
         // hal tsb dapat di lakukan dengan merubah   
         // pencarian pakai FTS3 dengan mengganti KEY_WORD dgn  
         // FTS_VIRTUAL_TABLE (soearch bolak-balik) tapi   
         // hal tsb akan menyulitkan pengaturan abjad karena  
         // kadang satu kata akan memiliki arti lebih dari satu kata  
           
    }  
  
      
     // melakukan query ke database.  
     // @parameter 'pilihan' adalah mengatur memilih kata  
     // @param gudangKataKata adalah pilihan arguments   
     // untuk komponen "?" dalam pilihan  
     // @param kolom adalah kolom untuk di isi (return)  
     // @return A Cursor pada semua baris yang   
     // cocok dengan tujuan pencarian  
       
    private Cursor query(String pilihan, String[] gudangKataKata, String[] kolom) {  
          
         // SQLiteBuilder menyediakan sebuah   
         // penghubung(map)utk semua kolom yg di request dengan  
         // kolom sebenarnya di databse, membuat   
         // mekanisme kolom aliasnya yg sederhana   
         // yang mana PenghubungData tak perlu   
         // tahu nama kolom sebenarnya  
           
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();  
        builder.setTables(TEMPAT_MUNCUL_KATA);  
        builder.setProjectionMap(penghubungKolom);  
  
        Cursor cursor = builder.query(pembukaDatabase.getReadableDatabase(),  
                kolom, pilihan, gudangKataKata, null, null, null);  
  
        if (cursor == null) {  
            return null;  
        } else if (!cursor.moveToFirst()) {  
            cursor.close();  
            return null;  
        }  
        return cursor;  
    }  
  
  
      
     // ini utk membuat/membuka databasenya.  
       
    private static class DictionaryOpenHelper extends SQLiteOpenHelper {  
  
        private final Context bantuBukaDB;  
        private SQLiteDatabase databasenya;  
  
          
         // catatan bahwa FTS3 tidak mendukung   
         // hukum kolom oleh karena itu  
         // tak dapat mendeklarasikan sebuah   
         // primary key. Namun demikian, "rowid" akan    
         // di gunakan secara otomais sebagai   
         // unique identifier maka ketika membuat pencarian,  
         //  kita akan gunakan "_id" sebagai sebuah  
         //  alias untuk "rowid"  
           
        private static final String FTS_TABLE_CREATE =  
                    "CREATE VIRTUAL TABLE " + TEMPAT_MUNCUL_KATA +  
                    " USING fts3 (" +  
                    KATA + ", " +  
                    ARTI_NYA + ");";  
  
        DictionaryOpenHelper(Context bantuBukaKamus) {  
            super(bantuBukaKamus, NAMA_DATABASE, null, VERSI_DATABASE);  
            bantuBukaDB = bantuBukaKamus;  
        }  
  
        @Override  
        public void onCreate(SQLiteDatabase membuatDatabase) {  
            databasenya = membuatDatabase;  
            databasenya.execSQL(FTS_TABLE_CREATE);  
            masukanDiKamus();  
        }  
  
          
          
         // mulai sebuah thread utk masukan sebuah   
         // table di database beserta kata-katanya  
           
        private void masukanDiKamus() {  
            new Thread(new Runnable() {  
                public void run() {  
                    try {  
                        masukanKataKata();  
                    } catch (IOException e) {  
                        throw new RuntimeException(e);  
                    }  
                }  
            }).start();  
        }  
  
        private void masukanKataKata() throws IOException {  
            Log.d(TAG, "Please Wait... Loading Database...");  
            final Resources sumberKataKata = bantuBukaDB.getResources();  
            InputStream masukanKataKata = sumberKataKata.openRawResource(R.raw.jawa_indo);
            //InputStream masukanKataKata = sumberKataKata.openRawResource(R.raw.jawa_indo);  
            BufferedReader membacaKataKata = new BufferedReader(new InputStreamReader(masukanKataKata));  
  
            try {  
                String barisanKataKata;  
                while ((barisanKataKata = membacaKataKata.readLine()) != null) {  
                    String[] daftarKataKata = TextUtils.split(barisanKataKata, "-");  
                    if (daftarKataKata.length < 2) continue;  
                    long idKataKata = tambahKataKata(daftarKataKata[0].trim(), daftarKataKata[1].trim());  
                    if (idKataKata < 0) {  
                        Log.e(TAG, "tak bisa menambah Kata: " + daftarKataKata[0].trim());  
                    }  
                }  
            } finally {  
             membacaKataKata.close();  
            }  
            Log.d(TAG, "selesai loading kata-kata.");  
        }  
  
         
         // tambah kata untuk membentang ke bawah.  
         // @return rowId or -1 jikalau gagal  
           
        public long tambahKataKata(String kata, String arti_nya) {  
            ContentValues jajaranKataKata = new ContentValues();  
            jajaranKataKata.put(KATA, kata);  
            jajaranKataKata.put(ARTI_NYA, arti_nya);  
  
            return databasenya.insert(TEMPAT_MUNCUL_KATA, null, jajaranKataKata);  
        }  
  
        @Override  
        public void onUpgrade(SQLiteDatabase gantiDataBase, int versiLama, int versiBaru) {  
            Log.w(TAG, "Database di upgrade dari versi " + versiLama + " ke "  
                    + versiBaru + ", dan akan menghapus semua data yg lama");  
            gantiDataBase.execSQL("DROP TABLE IF EXISTS " + TEMPAT_MUNCUL_KATA);  
            onCreate(gantiDataBase);  
        }  
    }  
  
}