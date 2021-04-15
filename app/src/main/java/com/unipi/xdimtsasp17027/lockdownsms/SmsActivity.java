package com.unipi.xdimtsasp17027.lockdownsms;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SmsActivity extends AppCompatActivity implements LocationListener {

    EditText fullnameEditText, addressEditText;

    ArrayList<View> viewArrayList;

    TextView smsNumber, smsTitle, smsDetails;

    ArrayList<ImageButton> selectButtonList;

    LinearLayout layout;

    ImageButton selectButton;

    ArrayList<String> numberList;


    private FirebaseAuth mAuth;

    SQLiteDatabase db;

    String home;

    Button sendButton;

    String choosenNumber;

    LocationManager locationManager;

    double x, y;
    String location;

    Timestamp timestamp;

    private static final int REC_RESULT = 653;

    Tts tts;

    AlertDialog.Builder speechRecognitionLesson;

    AlertDialog.Builder speechRecognitionWrongRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);



        tts = new Tts(this);

        location = null;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        choosenNumber = "";
        sendButton = findViewById(R.id.button4);

        numberList = new ArrayList<String>();

        mAuth = FirebaseAuth.getInstance();
        fullnameEditText = findViewById(R.id.fullNameEditText);
        addressEditText = findViewById(R.id.homeAddressEditText);

        selectButtonList = new ArrayList<ImageButton>();
        layout = findViewById(R.id.linearLayout2);
        viewArrayList = new ArrayList<View>();


        fullnameEditText.setText(mAuth.getCurrentUser().getDisplayName());

        db = openOrCreateDatabase("AppDB", Context.MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS HOME(std_email TEXT,std_address TEXT)");


        Cursor cursor = db.rawQuery("SELECT * FROM HOME WHERE std_email=?", new String[]{mAuth.getCurrentUser().getEmail()}, null);


        home = "-";

        //ελέγχος για τον αν ο χρήστης έχει αποθηκεύσει την διεύθυνσή του,ώστε να εμφανιστεί στο αντίστοιχο editText
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (!(cursor.getString(1).equals("-"))) {
                    home = cursor.getString(1);

                    addressEditText.setText(home);

                }
            }

        }


        sendButton.setText(sendButton.getText().toString() + ":\n" +
                fullnameEditText.getText().toString() + " " + addressEditText.getText().toString());
        sendButton.setEnabled(false);
        sendButton.setAlpha(0.5f);


        db.execSQL("CREATE TABLE IF NOT EXISTS SMS(std_number TEXT,std_title TEXT,std_details TEXT)");
        cursor = db.rawQuery("SELECT * FROM SMS", null);


        //εμφάνιση των μηνυμάτων της βάσης
        if (cursor.getCount() > 0) {


            //για κάθε μήνυμα δημιουργείται ένα view τύπου row_show_sms_option.
            //Το row_show_sms_option είναι ένα layout που έχουμε δημιουργήσει βάση του ο οποίου θα εμφανίζεται κάθε επιλογή μηνύματος της βάσης
            while (cursor.moveToNext()){
                View v = getLayoutInflater().inflate(R.layout.row_show_sms_option, null, false);


                smsNumber = (TextView) v.findViewById(R.id.numberTextView10);
                smsTitle = (TextView) v.findViewById(R.id.titleTextView10);
                smsDetails = (TextView) v.findViewById(R.id.detailsTextView10);
                selectButton = (ImageButton) v.findViewById(R.id.imageButton);

                //αποθηκεύουμε το view στην λίστα viewArrayList
                viewArrayList.add(v);

                //θέτουμε τις τιμές στα αντίστοιχα textView
                smsNumber.setText(cursor.getString(0));
                smsTitle.setText(cursor.getString(1));
                smsDetails.setText(cursor.getString(2));

                //αποθηκεύουμε το κουμπί selectButton του view στην λίστα selectButtonList
                selectButtonList.add(selectButton);
                //αποθηκεύουμε το νούμερο του μηνύματος στην λίστα numberList
                numberList.add(smsNumber.getText().toString());


            }

        }

        //όλα τα views που έχουμε στην viewArrayList τα προσθέτουμε στο layout ώστε να τα εμφανίσουμε στο scrollview
        for (int i = 0; i < viewArrayList.size(); i++) {
            layout.addView(viewArrayList.get(i));

            ImageButton button = selectButtonList.get(i);
            String number = numberList.get(i);

            //ορίζουμε τις ενέργειες που γίνονται όταν ο χρήστης επιλέγει κάποιο view δηλαδή ποιο μήνυμα επιθυμεί να στείλει
            viewArrayList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    choosenNumber = number;
                    //O αριθμός του μηνύματος προστίθενται στο κείμενο του sendButton
                    sendButton.setText("Αποστολή στο 13033:\n" + number + " " + fullnameEditText.getText().toString() + " " + addressEditText.getText().toString());
                    //αν με την εισαγωγή του αριθμού στο sendButton το κείμενο του sendButton έχει πάρει την μορφή που χρειάζεται για να θεωρείται
                    //έγκυρο το μήνυμα το sendButton ενεργοποιείται
                    if (!(fullnameEditText.getText().toString().equals("") || addressEditText.getText().toString().equals(""))) {
                        sendButton.setEnabled(true);
                        sendButton.setAlpha(1f);

                    }
                    //το κουμπί του view που επιλέχθηκε παίρνει το αντίστοιχο εικονίδιο που υποδικνύει ότι έχει επιλεχθεί
                    button.setImageResource(R.drawable.greenicon);
                    for (ImageButton b : selectButtonList) {
                        if (b != button) {
                            b.setImageResource(R.drawable.redicon);
                        }
                    }
                }
            });
        }


        //Κάθε αλλαγή του fullnameEditText και addressEditText αποτυπώνεται και στο κείμενο του sendButton και για κάθε αλλαγή ελέγχεται
        //αν το κείμενο του sendButton βρίσκεται σε έγκυρη μορφή ώστε να ενεργοποιηθεί ή να απανεργοποιηθεί
        fullnameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {



                sendButton.setText("Αποστολή στο 13033:\n" + choosenNumber + " " + fullnameEditText.getText().toString() + " " + addressEditText.getText().toString());

                if (!(fullnameEditText.getText().toString().equals("") || addressEditText.getText().toString().equals("") ||
                        choosenNumber.equals(""))) {
                    sendButton.setEnabled(true);
                    sendButton.setAlpha(1f);
                } else {
                    sendButton.setEnabled(false);
                    sendButton.setAlpha(0.5f);
                }
            }
        });

        addressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {


                sendButton.setText("Αποστολή στο 13033:\n" + choosenNumber + " " + fullnameEditText.getText().toString() + " " + addressEditText.getText().toString());

                if (!(fullnameEditText.getText().toString().equals("") || addressEditText.getText().toString().equals("") ||
                        choosenNumber.equals(""))) {
                    sendButton.setEnabled(true);
                    sendButton.setAlpha(1f);
                } else {
                    sendButton.setEnabled(false);
                    sendButton.setAlpha(0.5f);
                }
            }
        });


        //αυτό το μήνυμα θα εμφανίζεται κάθε φορά που ο χρήστης πατά το μικρόφωνο ώστε να τον ενημερώνει για τις εντολές που υποστηρίζει
        //η φωνητική αναγνώριση
        speechRecognitionLesson = new AlertDialog.Builder(this);
        speechRecognitionLesson.setMessage("'one'->Αλλαγή κωδικού\n'two'->Προφίλ χρήστη\n'three'->Διαγραφή διεύθυνσης\n'four'->Προσθήκη διεύθυνσης\n'five'->Προσθήκη νέας επιλογής sms\n'six'->Έλεγχος επιλογών sms\n'seven'->Αποσύνδεση");
        speechRecognitionLesson.setTitle("Εντολές που υποστηρίζονται");
        speechRecognitionLesson.setCancelable(true);
        speechRecognitionLesson.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλώ δώστε μία εντολή");

                        startActivityForResult(intent, REC_RESULT);


                    }
                });
        speechRecognitionLesson.setNegativeButton("Άκυρο", null);



        //αυτό το μήνυμα θα εμφανίζεται κάθε φορά που ο χρήστης θα δίνει μία φωνητική εντολή η οποία δεν υποστηρίζεται.
        speechRecognitionWrongRequest = new AlertDialog.Builder(this);
        speechRecognitionWrongRequest.setMessage("Η εντολή που δώσατε δεν υποστηρίζεται");
        speechRecognitionWrongRequest.setCancelable(true);
        speechRecognitionWrongRequest.setPositiveButton("Προσπαθείστε ξανά", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλώ δώστε μία εντολή");

                startActivityForResult(intent, REC_RESULT);
            }
        });
        speechRecognitionWrongRequest.setNegativeButton("Άκυρο", null);





    }

    public void goToProfileActivity(View view) {

        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

    }

    public void goTosmsOptions(View view) {
        startActivity(new Intent(getApplicationContext(), SmsOptionsActivity.class));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();


    }


    public void saveLoc() {


        location = null;
        //έλεγχος για permission προσδιορισμού τοποθεσίας
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);


        }else{
            //σε περίπτωση που ο χρήστης έχει δώσει άδεια
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


            if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                x = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
                y = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
                location = x + "," + y;
            }

            timestamp = Timestamp.valueOf(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date((System.currentTimeMillis()))));
            writeIntoFirebaseDatabase();
        }


    }

    public void writeIntoFirebaseDatabase() {



        String userID = mAuth.getCurrentUser().getUid();


        //δημιουργία HashMap και εισαγωγή τοποθεσίας και timestamp
        HashMap<String, Object> newPost = new HashMap();
        if (location != null) {
            newPost.put("location", location);
        } else {
            newPost.put("location", "null");
        }

        newPost.put("timestamp", timestamp.toString());


        //Το HashMap αποθηκεύεται ως node-παιδί στο αντίστοιχο node του χρήστη βάση του userID
        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).push().setValue(newPost);


    }


    public void sendSms(View view) {



        //έλεγχος για permission αποστολής μηνύματος
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        } else {
            saveLoc();

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("13033", null, choosenNumber + " " + fullnameEditText.getText().toString() + " " +
                    addressEditText.getText().toString(), null, null);


            Toast.makeText(getApplicationContext(), "Το μήνυμα εστάλη επιτυχώς", Toast.LENGTH_LONG).show();
        }


    }

    public void microphoneClicked(View view) {

        speechRecognitionLesson.create().show();
    }


    @Override
    public void onLocationChanged(Location location) {


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {


    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REC_RESULT && resultCode == Activity.RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.contains("1")) {
                startActivity(new Intent(getApplicationContext(), PasswordChangeActvity.class));

            } else if (matches.contains("2")) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            } else if (matches.contains("3")) {

                if (getAddressFromDatabase().equals("-")) {
                    Toast.makeText(getApplicationContext(), "Δεν υπάρχει αποθηκευμένη διεύθυνση", Toast.LENGTH_SHORT).show();
                } else {
                    db.execSQL("UPDATE HOME SET std_address=? WHERE std_email=?", new String[]{"-", mAuth.getCurrentUser().getEmail()});
                    Toast.makeText(getApplicationContext(), "Η διεύθυνση σας διαγράφτηκε επιτυχώς", Toast.LENGTH_SHORT).show();
                }
            } else if (matches.contains("4")) {
                if (getAddressFromDatabase().equals("-")) {
                    startActivity(new Intent(getApplicationContext(), AddHomeActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Έχετε ήδη αποθηκεύσει την διεύθυνση σας", Toast.LENGTH_SHORT).show();
                }

            } else if (matches.contains("5")) {
                startActivity(new Intent(getApplicationContext(), AddSmsOption.class).putExtra("fromActivity","smsActivity"));
            } else if (matches.contains("6")) {
                startActivity(new Intent(getApplicationContext(), SmsOptionsActivity.class));
            } else if (matches.contains("7")) {
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            } else {
                speechRecognitionWrongRequest.create().show();
            }
        }
    }

    public String getAddressFromDatabase() {

        db.execSQL("CREATE TABLE IF NOT EXISTS HOME(std_email TEXT,std_address TEXT)");

        Cursor cursor = db.rawQuery("SELECT * FROM HOME WHERE std_email=?", new String[]{mAuth.getCurrentUser().getEmail()}, null);

        String home = "-";

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (!(cursor.getString(1).equals("-"))) {
                    home = cursor.getString(1);

                }
            }

        }
        return home;
    }

    //κάνουμε override αυτήν την μέθοδο ώστε όταν ο χρήστης δώσει άδεια για την αποστολή μηνύματος και προσδιορισμού τοποθεσίας να πραγματοποιηθούν
    //αμέσως όλες οι διαδικασίες ππου θα γινόταν αν η εφαρμογή είχε εξ αρχής άδεια
    //διαφορετικά ο χρήστης θα έπρεπε να δώσει εντολή αποστολής μηνύματος,έπειτα να δώσει άδεια και μετά να ξαναδώσει εντολή
    //και αντίστοιχα η εφαρμογή θα αποθήκευε την τοποθεσία του χρήστη έπειτα από την φορά που έδωσε άδεια προσδιορισμού τοποθεσίας
    //θα έχανε δηλαδή την τοποθεσία του χρήστη την φορά που έδωσε άδεια
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                //αφού δώσει ο χρήστης άδεια αποστολής μηνύματος ακολουθούνται οι  ενέργειες αποστολής μηνύματος
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    saveLoc();

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("13033", null, choosenNumber + " " + fullnameEditText.getText().toString() + " " +
                            addressEditText.getText().toString(), null, null);


                    Toast.makeText(getApplicationContext(), "Το μήνυμα εστάλη επιτυχώς", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Δεν θα μπορέσετε να στείλετε μήνυμα μέσω της εφαρμογής χωρίς να δώσετε την άδεια σας!", Toast.LENGTH_SHORT).show();

                }
                break;
            }
            case 2: {
                //αφού δώσει άδεια προσδιορισμού τοποθεσίας
                if (grantResults.length > 0) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


                    if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                        x = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
                        y = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
                        location = x + "," + y;
                    }

                    timestamp = Timestamp.valueOf(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date((System.currentTimeMillis()))));
                    writeIntoFirebaseDatabase();
                }
                //αφού αρνηθεί άδεια προσδιορισμού τοποθεσίας
                else{
                    location=null;
                    timestamp = Timestamp.valueOf(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date((System.currentTimeMillis()))));
                    writeIntoFirebaseDatabase();
                }

                break;
            }

        }
    }



}