package com.mytrendin.firebaseimageupload;


        import android.Manifest;
        import android.app.ProgressDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.graphics.Bitmap;
        import android.net.Uri;
        import android.os.Environment;
        import android.provider.MediaStore;
        import android.support.annotation.NonNull;
        import android.support.design.widget.Snackbar;
        import android.support.v4.app.ActivityCompat;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.util.SparseArray;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.firebase.ui.auth.AuthUI;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;
        import com.google.firebase.storage.FirebaseStorage;
        import com.google.firebase.storage.StorageReference;
        import com.google.firebase.storage.UploadTask;

        import java.io.File;
        import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    TextView button;
    ImageView imageView;
    StorageReference myrefernce;
    FirebaseDatabase database; //DB
    DatabaseReference myRef; //DB
    Parametrii_baza_de_date parametri; //DB
    Button send_button; //DB
    EditText text_numele; //DB
    EditText text_sesizarea;

    private static final String LOG_TAG = "Barcode Scanner API";
    private static final int PHOTO_REQUEST = 10;
    private TextView scan;
    private Uri imageuri;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), PHOTO_REQUEST);
        } else {
            Snackbar.make(getWindow().getDecorView().getRootView(), "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();
            //Load content
            launchMediaScanIntent();
        }

        text_numele = (EditText)findViewById(R.id.numele); //DB
        database=FirebaseDatabase.getInstance(); //DB
        myRef=database.getReference("--Sesizari"); //DB
        parametri = new Parametrii_baza_de_date(); //DB
        send_button = (Button) findViewById(R.id.trimite); //DB
        button = (TextView) findViewById(R.id.button);
        imageView = (ImageView) findViewById(R.id.imgview);
        scan = (TextView) findViewById(R.id.txtContent);
        text_sesizarea = (EditText) findViewById(R.id.casuta_sesizarea);


        myrefernce = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(MainActivity.this);

        if (savedInstanceState!=null){
            imageuri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
            scan.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
        }

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parametri.setNumele(text_numele.getText().toString());
                parametri.setURL("gs://uploadfirebasefromcamera.appspot.com/photos/picture341.jpg");
                parametri.setSesizarea(text_sesizarea.getText().toString());
                myRef.child((text_numele.getText().toString())).setValue(parametri);
                Toast.makeText(MainActivity.this, "Se trimite", Toast.LENGTH_LONG).show();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan.setText("");
                //imageView.setImageBitmap(null);
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_PERMISSION);
            }
        });
    }

    public void Send(View v){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        googleMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!"+requestCode, Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            launchMediaScanIntent();
            try {
                Scanner scanner = new Scanner();
                final Bitmap bitmap = scanner.decodeBitmapUri(MainActivity.this, imageuri);
                progressDialog.setTitle("Uploading..");
                progressDialog.show();
                StorageReference filepath = myrefernce.child("photos").child(imageuri.getLastPathSegment());
                filepath.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageView.setImageBitmap(bitmap);
                        Toast.makeText(MainActivity.this,"uploaded",Toast.LENGTH_LONG).show();
                        scan.setText("Image just uploaded on Firebase");
                        progressDialog.dismiss();
                    }
                });

            } catch (Exception e) {
                Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
                        .show();
                Log.e(LOG_TAG, e.toString());
            }
        }
    }

    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Random random = new Random();
        int key =random.nextInt(1000);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture"+key+".jpg");
        //File photo = new File(getCacheDir(), "picture.jpg");
        imageuri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageuri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageuri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, scan.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageuri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void confirmSignOut(View view) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Sunteti sigur ca vreti sa dati Sign Out?")
                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AuthUI.getInstance().signOut(MainActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Snackbar.make(getWindow().getDecorView().getRootView(), "You have been signed out.", Snackbar.LENGTH_SHORT).show();
                                finish();
                            }
                        });

                    }
                })
                .setNegativeButton("Nu", null).show();
    }

}
