package com.nicoagusto.smartdict;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
  
/** 
 * class ini utk menampilkan kata-kata dan artinya. 
 */  
public class TampilanKata extends Activity {  
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.kata);  
  
        ActionBar tempatTampilKata = getActionBar();  
        tempatTampilKata.setDisplayHomeAsUpEnabled(true);  
  
        Uri uri = getIntent().getData();  
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);  
  
        if (cursor == null) {  
            finish();  
        } else {  
            cursor.moveToFirst();  
  
            TextView kataKata = (TextView) findViewById(R.id.kolomKata);  
            TextView artinya = (TextView) findViewById(R.id.kolomArtinya);  
  
            int indexKata = cursor.getColumnIndexOrThrow(KamusDatabase.KATA);  
            int indexArtinya = cursor.getColumnIndexOrThrow(KamusDatabase.ARTI_NYA);  
  
            kataKata.setText(cursor.getString(indexKata));  
            artinya.setText(cursor.getString(indexArtinya));  
        }  
    }  
  
    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        MenuInflater inflater = getMenuInflater();  
        inflater.inflate(R.menu.untuk_menu, menu);  
  
        SearchManager aturPencarian = (SearchManager) getSystemService(Context.SEARCH_SERVICE);  
        SearchView tampilanPencarian = (SearchView) menu.findItem(R.id.cari).getActionView();  
        tampilanPencarian.setSearchableInfo(aturPencarian.getSearchableInfo(getComponentName()));  
        tampilanPencarian.setIconifiedByDefault(false);  
          
        return true;  
    }  
  
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        switch (item.getItemId()) {  
            case R.id.cari:  
                onSearchRequested();  
                return true;  
            case android.R.id.home:  
                Intent intent = new Intent(this, MainActivity.class);  
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
                startActivity(intent);  
                return true;  
            default:  
                return false; 
        }  
    }  
}