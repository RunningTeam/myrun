package com.example.myrun;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GamingEnd extends AppCompatActivity {

    private static final String TAG = "GameingEnd";

    public static final int REQUEST_TAKE_PHOTO = 10;
    public static final int REQUEST_PERMISSION = 11;

    private Button btnCamera, btnSave;
    private ImageView ivCapture;
    private String mCurrentPhotoPath;

    Toolbar toolbar;
    private Bitmap bmp;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaming_end);

        Intent receiveIntent = getIntent();
        TextView runResult = findViewById(R.id.runResult);
        if (receiveIntent != null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String[] names = user.getEmail().split("@");
            String name = names[0];

            // 뛰었던 기록들을 String 화
            String result = String.format("User: %s\n\nTime: %s\n\nDistance: %s\n\nKcal: %s", name,
                    receiveIntent.getStringExtra("time"),
                    receiveIntent.getStringExtra("km"),
                    receiveIntent.getStringExtra("kc"));
            runResult.setText(result);
            i = receiveIntent.getIntExtra("key",0);
        }

        toolbar = findViewById(R.id.toolbar_map);
        //상단 툴바 설정
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#55e6c3"))); // 툴바 배경색

        checkPermission();
        //loadImgArr(); 보류, 필수기능 X

        ivCapture = findViewById(R.id.ivCapture);
        btnCamera = findViewById(R.id.btnCapture);
        btnSave = findViewById(R.id.btnSave);


        Button btnGotomain = findViewById(R.id.btnGotomain);
        btnGotomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnCamera.setOnClickListener(v->captureCamera());

        // 저장을 누를시, 비트화 한 사진과 뛴 기록을 intent로 넘김
        btnSave.setOnClickListener(v->{
            try{
                BitmapDrawable drawable = (BitmapDrawable) ivCapture.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                if(bitmap == null){
                    Toast.makeText(this,"저장할 사진이 없습니다.", Toast.LENGTH_SHORT).show();
                }else{
                    saveImg();
                    mCurrentPhotoPath="";
                }
            }catch(Exception e){
                Log.w(TAG, "SAVE ERROR!", e);
            }

            try{
                String filename = "bitmap.png";
                FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
                bmp.recycle();
                Intent in1 = new Intent(this, MainActivity.class);
                in1.putExtra("image", filename);
                in1.putExtra("time" , receiveIntent.getStringExtra("time"));
                in1.putExtra("km" , receiveIntent.getStringExtra("km"));
                in1.putExtra("kc" , receiveIntent.getStringExtra("kc"));
                in1.putExtra("key",i);
                startActivity(in1);
                finish();
            }catch(Exception e){
                e.printStackTrace();
            }

        });

    }

    // 카메라 기능을 실행 후, 임시 파일에 저장해 View할 메서드
   private void captureCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePictureIntent.resolveActivity(getPackageManager())!=null){
            File photoFile = null;

            try{
                File tempDir = getCacheDir();

                String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String imageFilename = "Capture_" + timeStamp + "_";

                File tempImage = File.createTempFile(
                        imageFilename,
                        ".jpg",
                        tempDir
                );

                mCurrentPhotoPath = tempImage.getAbsolutePath();

                photoFile = tempImage;

            }catch(IOException e){
                Log.w(TAG, "파일 생성 에러!",e);
            }
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private void saveImg(){
        try{
            //저장할 파일 경로
            File storageDir = new File(getFilesDir() + "/capture");
            if(!storageDir.exists())
                storageDir.mkdirs();

            String filename = "캡쳐파일" + ".jpg";

            //기존에 있다면 삭제
            File file = new File(storageDir, filename);
            boolean deleted = file.delete();
            Log.w(TAG, "Delete Dup Check :" + deleted);
            FileOutputStream output = null;

            try{
                output = new FileOutputStream(file);
                BitmapDrawable drawable = (BitmapDrawable)ivCapture.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output); // 해상도에 맞춰, Compress
                bmp = bitmap;
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }finally{
                try{
                    assert output != null;
                    output.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

            Log.e(TAG, "Captured Saved");
            Toast.makeText(this, "Capture Saved", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            Log.w(TAG, "Capture Saving Error!", e);
            Toast.makeText(this, "Saved failed", Toast.LENGTH_SHORT).show();
        }
    }

    // 저장 경로에 따라 이미 저장된 파일이 있다면 앱 실행시 이미지 파일을 로드해오는 메서드
    private void loadImgArr(){
        try{
            File storageDir = new File(getFilesDir()+"/capture");
            String filename = "캡쳐파일" + ".jpg";

            File file = new File(storageDir, filename);
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            ivCapture.setImageBitmap(bitmap);
        }catch(Exception e){
            Log.w(TAG, "Capture loading Error!", e);
            Toast.makeText(this, "load failed", Toast.LENGTH_SHORT).show();
        }
    }

    //startActivityForResult로 요청받은 request 내용을 받아오는 메서드
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        try{
            switch(requestCode){
                case REQUEST_TAKE_PHOTO:{

                    if(resultCode == RESULT_OK){
                        File file =  new File(mCurrentPhotoPath);
                        Bitmap bitmap = MediaStore.Images.Media
                                .getBitmap(getContentResolver(), Uri.fromFile(file));
                        if(bitmap != null){
                            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

                            Bitmap rotatedBitmap = null;
                            switch(orientation){

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotateImage(bitmap, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(bitmap, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(bitmap, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    rotatedBitmap = bitmap;
                            }

                            ivCapture.setImageBitmap(rotatedBitmap);
                        }
                    }
                    break;
                }
            }
        }catch(Exception e){
            Log.w(TAG, "onActivityResult Error!", e);
        }
    }
    // 카메라에 맞게 이미지 로테이션
    public static Bitmap rotateImage(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    // 권한을 체크 해줌
    public void onResume(){
        super.onResume();
        checkPermission();
    }
    // 권한이 없으면 확인 후 권한 요청
    public void checkPermission(){
        int permissionCamera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int permissionRead = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permissionCamera != PackageManager.PERMISSION_GRANTED
                ||permissionRead != PackageManager.PERMISSION_GRANTED
                ||permissionWrite != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)){
                Toast.makeText(this,"이 앱을 실행하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);
        }
    }

    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case REQUEST_PERMISSION:{
                if(grantResults.length >0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"권한 확인", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this, "권한 없음", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}