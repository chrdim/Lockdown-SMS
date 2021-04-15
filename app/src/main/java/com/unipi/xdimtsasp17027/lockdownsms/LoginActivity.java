package com.unipi.xdimtsasp17027.lockdownsms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    EditText passwordEditText,emailEditText;

    SharedPreferences preferences;

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        passwordEditText=findViewById(R.id.passEditText);
        emailEditText=findViewById(R.id.emailAddressEditText);

        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        db=openOrCreateDatabase("AppDB", Context.MODE_PRIVATE,null);
    }

    public void goToRegisterActivity(View view){
        startActivity(new Intent(this,RegisterActivity.class));
    }

    public void signin(View view){

        //έλεγχος για το αν είναι συμπληρωμένα όλα τα πεδία
        if(!(emailEditText.getText().toString().equals("") || (passwordEditText.getText().toString()).equals(""))){

            //σύνδεση χρήστη με email και password
            mAuth.signInWithEmailAndPassword(emailEditText.getText().toString(),passwordEditText.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                SharedPreferences.Editor editor=preferences.edit();

                                //αποθηκεύεται σε sharepreference το email
                                editor.putString("email",emailEditText.getText().toString());

                                //έλεγχος αν ο χρήστης έχει αποθηκευμένη την διεύθυνση κατοικίας του στην τοπική βάση
                                //αυτο γίνεται διότι μπορεί κάποιος χρήστης να έχει δημιουργήσει λογαριασμό από άλλη συσκευή και τα στοιχεία
                                //της τοπικής βάσης όπως η διεύθυνση σπιτιού δεν υπάρχουν στην νέα συσκευή
                                checkIfHomeIsInDatabase();



                                //έλεγχος για τον αν υπάρχει η βάση με τα μηνύματα
                                //αυτό γίνεται με τον ίδιο λόγο που εξηγήθηκε πιο πάνω
                                checkifThereIsSMSTABLE();

                                startActivity(new Intent(getApplicationContext(),SmsActivity.class));



                            }
                            //έλεγχος για το αν το email είναι σε σωστή μορφή
                            else if(!(Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches())){
                                Toast.makeText(getApplicationContext(),"Η μορφή του email δεν είναι έγκυρη",Toast.LENGTH_SHORT).show();
                            }
                            //έλεγχος για το αν υπάρχει χρήστης με τα στοιχεία που εισήχθησαν
                            else{
                                Toast.makeText(getApplicationContext(),"Τα παραπάνω στοιχεία δεν αντιστοιχούν σε κάποιον χρήστη.\n" +
                                        "Αν δεν έχετε λογαρισμό δημιουργήστε έναν πατώντας 'ΔΗΜΙΟΥΡΓΙΑ ΝΕΟΥ ΛΟΓΑΡΙΑΣΜΟΥ'",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }else{
            Toast.makeText(getApplicationContext(),"Θα πρέπει να συμπληρωθούν όλα τα πεδία.",Toast.LENGTH_SHORT).show();

        }

    }

    public void checkIfHomeIsInDatabase(){


        db.execSQL("CREATE TABLE IF NOT EXISTS HOME(std_email TEXT,std_address TEXT)");

        Cursor cursor = db.rawQuery("SELECT * FROM HOME WHERE std_email=?", new String[]{mAuth.getCurrentUser().getEmail()}, null);


        //αν cursor.getCount==0 σημαίνει ότι ο πίνακας δεν υπήρχε οπότε τον δημιουργούμε και τον γεμίζουμε.Στο home βάζουμε -
        if (cursor.getCount() == 0) {

            db.execSQL("INSERT INTO HOME VALUES('"+mAuth.getCurrentUser().getEmail()+"','"+"-"+"') ");
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //σε περίπτωση που ο χρήστης πατήσει το κουμπί 'πίσω'  βγαίνει από την εφαρμογή
        finishAffinity();


    }

    public void checkifThereIsSMSTABLE(){


        db.execSQL("CREATE TABLE IF NOT EXISTS SMS(std_number TEXT,std_title TEXT,std_details TEXT)");

        Cursor cursor = db.rawQuery("SELECT * FROM SMS", null);


        //αν cursor.getCount==0 σημαίνει ότι ο πίνακας δεν υπήρχε οπότε τον δημιουργούμε και τον γεμίζουμε
        if (cursor.getCount() == 0) {



            db.execSQL("INSERT INTO SMS VALUES('"+"1"+"','"+"ΓΙΑΤΡΟΣ-ΦΑΡΜΑΚΕΙΟ"+"','"+"Μετάβαση σε φαρμακείο ή επίσκεψη στον γιατρό, εφόσον αυτό συνιστάται μετά από σχετική επικοινωνία. ΙΣΧΥΕΙ ΚΑΙ ΜΕΤΑ ΤΙΣ 21:00" +"')");
            db.execSQL("INSERT INTO SMS VALUES('"+"2"+"','"+"ΣΟΥΠΕΡ ΜΑΡΚΕΤ"+"','"+"Μετάβαση σε εν λειτουργία κατάστημα προμηθειών αγαθών πρώτης ανάγκης (σούπερ μάρκετ, μίνι μάρκετ), όπου δεν είναι δυνατή η αποστολή τους. ΔΕΝ ΙΣΧΥΕΙ ΜΕΤΑ ΤΙΣ 21:00" +"')");
            db.execSQL("INSERT INTO SMS VALUES('"+"3"+"','"+"ΤΡΑΠΕΖΑ-ΔΗΜΟΣΙΑ ΥΠΗΡΕΣΙΑ"+"','"+"Μετάβαση σε δημόσια υπηρεσία ή τράπεζα, στο μέτρο που δεν είναι δυνατή η ηλεκτρονική συναλλαγή. ΔΕΝ ΙΣΧΥΕΙ ΜΕΤΑ ΤΙΣ 21:00" +"')");
            db.execSQL("INSERT INTO SMS VALUES('"+"4"+"','"+"ΠΑΡΟΧΗ ΒΟΗΘΕΙΑΣ"+"','"+"Κίνηση για παροχή βοήθειας σε ανθρώπους που βρίσκονται σε ανάγκη ή συνοδεία ανηλίκων μαθητών από/προς το σχολείο. ΔΕΝ ΙΣΧΥΕΙ ΜΕΤΑ ΤΙΣ 21:00" +"')");
            db.execSQL("INSERT INTO SMS VALUES('"+"5"+"','"+"ΕΠΙΚΟΙΝΩΝΙΑ ΔΙΑΖΕΥΗΜΕΝΩΝ ΓΟΝΕΩΝ-\nΤΕΛΕΤΕΣ"+"','"+"Μετάβαση σε τελετή κηδείας υπό τους όρους που προβλέπει ο νόμος ή μετάβαση διαζευγμένων γονέων ή γονέων που τελούν σε διάσταση που είναι αναγκαία για τη διασφάλιση της επικοινωνίας γονέων και τέκνων, σύμφωνα με τις κείμενες διατάξεις. ΔΕΝ ΙΣΧΥΕΙ ΜΕΤΑ ΤΙΣ 21:00" +"')");
            db.execSQL("INSERT INTO SMS VALUES('"+"6"+"','"+"ΑΘΛΗΣΗ-ΚΑΤΟΙΚΙΔΙΑ"+"','"+"Σωματική άσκηση σε εξωτερικό χώρο ατομικά ή ανά δύο άτομα, τηρώντας στην τελευταία αυτή περίπτωση την αναγκαία απόσταση 1,5 μέτρου.. ΔΕΝ ΙΣΧΥΕΙ ΜΕΤΑ ΤΙΣ 21:00\nΚίνηση με κατοικίδιο ζώο, ατομικά ή ανά δύο άτομα, τηρώντας στην τελευταία αυτή περίπτωση την αναγκαία απόσταση 1,5 μέτρου. ΙΣΧΥΕΙ ΚΑΙ ΜΕΤΑ ΤΙΣ 21:00" +"')");
        }

    }
}