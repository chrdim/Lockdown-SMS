package com.unipi.xdimtsasp17027.lockdownsms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordActivity extends AppCompatActivity {

    EditText passwordEditText,passwordVerificationEditText;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        passwordEditText=findViewById(R.id.passwordEditText1);
        passwordVerificationEditText=findViewById(R.id.passwordVerificationEditText1);

        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public void goToConfirmActivity(View view){
        //έλεγχος για το αν είναι συμπληρωμένα όλα τα πεδία
        if(!((passwordEditText.getText().toString()).equals("") || (passwordVerificationEditText.getText().toString()).equals(""))){
            //έλεγχος για το αν ο κωδικός περιέχει τουλάχιστον 6 ψηφία διότι διαφορετικά θα δημιουργηθεί πρόβλημα στην εγγραφή του χρήστη στην firebase
            if(passwordEditText.getText().toString().length()>=6){
                //έλεγχος για τον αν ο κωδικός και η επαλήθευση είναι ίδιοι
                if((passwordEditText.getText().toString()).equals(passwordVerificationEditText.getText().toString())){
                    //αποθηκεύεται σε sharepreference ο κωδικός
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putString("password",passwordEditText.getText().toString());
                    editor.apply();
                    startActivity(new Intent(this,ConfirmActivity.class));
                }else{
                    Toast.makeText(getApplicationContext(),"Οι κωδικοί δεν ταιριάζουν.",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(),"Ο κωδικός θα πρέπει να περιέχει τουλάχιστον 6 χαρακτήρες.",Toast.LENGTH_SHORT).show();
            }



        }else{
            Toast.makeText(getApplicationContext(),"Θα πρέπει να συμπληρωθούν όλα τα πεδία.",Toast.LENGTH_SHORT).show();
        }

    }
}