package com.example.imagepostroom;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.imagepostroom.AppDatabase;
import com.example.imagepostroom.FileUtils;
import com.example.imagepostroom.RetrofitAPI;
import com.example.imagepostroom.RetrofitClient;
import com.example.imagepostroom.UploadResponse;
import com.example.imagepostroom.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadImageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final String TAG = "UploadImageActivity";
    private Uri imageUri;

    private RetrofitAPI retrofitAPI;
    private AppDatabase appDatabase;

    private EditText nameEditText;
    private EditText ageEditText;
    private Button uploadButton;
    private Button selectImageButton;
    private Button deleteImageButton;
    private Button viewDataButton;
    private ImageView selectedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        retrofitAPI = RetrofitClient.getRetrofitAPI();
        appDatabase = AppDatabase.getInstance(this);

        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        uploadButton = findViewById(R.id.uploadButton);
        selectImageButton = findViewById(R.id.selectImageButton);
        deleteImageButton = findViewById(R.id.deleteImageButton);
        viewDataButton = findViewById(R.id.viewDataButton);
        selectedImageView = findViewById(R.id.selectedImageView);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectedImage();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String age = ageEditText.getText().toString();

                if (imageUri != null && !name.isEmpty() && !age.isEmpty()) {
                    uploadData(imageUri, name, age);
                } else {
                    Toast.makeText(UploadImageActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAndLogStoredData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            selectedImageView.setImageURI(imageUri);
        }
    }

    private void uploadData(Uri fileUri, String name, String age) {
        MultipartBody.Part body = FileUtils.prepareFilePart("image", fileUri, this);
        if (body == null) {
            Toast.makeText(this, "File conversion error", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody namePart = FileUtils.createPartFromString(name);
        RequestBody agePart = FileUtils.createPartFromString(age);

        Call<UploadResponse> call = retrofitAPI.uploadData(body, namePart, agePart);
        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if (response.isSuccessful()) {
                    UploadResponse uploadResponse = response.body();
                    if (uploadResponse != null) {
                        Log.d(TAG, "Upload Success");
                        Log.d(TAG, "Form data: " + uploadResponse.getForm().toString());
                        Log.d(TAG, "File data: " + uploadResponse.getFiles().toString());
                        storeDataInDatabase(name, Integer.parseInt(age), fileUri.getPath());
                    } else {
                        Log.d(TAG, "Response was successful but body was null.");
                    }
                } else {
                    Log.d(TAG, "Upload failed with response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Log.e(TAG, "Upload error: " + t.getMessage());
            }
        });
    }

    private void storeDataInDatabase(String name, int age, String imagePath) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setImagePath(imagePath);

        new Thread(() -> {
            appDatabase.userDao().insert(user);
            runOnUiThread(() -> {
                Toast.makeText(UploadImageActivity.this, "Data saved locally", Toast.LENGTH_SHORT).show();
                // Clear input fields and image view
                clearInputs();
            });
        }).start();
    }

    private void fetchAndLogStoredData() {
        new Thread(() -> {
            List<User> users = appDatabase.userDao().getAllUsers();
            for (User user : users) {
                Log.d(TAG, "Stored User: " + user.getName() + ", Age: " + user.getAge() + ", ImagePath: " + user.getImagePath());
            }
        }).start();
    }

    private void clearInputs() {
        nameEditText.setText("");
        ageEditText.setText("");
        selectedImageView.setImageURI(null); // or set a placeholder image
        imageUri = null;
    }

    private void deleteSelectedImage() {
        selectedImageView.setImageURI(null);
        imageUri = null;
    }
}
