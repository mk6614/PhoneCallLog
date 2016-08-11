package com.mojca.ca.phonecalllog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class DialogBoxLayoutActivity extends ActionBarActivity {

    String opis="";
    String o;
    String d;
    EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_box_layout);
        Intent in = getIntent();
        o = in.getStringExtra("o");
        d = in.getStringExtra("d");
        Log.i("DialogBoxCheck", "cas:" + d);
        et = (EditText) findViewById(R.id.text);

        Log.i("poblemX", "intent: "+ d);
        Cursor t = MainActivity.baza.rawQuery("SELECT * FROM klic WHERE cas='" + d + "'", null);
        t.moveToFirst();
        et.setText(t.getString(3));


        ((Button) findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    opis = et.getText().toString().trim();
                    Log.i("DialogBoxCheck", "nov opis: " + opis);
                    Cursor c = MainActivity.baza.rawQuery("SELECT * FROM klic WHERE cas='" + d + "'", null);
                    if (c.moveToFirst()) {
                        Log.i("DialogBoxCheck", "neki se dogaja");
                        Log.i("DialogBoxCheck", "Stevilka: " + c.getString(0) + "\n");
                        Log.i("DialogBoxCheck", "Datum: " + c.getString(1) + "\n");
                        Log.i("DialogBoxCheck", "klic: " + c.getString(2) + "\n");
                        Log.i("DialogBoxCheck", "opis: " + c.getString(3) + "\n\n");
                    }
                    MainActivity.baza.execSQL("UPDATE klic SET sporocilo='" + opis + "' WHERE cas='" + d + "'");
                    Cursor x = MainActivity.baza.rawQuery("SELECT * FROM klic WHERE cas='" + d + "'", null);
                    if (x.moveToFirst()) {
                        Log.i("DialogBoxCheck", "neki se dogaja");
                        Log.i("DialogBoxCheck", "Stevilka: " + x.getString(0) + "\n");
                        Log.i("DialogBoxCheck", "Datum: " + x.getString(1) + "\n");
                        Log.i("DialogBoxCheck", "klic: " + x.getString(2) + "\n");
                        Log.i("DialogBoxCheck", "opis: " + x.getString(3) + "\n\n");
                    }

                } catch (Exception e) {
                    Log.e("DialogBoxException: ", e.toString());
                }
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();

            }
        });

        ((Button) findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dialog_box_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
