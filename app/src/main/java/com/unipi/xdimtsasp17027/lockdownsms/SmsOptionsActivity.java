package com.unipi.xdimtsasp17027.lockdownsms;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SmsOptionsActivity extends AppCompatActivity {

    SQLiteDatabase database;
    ImageView addButton,editButton;
    LinearLayout layout;
    ArrayList<View> viewArrayList;
    ArrayList<ImageView> editButtonList;
    ArrayList<String> numbersList,titlesList,detailsList;


    TextView smsNumber,smsTitle,smsDetails;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_options);



        numbersList=new ArrayList<String>();
        titlesList=new ArrayList<String>();
        detailsList=new ArrayList<String>();

        editButtonList =new ArrayList<ImageView>();

        addButton=findViewById(R.id.imageView11);


        layout=findViewById(R.id.linearLayout);

        database=openOrCreateDatabase("AppDB", Context.MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS SMS(std_number TEXT,std_title TEXT,std_details TEXT)");


        viewArrayList=new ArrayList<View>();

        Cursor cursor=database.rawQuery("SELECT * FROM SMS",null);

        if(cursor.getCount()>0){


            //για κάθε μήνυμα δημιουργείται ένα view τύπου row_edit_sms_option.
            //Το row_show_sms_option είναι ένα layout που έχουμε δημιουργήσει βάση του ο οποίου θα εμφανίζεται κάθε επιλογή μηνύματος της βάσης
            //ακολουθείται η ίδια διαδικασία που εξηγήθηκε στο smsActivity
            while(cursor.moveToNext()){
                View v=getLayoutInflater().inflate(R.layout.row_edit_sms_option,null,false);
                smsNumber=(TextView) v.findViewById(R.id.numberTextView1);
                smsTitle=(TextView) v.findViewById(R.id.titleTextView1);
                smsDetails=(TextView) v.findViewById(R.id.detailsTextView1);
                editButton=(ImageView) v.findViewById(R.id.imageView20);
                viewArrayList.add(v);

                smsNumber.setText(cursor.getString(0));
                smsTitle.setText(cursor.getString(1));
                smsDetails.setText(cursor.getString(2));

                editButtonList.add(editButton);
                detailsList.add(cursor.getString(2));
                titlesList.add(cursor.getString(1));
                numbersList.add(cursor.getString(0));



            }

        }

        for(View v:viewArrayList){
            layout.addView(v);

        }





        for(int i=0;i<editButtonList.size();i++){
            String number=numbersList.get(i);
            String title=titlesList.get(i);
            String details=detailsList.get(i);


            editButtonList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), EditSmsOption.class)
                            .putExtra("number", number)
                            .putExtra("title", title)
                            .putExtra("details", details));
                }
            });
        }

    }

    public void goToAddsmsOption(View view){
        startActivity(new Intent(getApplicationContext(), AddSmsOption.class).putExtra("fromActivity","smsOptions"));

    }

    @Override
    public void onBackPressed() {

        finish();
        startActivity(new Intent(getApplicationContext(),SmsActivity.class));

    }

}