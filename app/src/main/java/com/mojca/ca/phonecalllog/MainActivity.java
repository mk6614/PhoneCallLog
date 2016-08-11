package com.mojca.ca.phonecalllog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    public static TelephonyManager telefon;
    public static SQLiteDatabase baza;
    public Context context = this;
    static ListView klici;
    public static PhoneStateListener p;
    public static String TAG = "MyPhoneStateListener";
    private int prejsnjeStanje = TelephonyManager.CALL_STATE_IDLE;
    private String zgresenKlic = "zgresen klic";
    private String dohodniKlic = "dohodni klic";
    private String odhodniKlic = "odhodni klic";
    private String opis = "";
    private boolean dohodni = false;
    static ArrayList<Klic> list;
    public static View.OnClickListener uredi, izbrisi, imenik;
    public static Intent in;
    public static ListAdapter a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baza=this.openOrCreateDatabase("klicniCenter", Context.MODE_PRIVATE, null);
        baza.execSQL("CREATE TABLE IF NOT EXISTS klic(stevilka VARCHAR,cas VARCHAR, tip VARCHAR, sporocilo VARCHAR);");
        baza.execSQL("CREATE TABLE IF NOT EXISTS imenik(stevilka VARCHAR,naziv VARCHAR);");
        klici = (ListView) findViewById(R.id.klici);
        in = new Intent(context, DialogBoxLayoutActivity.class);

        telefon = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        p = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (state == prejsnjeStanje) {
                    return;
                }

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date;
                opis = "";

                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (prejsnjeStanje!=TelephonyManager.CALL_STATE_IDLE) {
                            Log.i("vpis klica", "onCallStateChanged: CALL_STATE_IDLE prejsnjeStanje: "+prejsnjeStanje);
                            if (prejsnjeStanje == TelephonyManager.CALL_STATE_RINGING) {
                                date  = new Date();
                                MainActivity.baza.execSQL("INSERT INTO klic VALUES('" + incomingNumber + "','" + dateFormat.format(date)
                                        + "','" + zgresenKlic + "','" + opis + "');");
                                Log.i("vpis klica", "zgresen; number: " + incomingNumber + " datum: " + dateFormat.format(date) + " opis:" + opis );
                                zapisi();
                                a = new ListAdapter(context, list);
                                klici.setAdapter(a);
                                klici.invalidateViews();
                                dohodni = false;
                                //Toast.makeText(context, "zgresen klic", Toast.LENGTH_LONG).show();
                            } else if (prejsnjeStanje == TelephonyManager.CALL_STATE_OFFHOOK && dohodni) {
                                date = new Date();
                                MainActivity.baza.execSQL("INSERT INTO klic VALUES('" + incomingNumber + "','" + dateFormat.format(date)
                                        + "','" + odhodniKlic + "','" + opis + "');");
                                //Toast.makeText(context, "dohodni klic", Toast.LENGTH_LONG).show();
                                dohodni = false;
                                Log.i("vpis klica", "odhodni; number: " + incomingNumber + " datum: " + dateFormat.format(date) + " opis:" + opis );
                                Intent in = new Intent(context, DialogBoxLayoutActivity.class);
                                in.putExtra("st", incomingNumber);
                                in.putExtra("d", dateFormat.format(date));
                                startActivityForResult(in, 1);
                            } else {
                                date = new Date();
                                MainActivity.baza.execSQL("INSERT INTO klic VALUES('" + incomingNumber + "','" + dateFormat.format(date)
                                        + "','" + dohodniKlic + "','" + opis + "');");
                                //Toast.makeText(context, "odhodni klic", Toast.LENGTH_LONG).show();
                                dohodni = false;
                                Log.i("vpis klica", "dohodni; number: " + incomingNumber + " datum: " + dateFormat.format(date) + " opis:" + opis );
                                in.putExtra("d", dateFormat.format(date));
                                startActivityForResult(in, 1);
                            }
                        }
                        prejsnjeStanje = TelephonyManager.CALL_STATE_IDLE;
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.i("vpis klica", "onCallStateChanged: CALL_STATE_RINGING");
                        prejsnjeStanje = TelephonyManager.CALL_STATE_RINGING;
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (prejsnjeStanje ==TelephonyManager.CALL_STATE_IDLE) {
                            dohodni = true;
                        }
                        prejsnjeStanje = TelephonyManager.CALL_STATE_OFFHOOK;
                        Log.i("vpis klica", "onCallStateChanged: CALL_STATE_OFFHOOK");
                        break;
                    default:
                        Log.i("vpis klica", "UNKNOWN_STATE: " + state);
                        prejsnjeStanje = -1;
                        break;
                }
            }
        };


        telefon.listen(p, PhoneStateListener.LISTEN_CALL_STATE);
        Log.i("vpis klica", "kokrat se to klice?");

        zapisi();
        a = new ListAdapter(context, list);
        klici.setAdapter(a);

        imenik = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("imenik", "klikklik");
                final Dialog d = new Dialog(context);
                d.setContentView(R.layout.dialog_box_layout);
                d.setTitle("preimenovanje");
                final String st = ((Button)view).getText().toString();
                ((Button) d.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String ime = ((TextView) d.findViewById(R.id.text)).getText().toString();
                        Cursor t = MainActivity.baza.rawQuery("SELECT * FROM imenik WHERE stevilka='" + st + "'", null);
                        if(t.moveToFirst()){
                            Log.i("imenik", "najdeno ime: " + t.getString(0));
                            baza.execSQL("UPDATE imenik SET naziv='" + ime + "' WHERE stevilka='" + st + "'");
                            Log.i("imenik", "UPDATE: " + st + "," + ime);
                        } else {
                            baza.execSQL("INSERT INTO imenik VALUES('" + st + "','" + ime +"');");
                            Log.i("imenik", "INSERT: " + st + "," + ime);
                        }

                        d.dismiss();
                    }
                });
                ((Button) d.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        d.dismiss();
                    }
                });

                d.show();
            }
        };

        uredi = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = (View) view.getParent(); //.getParent();
                TextView st = (TextView) v.findViewById(R.id.cas);
                Log.i("problemX", "uredi: "+st.getText().toString());
                MainActivity.in.putExtra("d", st.getText().toString());
                startActivityForResult(in, 1);
            }
        };

        izbrisi = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = (View) view.getParent(); //.getParent();
                TextView st = (TextView) v.findViewById(R.id.cas);
                String tmp = st.getText().toString();
                Log.i("problemX", "izbrisi: "+tmp);
                baza.execSQL("UPDATE klic SET sporocilo='' WHERE cas='" + tmp + "'");
                zapisi();
                a = new ListAdapter(context, list);
                klici.setAdapter(a);
                klici.invalidateViews();
            }
        };

    }

    public static void zapisi() {
        list = new ArrayList<Klic>();
        if (baza!=null) {
            //klici.setText("");
            Cursor c = baza.rawQuery("SELECT * FROM klic", null);
            // Checking if no records found
            if (c.getCount() == 0) {
                //klici.setText("ni klicev v bazi");
                return;
            }
            Log.i(TAG, "count:" + c.getCount());
            c.moveToLast();
            Klic k = new Klic(c.getString(0), c.getString(1), c.getString(2), c.getString(3));
            list.add(k);
            Log.i(TAG, "Stevilka: " + c.getString(0) + "\n");
            Log.i(TAG,"Datum: " + c.getString(1) + "\n");
            Log.i(TAG,"klic: " + c.getString(2) + "\n");
            Log.i(TAG, "opis: " + c.getString(3) + "\n\n");
            // Appending records to a string buffer
            while (c.moveToPrevious()) {
                k = new Klic(c.getString(0), c.getString(1), c.getString(2), c.getString(3));
                list.add(k);
                Log.i(TAG, "Stevilka: " + c.getString(0) + "\n");
                Log.i(TAG,"Datum: " + c.getString(1) + "\n");
                Log.i(TAG,"klic: " + c.getString(2) + "\n");
                Log.i(TAG,"opis: " + c.getString(3) + "\n\n");
                /*klici.append("Stevilka: " + c.getString(0) + "\n");
                klici.append("Datum: " + c.getString(1) + "\n");
                klici.append("klic: " + c.getString(2) + "\n");
                klici.append("opis: " + c.getString(3) + "\n\n");*/
            }
        } else {
            Log.i("bazaError", " baza je null");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                Log.i("vpis klica", "a se to sploh klice?");
                zapisi();
                a = new ListAdapter(context, list);
                klici.setAdapter(a);
                klici.invalidateViews();

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            zapisi();
            a = new ListAdapter(context, list);
            klici.setAdapter(a);
            klici.invalidateViews();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
