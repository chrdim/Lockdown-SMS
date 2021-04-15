package com.unipi.xdimtsasp17027.lockdownsms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences preferences;
    EditText nameEditText,surnameEditText,emailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        nameEditText=findViewById(R.id.nameEditText);
        surnameEditText=findViewById(R.id.surNameEditText);
        emailEditText=findViewById(R.id.emailEditText);

    }

    public void goToHomeActivity(View view){

        //έλεγχος για το αν είναι συμπληρωμένα όλα τα πεδία
        if(!((nameEditText.getText().toString()).equals("") || (surnameEditText.getText().toString()).equals("")
                || (emailEditText.getText().toString()).equals(""))){
            //έλεγχος για το αν το email είναι σε σωστή μορφή,διότι αν δεν είναι θα δημιουργηθούν προβλήματα κατά την εγγραφή του χρήστη στην firebase
            if(Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()){
                SharedPreferences.Editor editor=preferences.edit();
                //το email και το πλήρες όνομα αποθηκεύονται σε αντίστοιχα sharepreferences
                editor.putString("email",emailEditText.getText().toString());
                editor.putString("fullname",surnameEditText.getText().toString()+" "+nameEditText.getText().toString());
                editor.apply();
                startActivity(new Intent(this,MyHomeActivity.class));
            }else{
                Toast.makeText(getApplicationContext(),"Η μορφή του email δεν είναι έγκυρη",Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(getApplicationContext(),"Θα πρέπει να συμπληρωθούν όλα τα πεδία.",Toast.LENGTH_SHORT).show();
        }




    }

}