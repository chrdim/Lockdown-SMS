package com.unipi.xdimtsasp17027.lockdownsms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MyHomeActivity extends AppCompatActivity {

    EditText roadEditText,numberEditText,cityEditText;



    SharedPreferences preferences;

    SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_home);


        roadEditText=findViewById(R.id.roadEditText);
        numberEditText=findViewById(R.id.numberEditText);
        cityEditText=findViewById(R.id.cityEditText);

        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        editor=preferences.edit();

    }

    public void goToPasswordActivity(View view){


        //έλεγχος για το αν είναι συμπληρωμένα όλα τα πεδία
        if(!((roadEditText.getText().toString()).equals("") || (numberEditText.getText().toString()).equals("")
                || (cityEditText.getText().toString()).equals(""))) {
            //έλεγχος για το αν τα πεδία 'οδός' και 'πόλη' περιέχουν μόνο γράμματα
            if(roadEditText.getText().toString().chars().allMatch(Character::isLetter) &&
                    cityEditText.getText().toString().chars().allMatch(Character::isLetter)){

                //η οδός,η πόλη και το νούμερο ενώνονται σε ένα string
                StringBuilder address=new StringBuilder();
                address.append(roadEditText.getText().toString()+" ");
                address.append(numberEditText.getText().toString()+" ");
                address.append(cityEditText.getText().toString());

                //το string της διεύθυνσης αποθηκεύεται σε sharepreference
                editor.putString("address",address.toString());
                editor.apply();

                startActivity(new Intent(this,PasswordActivity.class));
            }else{
                Toast.makeText(getApplicationContext(),"Κάποιο πεδίο περιλαμβάνει μη αποδεκτό χαρακτήρα.",Toast.LENGTH_SHORT).show();
            }


        }else{
            Toast.makeText(getApplicationContext(),"Θα πρέπει να συμπληρωθούν όλα τα πεδία.",Toast.LENGTH_SHORT).show();
        }

    }

    public void goToPasswordActivityWithoutHome(View view){

        //αν ο χρήστης επιλέξει να 'παράλειψη' στο sharepreference της διεύθυνσης αποθηκεύεται ο χαρακτήρας '-'
        editor.putString("address","-");
        editor.apply();
        startActivity(new Intent(this,PasswordActivity.class));




    }
}