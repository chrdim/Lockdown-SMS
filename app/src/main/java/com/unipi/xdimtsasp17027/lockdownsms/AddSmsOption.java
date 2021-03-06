package com.unipi.xdimtsasp17027.lockdownsms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AddSmsOption extends AppCompatActivity {

    EditText titleEditText,numberEditText,detailsEditText;

    SQLiteDatabase database;

    AlertDialog.Builder duplicatedNumberAlert;

    AlertDialog.Builder onBackPressedAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sms_option);

        titleEditText=findViewById(R.id.editTextTextPersonName1);
        numberEditText=findViewById(R.id.editTextNumber4);
        detailsEditText=findViewById(R.id.detailsEditTextTextPersonName1);


        database=openOrCreateDatabase("AppDB", Context.MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS SMS(std_number TEXT,std_title TEXT,std_details TEXT)");

    }

    public void save(View view){
        String title="";
        //έλεγχος για το αν έχουν συμπήρωθεί όλα τα πεδία
        if(!(titleEditText.getText().toString().equals("") || numberEditText.getText().toString().equals("")
                ||detailsEditText.getText().toString().equals(""))){
            Cursor cursor=database.rawQuery("SELECT * FROM SMS WHERE std_number=?",new String[]{numberEditText.getText().toString()});
            boolean duplicateNumber=false;
            //έλεγχος για τον αν το νούμερο που επέλεξε ο χρήστης υπάρχει ήδη
            if(cursor.getCount()>0){
                while(cursor.moveToNext()){
                    if((cursor.getString(0).equals(numberEditText.getText().toString()))){
                        duplicateNumber=true;
                        title=cursor.getString(1);
                        break;
                    }
                }
            }
            if(duplicateNumber==false){
                database.execSQL("INSERT INTO SMS VALUES('"+numberEditText.getText().toString()+"','"+titleEditText.getText().toString()+"','"+detailsEditText.getText().toString()+"')");

                finish();
                startActivity(new Intent(getApplicationContext(),SmsOptionsActivity.class));

            }else{

                //αν ο χρήστης επέλεξε νούμερο που υπάρχει ήδη για άλλο μήνυμα μπορεί να το αντικαταστήσει το παλιό μήνυμα με το καινούριο
                duplicatedNumberAlert = new AlertDialog.Builder(this);
                duplicatedNumberAlert.setMessage("Υπάρχει ήδη ένα μήνυμα με τον αριθμό που επιλέξατε με τίτλο:\n"
                        +title+"\n"+"Επιλέξτε μία απο τις παρακάτω ενέργειες");
                duplicatedNumberAlert.setTitle("ΠΡΟΣΟΧΗ");
                duplicatedNumberAlert.setCancelable(true);
                duplicatedNumberAlert.setNegativeButton("Άκυρο", null);
                duplicatedNumberAlert.setPositiveButton("Αντικατάσταση",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                database.execSQL("UPDATE SMS SET std_number=?,std_title=?,std_details=? WHERE std_number=?",
                                        new String[]{numberEditText.getText().toString(),titleEditText.getText().toString(),detailsEditText.getText().toString(),
                                                numberEditText.getText().toString()});
                                finish();
                                startActivity(new Intent(getApplicationContext(),SmsOptionsActivity.class));

                            }
                        });
                duplicatedNumberAlert.create().show();

            }

        }else{
            Toast.makeText(getApplicationContext(),"Θα πρέπει να συμπληρωθούν όλα τα πεδία.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {


        onBackPressedAlert = new AlertDialog.Builder(this);
        onBackPressedAlert.setMessage("Τα δεδομενα θα χαθουν");
        onBackPressedAlert.setTitle("ΠΡΟΣΟΧΗ");
        onBackPressedAlert.setCancelable(true);
        onBackPressedAlert.setNegativeButton("Άκυρο", null);
        onBackPressedAlert.setPositiveButton("Πίσω",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                        if(getIntent().getStringExtra("fromActivity").equals("smsActivity")){
                            startActivity(new Intent(getApplicationContext(),SmsActivity.class));
                        }else if(getIntent().getStringExtra("fromActivity").equals("smsOptions")) {
                            startActivity(new Intent(getApplicationContext(),SmsOptionsActivity.class));
                        }


                    }
                });

        onBackPressedAlert.create().show();


    }
}