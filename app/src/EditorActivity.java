package be.kuleuven.softdev.haientang.newsclient;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.DatePicker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

public class EditorActivity extends AppCompatActivity {
    private static final int IMAGE_REQUEST_CODE_ONE = 3;
    private static final int IMAGE_REQUEST_CODE_TWO = 4;
    private static final int STORAGE_PERMISSION_CODE = 123;
    private Bitmap bitmap;
    private Uri fileUriUp;
    private Uri fileUriDown;

    EditText titleEdit,tagsEdit, contentEdit;
    Spinner categorySpin;
    TextView dateTv;
    Button submitBut;
    ImageView calenderImg, upImagePic, downImagePic,home;

    int day,month,year;
    Calendar mCurrentDate;
    String date;
    //int cureentMaxId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        initAllRef();

        setDate();
        clickOnHomeIcon();
        requestStoragePermission();
        setUpImage();
        setDownImage();
        clickOnSubmitButton();
    }

    private void initAllRef(){
        titleEdit = (EditText) findViewById(R.id.title);
        categorySpin = (Spinner) findViewById(R.id.categSpinner);
        dateTv = (TextView) findViewById(R.id.date);
        tagsEdit = (EditText) findViewById(R.id.Tags);

        contentEdit = (EditText) findViewById(R.id.firstPart);
        submitBut = (Button) findViewById(R.id.ButSubmit);
        calenderImg=(ImageView) findViewById(R.id.calendImg);

        upImagePic =(ImageView) findViewById(R.id.upImage);
        downImagePic =(ImageView) findViewById(R.id.downImage);
        home=(ImageView) findViewById(R.id.home);

        mCurrentDate=Calendar.getInstance();
        day=mCurrentDate.get(Calendar.DAY_OF_MONTH);
        month=mCurrentDate.get(Calendar.MONTH)+1;
        year=mCurrentDate.get(Calendar.YEAR);
        date=year+"-"+month+'-'+day;
    }

    private void setDate() {
        dateTv.setText(date);//date by default
        calenderImg.setOnClickListener(new View.OnClickListener() {//choose the date from calendar and display it
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog= new DatePickerDialog(EditorActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                        dateTv.setText(y+"-"+(m+1)+"-"+d);
                    }
                },year,month-1,day);
                datePickerDialog.show();
            }
        });
    }

    private void clickOnHomeIcon()
    {
        home.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                Intent intent=new Intent(EditorActivity.this,MainActivity.class);
                startActivity(intent);
            }

        });
    }

    private void setUpImage() {
        upImagePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), IMAGE_REQUEST_CODE_ONE);
            }}
        );
    }

    private void setDownImage() {
        downImagePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), IMAGE_REQUEST_CODE_TWO);
            }}
        );
    }

    private void clickOnSubmitButton() {
        submitBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title=titleEdit.getText().toString();
                String category=categorySpin.getSelectedItem().toString();
                String tags= tagsEdit.getText().toString();
                String cont=contentEdit.getText().toString();
                String url="http://api.a17-sd606.studev.groept.be/postNews/"
                        +title +"/" +cont+"/"+tags+"/"+category+"/"+date;
                uploadNews(url);
            }
        });
    }

    private void uploadNews(String url){
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        uploadImagesByMultipart();
                        Toast.makeText(getApplicationContext(), "Post succeed! To be reviewed by administrator!", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {Toast.makeText(getApplicationContext(), "Oops,please try again later!", Toast.LENGTH_SHORT).show();}
        });
        queue.add(stringRequest);
    }

    /*Follow codes will upload the images to the web server*/

    //ask for permission to local storage
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    //use a library of Multipart to upload image
    public void uploadImagesByMultipart() {
        String UPLOAD_URL = "http://a17-sd606.studev.groept.be/ImageUpload.php";  //the php url
        //getting the actual path of the image
        String pathUp = getPathThroughUri(fileUriUp);
        String pathDown= getPathThroughUri(fileUriDown);
        //Uploading the first picture
        try {
            String uploadId = UUID.randomUUID().toString();
            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, UPLOAD_URL)
                    .addFileToUpload(pathUp, "image") //Adding file
                    .addParameter("isSuccess","success")
                    .addParameter("position","up")
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //Uploading the second picture
        try {
            String uploadId = UUID.randomUUID().toString();
            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, UPLOAD_URL)
                    .addFileToUpload(pathDown, "image") //Adding file
                    .addParameter("isSuccess","success")
                    .addParameter("position","down")
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //fileUriUp is a uri of the first picture on the newsShow layout, fileUriDown is of the second one
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE_ONE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUriUp = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUriUp);
                upImagePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode==IMAGE_REQUEST_CODE_TWO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUriDown = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUriDown);
                downImagePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPathThroughUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
}
