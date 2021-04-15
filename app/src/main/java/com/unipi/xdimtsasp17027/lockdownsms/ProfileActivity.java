package com.unipi.xdimtsasp17027.lockdownsms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    TextView nameTextView,emailTextView,homeAddressTextView,passwordTextView;
    private FirebaseAuth mAuth;

    EditText passwordEditText,editTextRoad,editTextNumber,editTextCity,editTextNewPassword,editTextValideNewPassword;

    SQLiteDatabase db;

    ImageView addAddressImageView,deleteAddressImageView,addressFinishImageView,addressCloseImageView,passwordFinishImageView,passwordCloseImageView;


    AlertDialog.Builder deleteAddressAlert;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        db=openOrCreateDatabase("AppDB", Context.MODE_PRIVATE,null);

        nameTextView=findViewById(R.id.nameTextView);

        emailTextView=findViewById(R.id.emailTextView);


        passwordTextView=findViewById(R.id.textView5);

        homeAddressTextView=findViewById(R.id.homeAdressTextView);

        editTextRoad=findViewById(R.id.editTextTextPersonNameRoad);
        editTextCity=findViewById(R.id.editTextTextPersonNameCity);
        editTextNumber=findViewById(R.id.editTextNumber);


        editTextRoad.setVisibility(View.INVISIBLE);
        editTextCity.setVisibility(View.INVISIBLE);
        editTextNumber.setVisibility(View.INVISIBLE);

        editTextRoad.setEnabled(false);
        editTextNumber.setEnabled(false);
        editTextCity.setEnabled(false);




        addressFinishImageView=findViewById(R.id.imageView15);
        addressCloseImageView=findViewById(R.id.imageView16);

        addressFinishImageView.setVisibility(View.INVISIBLE);
        addressCloseImageView.setVisibility(View.INVISIBLE);

        addressFinishImageView.setEnabled(false);
        addressCloseImageView.setEnabled(false);




        mAuth = FirebaseAuth.getInstance();


        //παίρνουμε από την firebase το displayName και το email του χρήστη και τα εμφανίζουμε στα αντίστοιχα textView
        nameTextView.setText(mAuth.getCurrentUser().getDisplayName());
        emailTextView.setText(mAuth.getCurrentUser().getEmail());

        //παίρνουμε την διεύθυνση του χρήστη και την εμφανίζουμε στο αντίστοιχο textView
        if(!(getAddressFromDatabase().equals("-"))){
            homeAddressTextView.setText(getAddressFromDatabase());
        }else{
            homeAddressTextView.setText("Δεν υπάρχει αποθηκευμένη διεύθυνση");
        }

        passwordEditText=findViewById(R.id.passwordEditText);

        editTextFormating(passwordEditText);



        //προσδιορίζουμε τις ενέργειες που θα πραγματοποιηθούν αφού ο χρήστης συμπληρώσει το editText του κωδικού
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(),passwordEditText.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        beforeUpdatingPassword();
                                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        mgr.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
                                    }else{
                                        Toast.makeText(getApplicationContext(),"Λάθος κωδικός.Προσπαθείστε ξανά",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
                return false;
            }
        });




        addAddressImageView =findViewById(R.id.addAdressImageView);
        deleteAddressImageView=findViewById(R.id.deleteAdressImageView);

        addAddressImageView.setEnabled(true);



        editTextNewPassword=findViewById(R.id.editTextTextPassword2);
        editTextValideNewPassword=findViewById(R.id.editTextTextPassword);

        editTextNewPassword.setVisibility(View.INVISIBLE);
        editTextValideNewPassword.setVisibility(View.INVISIBLE);

        editTextNewPassword.setEnabled(false);
        editTextValideNewPassword.setEnabled(false);

        editTextFormating(editTextNewPassword);
        editTextFormating(editTextValideNewPassword);


        passwordFinishImageView=findViewById(R.id.imageView17);
        passwordCloseImageView=findViewById(R.id.imageView18);

        passwordFinishImageView.setEnabled(false);
        passwordCloseImageView.setEnabled(false);

        passwordFinishImageView.setVisibility(View.INVISIBLE);
        passwordCloseImageView.setVisibility(View.INVISIBLE);

        //μήνυμα που θα εμφανίζεται όταν ο χρήστης επιλέξει να διαγράψει την διεύθυνσή του
        deleteAddressAlert = new AlertDialog.Builder(this);
        deleteAddressAlert.setMessage("Είστε σίγουροι ότι θέλετε να διαγράψετε την διεύθυνση σας;");
        deleteAddressAlert.setTitle("Μήνυμα");
        deleteAddressAlert.setCancelable(true);
        deleteAddressAlert.setNegativeButton("Ακύρωση", null);
        deleteAddressAlert.setPositiveButton("Διαγραφή",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"Η διεύθυνση σας διαγράφηκε επιτυχώς",Toast.LENGTH_SHORT);
                        db.execSQL("UPDATE HOME SET std_address=? WHERE std_email=?",new String[]{"-",mAuth.getCurrentUser().getEmail()});
                        homeAddressTextView.setText("Δεν υπάρχει αποθηκευμένη διεύθυνση");
                        afterAddorRemoveAddress("");

                    }
                });

        deleteAddressImageView.setEnabled(true);
    }

    public void deleteHome(View view){


        if(getAddressFromDatabase().equals("-")){
            Toast.makeText(getApplicationContext(),"Δεν υπάρχει αποθηκεύμενη διεύθυνση",Toast.LENGTH_SHORT).show();
        }else{
            deleteAddressAlert.create().show();
        }
    }

    public void addAddress (View view){
        if(getAddressFromDatabase().equals("-")){
            beforeAddingAddress();
            passwordEditText.setEnabled(false);
        }else{
            Toast.makeText(getApplicationContext(),"Έχετε ήδη αποθηκεύσει την διεύθυνσή σας",Toast.LENGTH_SHORT).show();
        }
    }

    public void editTextFormating(EditText editText){

        editText.setAlpha(0.5f);

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editText.setAlpha(1f);
                return false;
            }
        });

    }

    public void saveAddress(View view){
        //γίνονται οι έλεγχοι που εξηγήθηκαν και στο MyHomeActivity
        if(!(editTextRoad.getText().toString().equals("")|| editTextNumber.getText().toString().equals("")
                || editTextCity.getText().toString().equals(""))){
            if(editTextCity.getText().toString().chars().allMatch(Character::isLetter) &&
                    editTextRoad.getText().toString().chars().allMatch(Character::isLetter)){

                db.execSQL("CREATE TABLE IF NOT EXISTS HOME(std_email TEXT,std_address TEXT)");
                StringBuilder address=new StringBuilder();
                address.append(editTextRoad.getText().toString()+" ");
                address.append(editTextNumber.getText().toString()+" ");
                address.append(editTextCity.getText().toString());

                Cursor cursor = db.rawQuery("SELECT * FROM HOME WHERE std_email=?", new String[]{mAuth.getCurrentUser().getEmail()}, null);



                if (cursor.getCount() == 0) {

                    db.execSQL("INSERT INTO HOME VALUES('"+emailTextView.getText().toString()+"','"+address.toString()+"') ");


                }else{
                    db.execSQL("UPDATE HOME SET std_address=? WHERE std_email=?",new String[]{address.toString(),mAuth.getCurrentUser().getEmail()});

                }

                Toast.makeText(getApplicationContext(),"Η διεύθυνσή σας αποθηκεύτηκε επιτυχώς.",Toast.LENGTH_SHORT).show();





                afterAddorRemoveAddress(address.toString());





            }else{
                Toast.makeText(getApplicationContext(),"Κάποιο πεδίο περιλαμβάνει μη αποδεκτό χαρακτήρα.",Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(getApplicationContext(),"Θα πρέπει να συμπληρωθούν όλα τα πεδία.",Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelAddingAddress(View view){
        afterAddorRemoveAddress("");
    }

    //η μέθοδος αυτήν περιέχει τις ενέργειες που θα πρέπει να πραγματοποιηθούν όταν ο χρήστης πατήσει το εικονίδιο προσθήκης διεύθυσνης
    public void beforeAddingAddress(){
        editTextFormating(editTextRoad);
        editTextFormating(editTextCity);
        editTextFormating(editTextNumber);

        homeAddressTextView.setVisibility(View.INVISIBLE);

        editTextRoad.setVisibility(View.VISIBLE);
        editTextCity.setVisibility(View.VISIBLE);
        editTextNumber.setVisibility(View.VISIBLE);

        addressFinishImageView.setEnabled(true);
        addressCloseImageView.setEnabled(true);

        editTextRoad.setEnabled(true);
        editTextNumber.setEnabled(true);
        editTextCity.setEnabled(true);

        addressFinishImageView.setVisibility(View.VISIBLE);
        addressCloseImageView.setVisibility(View.VISIBLE);

        addAddressImageView.setVisibility(View.INVISIBLE);
        deleteAddressImageView.setVisibility(View.INVISIBLE);
    }

    //η μέθοδος αυτήν περιέχει τις ενέργειες που θα πρέπει να πραγματοποιηθούν όταν ο χρήστης τελείωσει ή διακόψει την διαδικασία προσθήκης διεύθυνσης
    public void afterAddorRemoveAddress(String address){

        homeAddressTextView.setVisibility(View.VISIBLE);

        if(address.equals("")){
            homeAddressTextView.setText("Δεν υπάρχει αποθηκευμένη διεύθυνση");
        }else{
            homeAddressTextView.setText(address);
        }

        editTextRoad.setVisibility(View.INVISIBLE);
        editTextCity.setVisibility(View.INVISIBLE);
        editTextNumber.setVisibility(View.INVISIBLE);

        editTextRoad.setEnabled(false);
        editTextNumber.setEnabled(false);
        editTextCity.setEnabled(false);

        editTextRoad.setText("");
        editTextCity.setText("");
        editTextNumber.setText("");

        addressFinishImageView.setVisibility(View.INVISIBLE);
        addressCloseImageView.setVisibility(View.INVISIBLE);

        addAddressImageView.setVisibility(View.VISIBLE);
        deleteAddressImageView.setVisibility(View.VISIBLE);

        addressFinishImageView.setEnabled(false);
        addressCloseImageView.setEnabled(false);

        addAddressImageView.setEnabled(true);
        deleteAddressImageView.setEnabled(true);

        editTextRoad.setText("");
        editTextCity.setText("");
        editTextNumber.setText("");

        passwordEditText.setEnabled(true);

    }


    public void updatePassword(View view){

        //γίνονται οι έλεγχοι που εξηγήθηκαν και στο PasswordActivity
        if(!((editTextNewPassword.getText().toString()).equals("") && (editTextValideNewPassword.getText().toString()).equals(""))){
            if(editTextNewPassword.getText().toString().length()>=6){
                if((editTextNewPassword.getText().toString()).equals(editTextValideNewPassword.getText().toString())){

                    mAuth.getCurrentUser().updatePassword(editTextNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            afterUpdatingPassword();
                            Toast.makeText(getApplicationContext(),"Επιτυχής αλλαγή κωδικού",Toast.LENGTH_SHORT).show();

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
    //η μέθοδος αυτήν περιέχει τις ενέργειες που θα πρέπει να πραγματοποιηθούν όταν ο χρήστης βρίσκεται στην διαδιασία αλλαγής κωδικού
    public void beforeUpdatingPassword(){

        passwordTextView.setVisibility(View.INVISIBLE);
        passwordEditText.setVisibility(View.INVISIBLE);
        passwordEditText.setEnabled(false);

        passwordFinishImageView.setVisibility(View.VISIBLE);
        passwordCloseImageView.setVisibility(View.VISIBLE);

        passwordFinishImageView.setEnabled(true);
        passwordCloseImageView.setEnabled(true);

        editTextNewPassword.setVisibility(View.VISIBLE);
        editTextValideNewPassword.setVisibility(View.VISIBLE);

        editTextNewPassword.setEnabled(true);
        editTextValideNewPassword.setEnabled(true);


    }

    //η μέθοδος αυτήν περιέχει τις ενέργειες που θα πρέπει να πραγματοποιηθούν όταν ο χρήστης τελειώσει ή ακυρώσει την διαδιασία αλλαγής κωδικού
    public void afterUpdatingPassword(){

        passwordTextView.setVisibility(View.VISIBLE);
        passwordEditText.setVisibility(View.VISIBLE);
        passwordEditText.setEnabled(true);

        passwordFinishImageView.setVisibility(View.INVISIBLE);
        passwordCloseImageView.setVisibility(View.INVISIBLE);

        passwordFinishImageView.setEnabled(false);
        passwordCloseImageView.setEnabled(false);

        editTextNewPassword.setVisibility(View.INVISIBLE);
        editTextValideNewPassword.setVisibility(View.INVISIBLE);

        editTextNewPassword.setEnabled(false);
        editTextValideNewPassword.setEnabled(false);
        passwordEditText.setText("");

        editTextNewPassword.setText("");
        editTextValideNewPassword.setText("");

    }

    public void cancelChangePassword(View view){
        afterUpdatingPassword();
    }

    public void logout(View view){

        finish();
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));

    }

    public String getAddressFromDatabase() {

        db.execSQL("CREATE TABLE IF NOT EXISTS HOME(std_email TEXT,std_address TEXT)");

        Cursor cursor = db.rawQuery("SELECT * FROM HOME WHERE std_email=?", new String[]{mAuth.getCurrentUser().getEmail()}, null);

        String home ="-";

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (!(cursor.getString(1).equals("-"))) {
                    home = cursor.getString(1);

                }
            }

        }
        return home;
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(getApplicationContext(),SmsActivity.class));
    }
}