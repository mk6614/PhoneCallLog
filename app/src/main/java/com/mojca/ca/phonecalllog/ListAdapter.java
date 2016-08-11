package com.mojca.ca.phonecalllog;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mojca on 10/08/2016.
 */
public class ListAdapter extends ArrayAdapter<Klic> {

    public ListAdapter(Context context, ArrayList<Klic> klici) {
        super(context, 0, klici);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Klic klic = getItem(position);
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter, parent, false);

        ImageView vrsta = (ImageView) convertView.findViewById(R.id.vrsta);
        Button st = (Button) convertView.findViewById(R.id.st);
        TextView cas = (TextView) convertView.findViewById(R.id.cas);
        ImageButton izbrisi = (ImageButton) convertView.findViewById(R.id.izbrisi);
        ImageButton uredi = (ImageButton) convertView.findViewById(R.id.uredi);
        TextView opis = (TextView) convertView.findViewById(R.id.opis);

        Cursor t = MainActivity.baza.rawQuery("SELECT * FROM imenik WHERE stevilka='" + klic.st + "'", null);
        if(t.moveToFirst()){
            Log.i("imenik", "najdeno ime: "+ t.getString(1));
            st.setText(t.getString(1));
        } else {
            st.setText(klic.st);
        }
        cas.setText(klic.cas);
        opis.setText(klic.opis);

        if (klic.tip.equals("zgresen klic")){
            vrsta.setImageResource(R.drawable.zgresen32);
        } else if (klic.tip.equals("dohodni klic")) {
            vrsta.setImageResource(R.drawable.dohodni32);
        } else {
            vrsta.setImageResource(R.drawable.odhodni32);
        }

        if (klic.opis.trim().length()==0){
            izbrisi.setVisibility(View.INVISIBLE);
            uredi.setImageResource(R.drawable.dodaj64);
        } else {
            izbrisi.setVisibility(View.VISIBLE);
            izbrisi.setImageResource(R.drawable.izbrisi64);
            uredi.setImageResource(R.drawable.uredi64);
        }

        uredi.setOnClickListener(MainActivity.uredi);
        izbrisi.setOnClickListener(MainActivity.izbrisi);
        st.setOnClickListener(MainActivity.imenik);


        return convertView;
    }
}
