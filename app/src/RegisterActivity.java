package be.kuleuven.softdev.haientang.newsclient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private EditText firstNameTxt,surnameTxt,emailTxt,passwd,rePasswd;
    private TextView multiText;
    private Button submitBut;
    private String URL;
    private ImageView profilePic;
    private static final String UPLOAD_URL="http://a17-sd606.studev.groept.be/user_portrait.php";
    private static final int IMAGE_REQUEST_CODE=1;
    private static final int STORAGE_PERMISSION_CODE=123;
    private Bitmap bitmap;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initAllRef();

        //upload a client`s portrait
        requestStoragePermission();
        clickUploadProfilePic();
        emailDynamicCheck();
        passwdDynamicCheck();
        clickSubmitButton();
    }

    private void initAllRef() {
        firstNameTxt = (EditText) findViewById(R.id.firstName);
        surnameTxt = (EditText) findViewById(R.id.surname);
        emailTxt = (EditText) findViewById(R.id.email);
        passwd = (EditText) findViewById(R.id.passwd);
        rePasswd = (EditText) findViewById(R.id.repeatPasswd);
        multiText=(TextView) findViewById(R.id.multiline);
        submitBut = (Button) findViewById(R.id.butSubmit);
        profilePic =(ImageView) findViewById(R.id.uploadImage) ;
    }

    private void clickUploadProfilePic() {
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), IMAGE_REQUEST_CODE);
            }
        });
    }

    private void emailDynamicCheck() {
        emailTxt.addTextChangedListener(new TextWatcher() {//haien.tang@student.kuleuven.be or r0650137@kuleuven.be
            String reg="^[a-zA-Z0-9]+[-|_|.]?[a-zA-Z0-9]+[@]{1}[a-zA-Z0-9]+[.]{1}[a-zA-Z]+[.]?[a-zA-Z]+";//"+" means [1,infinite] times
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) {
                boolean bool= Pattern.matches(reg,emailTxt.getText().toString());
                if(bool)
                    emailTxt.setTextColor(Color.BLACK);
                else
                    emailTxt.setTextColor(Color.RED);
            }
        });
    }

    private void passwdDynamicCheck() {
        passwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                String password=passwd.getText().toString();
                boolean isUpper = Pattern.matches("^[A-Z].+",password);//start with uppercase
                boolean isDigitNoSpace = Pattern.matches(".+[0-9]+.+",password);//contains digit(s) and no space
                boolean isLength = (password.length()>7&&password.length()<17);//8<=lenght<=16
                if(!isUpper||!isDigitNoSpace||!isLength) {
                    multiText.setTextColor(Color.RED);
                }else
                    multiText.setTextColor(Color.BLACK);
            }
        });
    }

    private void clickSubmitButton() {
        submitBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //1. check empty fields
                if(firstNameTxt.getText().toString().isEmpty()
                        ||surnameTxt.getText().toString().isEmpty()
                        ||emailTxt.getText().toString().isEmpty()
                        ||passwd.getText().toString().isEmpty()
                        ||rePasswd.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please fill in any empty fields!", Toast.LENGTH_SHORT).show();
                }
                //2.check email format in java
                else if (emailTxt.getCurrentTextColor()!= Color.BLACK){
                    Toast.makeText(getApplicationContext(), "Please enter valid email!", Toast.LENGTH_SHORT).show();
                }
                //3.check passwd format
                else if (multiText.getCurrentTextColor()!=Color.BLACK){
                    Toast.makeText(getApplicationContext(),"Please enter valid password", Toast.LENGTH_SHORT).show();
                }
                //4.check passwd consistence
                else if(!passwd.getText().toString().equals(rePasswd.getText().toString())){
                    Toast.makeText(getApplicationContext(),"Your password doesn't match!",Toast.LENGTH_SHORT).show();
                }
                //5.check emails duplication in database
                else
                    checkEmailDuplication();
            }
        });
    }

    private void checkEmailDuplication(){
        String url="http://api.a17-sd606.studev.groept.be/checkEmailDuplication/"+emailTxt.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jArr=new JSONArray(response);
                            if(jArr.length()!=0){//email already existed
                                Toast.makeText(getApplicationContext(), "Email already existed!", Toast.LENGTH_SHORT).show();
                            }else if(jArr.length()==0){////email not existed
                                uploadUserInfo();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RegisterActivity.this, "Oops,please try again later!", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }

    //upload the user`s register information including profile picture
    public void uploadUserInfo() {
        String url="http://api.a17-sd606.studev.groept.be/usersRegister/";
        URL=url+firstNameTxt.getText().toString()
                +"/"+surnameTxt.getText().toString()+
                "/"+emailTxt.getText().toString()
                +"/"+passwd.getText().toString();//2 refers to teh userType registered user, 0 refers to guest
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(RegisterActivity.this, "Registration succeed!", Toast.LENGTH_SHORT).show();
                        switchToLogin();//new method to switch to login dialog
                        uploadMultipart();//this method will upload the image

                    }
                }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(RegisterActivity.this, "Oops,please try again later!", Toast.LENGTH_SHORT).show();
                }
            });
        queue.add(stringRequest);// Add the request to the RequestQueue.


    }

    public void switchToLogin() {
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(this);//create alert dialog
        View mView=getLayoutInflater().inflate(R.layout.dialog_login,null);//referencing the alert dialog to the login dialog
        //define the view inside the login layout
        final EditText mEmail=(EditText) mView.findViewById(R.id.etEmail);
        final EditText mpasswd=(EditText) mView.findViewById(R.id.etPasswd);
        mEmail.setText(emailTxt.getText());
        Button mLogin=(Button) mView.findViewById(R.id.butLogin);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mEmail.getText().toString().isEmpty()&&!mpasswd.getText().toString().isEmpty())
                {
                    LoginCheck(mEmail.getText().toString(),mpasswd.getText().toString());
                }else{
                    Toast.makeText(RegisterActivity.this, "Please fill in any empty fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mBuilder.setView(mView);
        AlertDialog dialog=mBuilder.create();
        dialog.show();
    }

    public void LoginCheck(final String emailCheck, String passwdCheck) {
        String url="http://api.a17-sd606.studev.groept.be/loginCheck/"+emailCheck+"/"+passwdCheck;

        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jArr=new JSONArray(response);
                            if(jArr.length()==0){//email or passwd wrong
                                Toast.makeText(getApplicationContext(), "Please enter correct Email or password!", Toast.LENGTH_SHORT).show();
                            }else if(jArr.length()==1){
                                JSONObject jo = jArr.getJSONObject(0);
                                Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(getApplicationContext(),NewsOverviewActivity.class);
                                int userID = jo.getInt("userID");
                                intent.putExtra("userID", userID);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Oops,please try again later!", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }

    /*Following codes responsible for image uploading*/

    // ask for permission to local storage
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
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    //when receiving an image_request_code, get the uri of the picture selected
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK  && data.getData() != null) {
            fileUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                profilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadMultipart() {
        //getting the actual path of the image
        String path = getPathThroughUri(fileUri);
        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();
            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, UPLOAD_URL)
                    .addFileToUpload(path, "image") //Adding file
                    .addParameter("isSuccess","success")
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
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
