package com.unipi.xdimtsasp17027.lockdownsms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordChangeActvity extends AppCompatActivity {

    EditText oldPassEditText,newPassEditText,validateNewPassEditText;
    private FirebaseAuth mAuth;
    Button saveButton;

    AlertDialog.Builder onBackPressedAlert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change_actvity);

        oldPassEditText=findViewById(R.id.passwordEditText52);
        newPassEditText=findViewById(R.id.passwordEditText50);
        validateNewPassEditText=findViewById(R.id.passwordVerificationEditText51);

        newPassEditText.setVisibility(View.INVISIBLE);
        validateNewPassEditText.setVisibility(View.INVISIBLE);

        newPassEditText.setEnabled(false);
        validateNewPassEditText.setEnabled(false);

        oldPassEditText.setEnabled(true);
        oldPassEditText.setVisibility(View.VISIBLE);

        saveButton=findViewById(R.id.button100);

        saveButton.setEnabled(false);
        saveButton.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();

        //προσδιορίζουμε τις ενέργειες που θα πραγματοποιηθούν αφού ο χρήστης συμπληρώσει το editText του κωδικού
        oldPassEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(),oldPassEditText.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        newPassEditText.setVisibility(View.VISIBLE);
                                        validateNewPassEditText.setVisibility(View.VISIBLE);

                                        newPassEditText.setEnabled(true);
                                        validateNewPassEditText.setEnabled(true);

                                        oldPassEditText.setEnabled(false);
                                        oldPassEditText.setVisibility(View.INVISIBLE);

                                        saveButton.setEnabled(true);
                                        saveButton.setVisibility(View.VISIBLE);
                                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        mgr.hideSoftInputFromWindow(newPassEditText.getWindowToken(), 0);
                                    }else{
                                        Toast.makeText(getApplicationContext(),"Λάθος κωδικός.Προσπαθείστε ξανά",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
                return false;
            }
        });
    }

    public void save(View view){
        if(!((newPassEditText.getText().toString()).equals("") && (validateNewPassEditText.getText().toString()).equals(""))){
            if(newPassEditText.getText().toString().length()>=6){
                if((newPassEditText.getText().toString()).equals(validateNewPassEditText.getText().toString())){

                    mAuth.getCurrentUser().updatePassword(newPassEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            startActivity(new Intent(getApplicationContext(),SmsActivity.class));

                        }
                    });



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