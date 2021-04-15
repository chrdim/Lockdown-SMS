package com.unipi.xdimtsasp17027.lockdownsms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class AddHomeActivity extends AppCompatActivity {

    EditText roadEditText,numEditText,cityEditText;
    SQLiteDatabase db;
    private FirebaseAuth mAuth;
    AlertDialog.Builder onBackPressedAlert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_home);

        roadEditText=findViewById(R.id.edit1);
        numEditText=findViewById(R.id.edit3);
        cityEditText=findViewById(R.id.edit2);

        db=openOrCreateDatabase("AppDB", Context.MODE_PRIVATE,null);

        mAuth = FirebaseAuth.getInstance();
    }

    public void save(View view){
        if(!((roadEditText.getText().toString()).equals("") || (numEditText.getText().toString()).equals("")
                || (cityEditText.getText().toString()).equals(""))) {
            if(roadEditText.getText().toString().chars().allMatch(Character::isLetter) &&
                    cityEditText.getText().toString().chars().allMatch(Character::isLetter)){

                db.execSQL("CREATE TABLE IF NOT EXISTS HOME(std_email TEXT,std_address TEXT)");
                StringBuilder address=new StringBuilder();
                address.append(roadEditText.getText().toString()+" ");
                address.append(numEditText.getText().toString()+" ");
                address.append(cityEditText.getText().toString());

                Cursor cursor = db.rawQuery("SELECT * FROM HOME WHERE std_email=?", new String[]{mAuth.getCurrentUser().getEmail()}, null);


                if (cursor.getCount() == 0) {

                    db.execSQL("INSERT INTO HOME VALUES('"+mAuth.getCurrentUser().getEmail()+"','"+address.toString()+"') ");


                }else{
                    db.execSQL("UPDATE HOME SET std_address=? WHERE std_email=?",new String[]{address.toString(),mAuth.getCurrentUser().getEmail()});

                }


                startActivity(new Intent(getApplicationContext(),SmsActivity.class));



            }else{
                Toast.makeText(getApplicationContext(),"Κάποιο πεδίο περιλαμβάνει μη αποδεκτό χαρακτήρα.",Toast.LENGTH_SHORT).show();
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
                        startActivity(new Intent(getApplicationContext(),SmsActivity.class));

                    }
                });

        onBackPressedAlert.create().show();


    }
}