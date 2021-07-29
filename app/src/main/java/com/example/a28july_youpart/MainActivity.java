 package com.example.a28july_youpart;

 import androidx.activity.result.ActivityResult;
 import androidx.activity.result.ActivityResultCallback;
 import androidx.activity.result.ActivityResultLauncher;
 import androidx.activity.result.contract.ActivityResultContracts;
 import androidx.annotation.NonNull;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.core.app.ActivityCompat;
 import androidx.core.content.ContextCompat;

 import android.Manifest;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.MediaController;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import android.widget.VideoView;

 import org.jetbrains.annotations.NotNull;

 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;

 import okhttp3.MediaType;
 import okhttp3.MultipartBody;
 import okhttp3.RequestBody;
 import retrofit2.Call;
 import retrofit2.Callback;
 import retrofit2.Response;

 public class MainActivity extends AppCompatActivity {
     private VideoView mVideoView;
     private Button mBtnOpenGallery;
     private Button mBtnUploadImage;
     private String videoPath;

     private ActivityResultLauncher<Intent> launchGallery = registerForActivityResult(
             new ActivityResultContracts.StartActivityForResult(), result -> {

                 if (result.getData() !=null){

                     Uri selectedVideo = result.getData().getData();
                     mVideoView.setVideoURI(selectedVideo);
                     mVideoView.setMediaController(new MediaController(MainActivity.this));
                     getVideoPathFromUri(selectedVideo);

                 }
             }
     );

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         initViews();
     }

     private void initViews() {
         mVideoView = findViewById(R.id.videoView);
         mBtnOpenGallery = findViewById(R.id.btnGallery);
         mBtnUploadImage = findViewById(R.id.btnUpload);
         mBtnOpenGallery.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (ifpermissionGranted()) {
                     openGallery();
                 } else {
                     ActivityCompat.requestPermissions(MainActivity.this, new String[]
                             {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                 }

             }
         });

         mBtnUploadImage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 uploadImage();
             }
         });
     }

     private void uploadImage() {
         ProgressDialog progressDialog = new ProgressDialog(this);
         progressDialog.setMessage("Uploading the Video");
         progressDialog.show();

         ApiService apiService = Network.getInstance().create(ApiService.class);
         File file = new File(videoPath); //convert image path to file
         RequestBody reqFile = RequestBody.create(MediaType.parse("video/*"), file);
         MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("video", file.getName(), reqFile);
         apiService.uploadVideo(multipartBody, "amol").enqueue(
                 new Callback<ResponseModel>() {
                     @Override
                     public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                         progressDialog.dismiss();
                         Toast.makeText(MainActivity.this, "Sucess", Toast.LENGTH_SHORT).show();

                     }

                     @Override
                     public void onFailure(Call<ResponseModel> call, Throwable t) {
                         progressDialog.dismiss();
                         Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show();

                     }
                 }
         );
     }

     private boolean ifpermissionGranted() {
         return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
     }

     private void openGallery() {
         Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.
                 Video.Media.EXTERNAL_CONTENT_URI);
         launchGallery.launch(intent);
     }


     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         if (requestCode == 1) {
             if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 openGallery();
             } else {
                 Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
             }
         }
     }

     @NotNull
     private void getVideoPathFromUri(Uri selectedImage) {
         String[] filePath = {MediaStore.Images.Media.DATA};
         Cursor c = getContentResolver().query(selectedImage, filePath,
                 null, null, null);
         c.moveToFirst();
         int columnIndex = c.getColumnIndex(filePath[0]);
         videoPath = c.getString(columnIndex);
     }
 }