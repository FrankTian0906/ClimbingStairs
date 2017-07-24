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

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.IOException;
import android.os.Message;

public class MainActivity extends AppCompatActivity {
    protected static final int ERROR = 2;
    protected static final int SUCCESS = 1;

    private EditText account;
    private EditText password;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SUCCESS:
                    Toast.makeText(MainActivity.this,(String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;

                case ERROR:
                    Toast.makeText(MainActivity.this,"Fail!",Toast.LENGTH_SHORT).show();
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
        Button login = (Button)findViewById(R.id.logButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("RUN!--------------------");
                login(v);
               //Intent intent = new Intent();
                //intent.setClass(MainActivity.this, TestActivity.class);
                //startActivity(intent);
            }
        });

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

    public void login(View view){
        final String acc = account.getText().toString();
        final String pwd = password.getText().toString();

        if(TextUtils.isEmpty(acc) || TextUtils.isEmpty(pwd)){
            Toast.makeText(this, "Account or password is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(){
            public void run() {
                try {
                    String path = "http://192.168.0.12/login.php";
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0(compatible;MSIE 9.0;Windows NT 6.1;Trident/5.0)");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    String data = "username= " + acc + "&password = " + pwd + "&button=";
                    conn.setRequestProperty("Content-Length", data.length()+"");
                    conn.setDoOutput(true);
                    byte[] bytes = data.getBytes();
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();
                    System.out.println("RESPONSE CODE：--------------------" + code);
                    if (code == 200) {
                        InputStream is = conn.getInputStream();
                        String result = StreamTools.readStream(is);
                        Message mas = Message.obtain();
                        mas.what = SUCCESS;
                        mas.obj = result;
                        handler.sendMessage(mas);
                    }
                    else {
                        Message mas = Message.obtain();
                        mas.what = ERROR;
                        handler.sendMessage(mas);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Message mas = Message.obtain();
                    mas.what = ERROR;
                    handler.sendMessage(mas);
                }
            }
        }.start();
    }
}
