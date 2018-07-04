package com.tianfei.climbingstairs;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    protected static final int ERROR = 2;
    protected static final int SUCCESS = 1;
    protected static final String path = "http://192.168.137.1/login.php";

    //private Message message;
    private EditText account;
    private EditText password;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SUCCESS:
                    String[] result =(String[])msg.obj;

                    for(int i = 0;i< result.length;i++){
                        System.out.println(i + " : "+result[i]);
                    }
                    if(null!=result[1]){

                        if(result[1].equals("true")) {
                            System.out.println("Welcome page!");
                            Toast.makeText(MainActivity.this,"Welcome " + result[5], Toast.LENGTH_SHORT).show();
                            System.out.println("param push!");
                            Intent intent = new Intent(MainActivity.this, UserActivity.class)
                                   .putExtra("username", result[0])
                                    .putExtra("name",result[5])
                                    .putExtra("weight", result[2])
                                   .putExtra("height",result[3])
                                   .putExtra("age",result[4])
                                   .putExtra("gender",result[6]);
                            startActivity(intent);
                            MainActivity.this.finish();
                        }

                        if(result[1].equals("false"))
                            Toast.makeText(MainActivity.this,"Sorry, didn't find the account!", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case ERROR:
                    Toast.makeText(MainActivity.this,"Parse failed! Please check the network.",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        account = (EditText)findViewById(R.id.account);
        password = (EditText)findViewById(R.id.password);

        //login button

        Button login = (Button)findViewById(R.id.logButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(v);
                //Intent intent = new Intent(MainActivity.this, UserActivity.class);
                //intent.setClass((MainActivity.this, UserActivity.class);
                //startActivity(intent);
            }

        });

        //register button

        Button register = (Button)findViewById(R.id.toRegister);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    //TODO **login method**

    public void login(View view){
        final String acc = account.getText().toString();
        final String pwd = password.getText().toString();
        //final boolean success;
        // are the text empty
        if(TextUtils.isEmpty(acc) || TextUtils.isEmpty(pwd)){
            Toast.makeText(this, "Account or password is empty!", Toast.LENGTH_SHORT).show();
            return ;
        }

        //connect to the server

        new Thread(){
            public void run() {
                try {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username", acc);
                    params.put("password", pwd);
                    String response = HttpRequestUtil.PostRequest(path,params,null);

                    //parse json data
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        System.out.println(jsonObj);
                        String success = jsonObj.getString("Sresult");
                        String username = jsonObj.getString("Susername");
                        String height = jsonObj.getString("Sheight");
                        String weight = jsonObj.getString("Sweight");
                        String age = jsonObj.getString("Sage");
                        String name = jsonObj.getString("Sname");
                        String gender = jsonObj.getString("Sgender");

                        // push the data from thread to handler
                        Message mas = new Message();
                        mas.what = SUCCESS;//successfully connect
                        String[] result =new String[7];
                        result[0] = username;
                        result[1] = success;
                        result[2] = height;
                        result[3] = weight;
                        result[4] = age;
                        result[5] = name;
                        result[6] = gender;

                        mas.obj = result;//参数
                        handler.sendMessage(mas);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    System.out.println(response);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    //fail to connect
                    Message mas = Message.obtain();
                    mas.what = ERROR;
                    handler.sendMessage(mas);
                }
            }

        }.start();

    }

}
