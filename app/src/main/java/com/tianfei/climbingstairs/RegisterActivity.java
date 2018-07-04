package com.tianfei.climbingstairs;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {
    protected static final int ERROR = 2;
    protected static final int SUCCESS = 1;
    protected static final String path = "http://192.168.137.1/register.php";
    private EditText account,password, conpassword, height,weight,name,age;
    private RadioGroup gender;
    private String genderVal = "M";
    private String notice;
    private Button register;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SUCCESS:
                    Toast.makeText(RegisterActivity.this,(String)msg.obj, Toast.LENGTH_SHORT).show();
                    String re = (String)msg.obj;
                    if(re.equals("success!"))
                        RegisterActivity.this.finish();
                    break;

                case ERROR:
                    Toast.makeText(RegisterActivity.this,"Fail!",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initVIew();
        initListener();
    }

    private void initVIew(){
        account = (EditText)findViewById(R.id.RegAccount);
        password = (EditText)findViewById(R.id.RegPassword);
        conpassword = (EditText)findViewById(R.id.RegConfirmPassword);
        height = (EditText)findViewById(R.id.RegHeight);
        weight = (EditText)findViewById(R.id.RegWeight);
        name = (EditText)findViewById(R.id.RegName);
        age = (EditText)findViewById(R.id.RegAge);
        gender = (RadioGroup)findViewById(R.id.GenderGroup);
        gender.check(R.id.Male);
        register = (Button)findViewById(R.id.addUser);
    }

    private void initListener(){
        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                int radioID = group.getCheckedRadioButtonId();
                if(radioID == R.id.Female)
                    genderVal = "F";
                else
                    genderVal = "M";
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewUser(v);
            }
        });
    }

    public void addNewUser(View v){
        final String accountVal = account.getText().toString();
        final String passwordVal = password.getText().toString();
        final String conPasswordVal = conpassword.getText().toString();
        final String nameVal = name.getText().toString();
        final String heightVal = height.getText().toString();
        final String weightVal = weight.getText().toString();
        final String genderV = genderVal;
        final String ageV = age.getText().toString();

        if(TextUtils.isEmpty(accountVal) || TextUtils.isEmpty(passwordVal) || TextUtils.isEmpty(conPasswordVal)){
            Toast.makeText(this, "Account or password is empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(!passwordVal.matches(conPasswordVal)){
            Toast.makeText(this,"Please confirm the password!",Toast.LENGTH_SHORT).show();
            return;
        }
        else if(50>Integer.parseInt(heightVal) || Integer.parseInt(heightVal)>300) {
            Toast.makeText(this, "Please confirm the password!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(50>Integer.parseInt(weightVal) || Integer.parseInt(weightVal)>400) {
            Toast.makeText(this, "Please confirm the password!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(1>Integer.parseInt(ageV) || Integer.parseInt(ageV)>130) {
            Toast.makeText(this, "Please confirm the password!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(){
            @Override
            public void run() {
                try {
                    //String path = "http://10.20.142.158/register.php";
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("account", accountVal);
                    params.put("password", passwordVal);
                    params.put("name",nameVal);
                    params.put("height",heightVal);
                    params.put("weight",weightVal);
                    params.put("gender", genderV);
                    params.put("age", ageV);
                    String response = HttpRequestUtil.PostRequest(path,params,null);
                    notice = response;
                    System.out.println(notice);
                    Message mas = new Message();
                    mas.what = SUCCESS;
                    mas.obj = notice;
                    handler.sendMessage(mas);

                }catch (IOException e) {
                    // TODO Auto-generated catch block
                    Message mas = Message.obtain();
                    mas.what = ERROR;
                    handler.sendMessage(mas);
                    return;
                }
            }
        }.start();

    }
}
