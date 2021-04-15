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

public class EditSmsOption extends AppCompatActivity {
    EditText titleEditText,numberEditText,detailsEditText;

    SQLiteDatabase database;

    AlertDialog.Builder duplicatedNumberAlert;

    AlertDialog.Builder onBackPressedAlert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sms_option);

        titleEditText=findViewById(R.id.editTextTextPersonName);
        numberEditText=findViewById(R.id.editTextNumber3);
        detailsEditText=findViewById(R.id.detailsEditTextTextPersonName2);



        titleEditText.setText(getIntent().getStringExtra("title"));
        numberEditText.setText(getIntent().getStringExtra("number"));
        detailsEditText.setText(getIntent().getStringExtra("details"));

        database=openOrCreateDatabase("AppDB", Context.MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS SMS(std_number TEXT,std_title TEXT,std_details TEXT)");



    }





    public void saveChanges(View view){

        String title="";
        if(!(titleEditText.getText().toString().equals("") || numberEditText.getText().toString().equals("")
                || detailsEditText.getText().toString().equals(""))){
            Cursor cursor=database.rawQuery("SELECT * FROM SMS WHERE std_number=?",new String[]{numberEditText.getText().toString()});
            boolean duplicateNumber=false;
            if(cursor.getCount()>0){
                while(cursor.moveToNext()){
                    if((cursor.getString(0).equals(numberEditText.getText().toString()))){
                        duplicateNumber=true;
                        break;
                    }
                }
            }
            //update την βάση με τις τιμές των editText
            if(duplicateNumber==false || numberEditText.getText().toString().equals( getIntent().getStringExtra("number"))){
                database.execSQL("UPDATE SMS SET std_number=?,std_title=?,std_details=? WHERE std_number=?",
                        new String[]{numberEditText.getText().toString(),titleEditText.getText().toString(),detailsEditText.getText().toString(),
                                getIntent().getStringExtra("number")});

                finish();
                startActivity(new Intent(getApplicationContext(),SmsOptionsActivity.class));

                //η ίδια διαδικασία που εξηγήθηκε και για το AddSmsOption Activity
            }else{

                duplicatedNumberAlert = new AlertDialog.Builder(this);
                duplicatedNumberAlert.setMessage("Υπάρχει ήδη ένα μήνυμα με τον αριθμό που επιλέξατε:\n"+numberEditText.getText().toString()
                        +title+"\n"+"Επιλέξτε μία απο τις παρακάτω ενέργειες");
                duplicatedNumberAlert.setTitle("ΠΡΟΣΟΧΗ");
                duplicatedNumberAlert.setCancelable(true);
                duplicatedNumberAlert.setNegativeButton("Άκυρο", null);

                duplicatedNumberAlert.setPositiveButton("Συνύπαρξη",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                database.execSQL("UPDATE SMS SET std_number=?,std_title=?,std_details=? WHERE std_number=?",
                                        new String[]{numberEditText.getText().toString(),titleEditText.getText().toString(),detailsEditText.getText().toString(),
                                                getIntent().getStringExtra("number")});

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

        //Άν έγιναν αλλαγές δεν θα αποθηκευτούν
        if(titleEditText.getText().toString().equals(getIntent().getStringExtra("title"))&&
                numberEditText.getText().toString().equals(getIntent().getStringExtra("number"))&&
                detailsEditText.getText().toString().equals(getIntent().getStringExtra("details"))){

            finish();
            startActivity(new Intent(getApplicationContext(),SmsOptionsActivity.class));

        }else{
            onBackPressedAlert = new AlertDialog.Builder(this);
            onBackPressedAlert.setMessage("Οι αλλαγές δεν θα αποθηκευτούν");
            onBackPressedAlert.setTitle("ΠΡΟΣΟΧΗ");
            onBackPressedAlert.setCancelable(true);
            onBackPressedAlert.setNegativeButton("Άκυρο", null);
            onBackPressedAlert.setPositiveButton("Πίσω",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            startActivity(new Intent(getApplicationContext(),SmsOptionsActivity.class));

                        }
                    });

            onBackPressedAlert.create().show();
        }

    }
}