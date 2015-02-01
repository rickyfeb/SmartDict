package com.nicoagusto.smartdict;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


/** 
 * halaman depan (main). 
 * yang memunculkan hasil pencarian dari 'search dialog' dan 
 * menghandle aksi apa yg di lakukan dengan hasil. 
 */ 
public class MainActivity extends Activity {
	
	private TextView munculTulisan;
	private ListView barisanKata;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tampilan);
		
		munculTulisan = (TextView) findViewById(R.id.tulisanMuncul);
		barisanKata = (ListView) findViewById(R.id.barisDemiBaris);
		
		handleIntent(getIntent());
	}
	
    @Override  
    protected void onNewIntent(Intent intent) {  
        // oleh karena aktivitas-nya di set dengan launchMode="singleTop", itu berarti   
        // halaman(aktivitas) dapat di daur ulang dalam arti kalau halaman telah   
    	// di buka sebelumnya maka tak perlu di buka lagi tapi hanya menghidupkan   
    	// intent pada halaman tsb sehingga mengangkatnya ke depan (terlihat lagi)  
       
        handleIntent(intent);  
    }

    private void handleIntent(Intent intent) {  
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {  
            // mengatur klik utk search suggestion dan menghidupkan  
         // activity yg membuat kata2 bisa di klik  
            Intent agarKataDptDiKlik = new Intent(this, TampilanKata.class);  
            agarKataDptDiKlik.setData(intent.getData());  
            startActivity(agarKataDptDiKlik);  
            finish();  
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {  
            // mengatur query pencarian  
            String query = intent.getStringExtra(SearchManager.QUERY);  
            showResults(query);  
        }  
    }  

    /** 
     * cari di kamus dan tunjukan hasil dari query tertentu 
     * @parameter query-nya adalah search query 
     */  
    private void showResults(String query) {    
    	Cursor cursor = getContentResolver().query(PenghubungData.CONTENT_URI, null, null,  
                                new String[] {query}, null);  
  
        if (cursor == null) {  
            // kalau tak ada hasil di ketemukan  
            munculTulisan.setText(getString(R.string.tak_ada_hasil, new Object[] {query}));  
        } else {  
            // tunjukan beberapa hasil  
            int count = cursor.getCount();  
            String countString = getResources().getQuantityString(R.plurals.hasil_pencarian,  
                                    count, new Object[] {count, query});  
            munculTulisan.setText(countString);  
  
            // KATA di taruh di kolom 'kolomKata'   
            // ARTI_NYA di taruh di 'kolomArtinya'  
            String[] dari = new String[] { KamusDatabase.KATA,  
                                           KamusDatabase.ARTI_NYA };  
  
            // buatkan hubungan antara design element, dimana kolom akan muncul   
            int[] ke = new int[] { R.id.kolomKata,  
                                   R.id.kolomArtinya };  
  
            // buatkan cursor adapter sederhana utk semua kata dan   
            // artinya dan tayangkan baris demi baris(ListView) pada layar  
            SimpleCursorAdapter letakanKataPadaTempatnya = new SimpleCursorAdapter(this,  
                                          R.layout.hasil, cursor, dari, ke, 0);
            barisanKata.setAdapter(letakanKataPadaTempatnya);  
  
            // apa yg terjadi saat klik pada kata2 yang telah berjajar baris demi baris di layar
            barisanKata.setOnItemClickListener(new OnItemClickListener() {
            	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
                    // ketika sebauh kata dapat di klik  
                    Intent kataDiKlik = new Intent(getApplicationContext(), TampilanKata.class);  
                    Uri data = Uri.withAppendedPath(PenghubungData.CONTENT_URI, String.valueOf(id));  
                    kataDiKlik.setData(data);  
                    startActivity(kataDiKlik);  
                }
			});
        }  
    }  

    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater perpanjangTampilan = getMenuInflater();  
        perpanjangTampilan.inflate(R.menu.untuk_menu, menu);  
  
        SearchManager mengaturSearch = (SearchManager) getSystemService(Context.SEARCH_SERVICE);  
        SearchView tampilanSearch = (SearchView) menu.findItem(R.id.cari).getActionView();  
        tampilanSearch.setSearchableInfo(mengaturSearch.getSearchableInfo(getComponentName()));  
        tampilanSearch.setIconifiedByDefault(false);  
  
        return true;  
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {  
        case R.id.cari:  
            onSearchRequested();  
            return true;  
        default:  
            return false;  
		}  
	}
}