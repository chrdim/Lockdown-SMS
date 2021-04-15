package com.unipi.xdimtsasp17027.lockdownsms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ConfirmActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    SharedPreferences preferences;
    SQLiteDatabase db;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        db=openOrCreateDatabase("AppDB", Context.MODE_PRIVATE,null);

        db.execSQL("CREATE TABLE IF NOT EXISTS HOME(std_email TEXT,std_address TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS SMS(std_number TEXT,std_title TEXT,std_details TEXT)");


        mAuth = FirebaseAuth.getInstance();

        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


    }

    public void signup(View view){


        //εγγραφή χρήστη με email και password,τα οποία είναι αποθηκευμένα στα αντίστοιχα sharepreferences απο τα προηγούμενα αντίστοιχα activities
        mAuth.createUserWithEmailAndPassword(preferences.getString("email",""),
                preferences.getString("password",""))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //θέτουμε ως displayName στον χρήστη το πλήρες όνομα του το οποίο στο registerActivity το έχουμε αποθηκεύσει σε sharepreference
                            mAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(preferences.getString("fullname","")).build());
                            //αποθηκεύουμε την διεύθυνση κατοικίας στην αντίστοιχη τοπική βάση.Η διεύθυνση υπάρχει σε sharepreference που δημιουργήθηκε
                            //στο MyHomeActivity
                            db.execSQL("INSERT INTO HOME VALUES('"+mAuth.getCurrentUser().getEmail()+"','"+preferences.getString("address","-")+"') ");

                            //έλεγχος για τον αν υπάρχει η βάση με τα μηνύματα
                            //εξηγήθηκε στο loginActivity
                            checkifThereIsSMSTABLE();

                            //συνδέουμε τον χρήστη που δημιουργήθηκε
                            mAuth.signInWithEmailAndPassword(preferences.getString("email",""),
                                    preferences.getString("password","")).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    //τον μεταφέρουμε στο επόμενο Activity
                                    startActivity(new Intent(getApplicationContext(),SmsActivity.class));
                                }
                            });

                        }else{
                            Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    public void checkifThereIsSMSTABLE(){


        db.execSQL("CREATE TABLE IF NOT EXISTS SMS(std_number TEXT,std_title TEXT,std_details TEXT)");

        Cursor cursor = db.rawQuery("SELECT * FROM SMS", null);


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