package weknownothing.p2_hp.soluchan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.entity.UrlEncodedFormEntityHC4;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button upload,choose;
    private EditText name;
    private ImageView imgView;
    private final int IMG_REQUEST = 1;
    private Bitmap bitmap;
    private String Uploadurl = "http://192.168.43.193/image.php" ;
    private String Uploadurl1 = "https://851a4e16.ngrok.io/testing/" ;
    private String Uploadurl2 = "https://c07d97c1.ngrok.io/" ;

    private static final String TAG = "MainActivity";
    private AndroidTextToSpeechActivity androidTextToSpeechActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        upload = (Button)findViewById(R.id.upload);
        choose = (Button)findViewById(R.id.choose);
        name = (EditText)findViewById(R.id.name);
        imgView=(ImageView)findViewById(R.id.imageView);

        upload.setOnClickListener(this);
        choose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.upload:
                uploadImage();
                //upload();
                break;
            case R.id.choose:
                selectImage();
                break;
        }
    }

    private void upload(){
        new UploadImage(bitmap,"image").execute();
    }
    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMG_REQUEST);
    }

    private void uploadImage()
    {
        String url = String.format(Uploadurl2,"/hi");
        Log.d(TAG, "**********uploadImage: "+ImagetoString(bitmap).length());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Uploadurl2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        name.setText(response);
                        androidTextToSpeechActivity.speakOut(response);
                        Log.d(TAG, "onResponse: "+response);
                        Log.d(TAG, "onResponse: in");
                        /*JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String res = null;
                        try {
                            res = jsonObject.getString("response");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: error here"+error.toString());
            }

        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("name",name.getText().toString().trim());
                params.put("image",ImagetoString(bitmap));

                return params;
            }
           // stringRequest.setRetryPolicy(new DefaultRetryPolicy(
             //   MY_SOCKET_TIMEOUT_MS,
               // DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                // DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        };

        MySingelton.getInstance(MainActivity.this).addToRequestQue(stringRequest);

    }
    private String ImagetoString(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes,Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMG_REQUEST && resultCode==RESULT_OK && data!=null){

            Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),path);
                imgView.setImageBitmap(bitmap);
                imgView.setVisibility(View.VISIBLE);
                name.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public class UploadImage extends AsyncTask<Void,Void,Void>{
        Bitmap image;
        String name;


        public UploadImage(Bitmap image,String name) {
            this.image=image;
            this.name = name;

        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: in");
            String encodedimage = ImagetoString(bitmap);
            ArrayList<NameValuePair> dataToSend  = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image",encodedimage));
            dataToSend.add(new BasicNameValuePair("name",name));

            HttpParams httpRequestParams=getHttpRequestParams();
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(Uploadurl1);

            try{
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            }catch(Exception e){
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: uploaded************");
        }
    }

    private HttpParams getHttpRequestParams(){
        HttpParams httpRequestParams=new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpRequestParams,1000*30);
        HttpConnectionParams.setSoTimeout(httpRequestParams,1000*30);
        return httpRequestParams;

    }
}
