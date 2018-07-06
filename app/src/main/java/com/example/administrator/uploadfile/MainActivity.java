package com.example.administrator.uploadfile;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    Button select,upload;
    TextView notify;
    Uri pdfUri;
    ProgressDialog progressDialog;

    FirebaseStorage storage;// used for uploading files
    FirebaseDatabase database;// store url of uploaded files

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage=FirebaseStorage.getInstance();// returns from firebase storage
        database=FirebaseDatabase.getInstance();// returns database from storage


        select=findViewById(R.id.select);
        upload=findViewById(R.id.upload);
        notify=findViewById(R.id.notify);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    selectPdf();
                }
                else
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},3);
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pdfUri!=null)//file is selected
                {
                    uploadFile(pdfUri);
                }
                else
                {
                    Toast.makeText(MainActivity.this,"Select a valid File",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void uploadFile(Uri pdfUri) {

        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading Your File. Please have Patience.");
        progressDialog.setProgress(0);
        progressDialog.show();


        final String filename=System.currentTimeMillis()+"";
        StorageReference storageReference=storage.getReference();//returns path of stored file

        storageReference.child("Uploads").child(filename).putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String url= taskSnapshot.getDownloadUrl().toString();//returns uploaded Url
                //storing Url
                DatabaseReference reference=database.getReference();// returns the path

                reference.child(filename).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            Toast.makeText(MainActivity.this,"File is Successfully uploaded",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(MainActivity.this,"File is not uploaded successfully",Toast.LENGTH_SHORT).show();


                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"File is not uploaded successfully",Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                // track progress of the upload
                int progress= (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(progress);

            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==3 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
            selectPdf();
        }
        else
        {
            Toast.makeText(MainActivity.this, "Please give required permissions",Toast.LENGTH_SHORT).show();
        }

    }

    private void selectPdf() {
        //Selection of File with File manager
        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);// fetch the contents of files
        startActivityForResult(intent,86);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==56 && requestCode==RESULT_OK && data!=null)
        {
            pdfUri=data.getData();//return the uri of selected file
        }
        else{
            Toast.makeText(MainActivity.this, "Please Select the file",Toast.LENGTH_SHORT).show();
        }
    }
}
