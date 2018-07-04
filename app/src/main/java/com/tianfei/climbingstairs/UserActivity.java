package com.tianfei.climbingstairs;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Color;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.content.Context;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.EventLogTags;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONObject;




public class UserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected static final int DAY_MODEL = 1;
    protected static final int WEEK_MODEL = 2;
    protected static final int MONTH_MODEL = 3;
    protected static final String path_DAY = "http://192.168.137.1/day.php";
    protected static final String path_WEEK = "http://192.168.137.1/week.php";
    protected static final String path_MONTH = "http:/192.168.137.1/month.php";
    protected static final String path_DATA = "http:/192.168.137.1/data.php";
    //model 1--day; 2--week; 3--month;
    private int model;

    //line chart
    private LineChart lineChart;
    private ArrayList<LineDataSet> lineDataSets;

    //timer
    private Chronometer timerView;
    private Button startButton;

    //sensor
    private TextView mAltitude;
    private Sensor mPressure;
    private SensorManager sensorManager;
    private SensorEventListener pressureListener;
    private double height;
    private double firstPre = 0.0, secondPre;
    private double result;
    private double finalOutput = 0.0;
    private final Timer timer = new Timer();
    private TimerTask task;

    //date
    private Calendar calendar;
    private Calendar calendarMon;
    private Calendar calendarYear;
    private TextView dateTextView;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat dateView = new SimpleDateFormat("y/M/d");

    private Button dayButton;
    private Button weekButton;
    private Button monthButton;
    private Button leftButton;
    private Button rightButton;

    private String username;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private AlertDialog.Builder alertDialogBuilder;

    //menu
    private MenuItem nickname;
    private MenuItem userGender;
    private MenuItem userAge;
    private MenuItem userHeight;
    private MenuItem userWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initView();
        initData();
        initListener();
        dayButton.performClick();

    }

    /**
     * 初始化控件
     */
    private void initView() {
        //TODO main content
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        lineChart = (LineChart) findViewById(R.id.linechart);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        startButton = (Button) findViewById(R.id.buttonStart);
        timerView = (Chronometer) findViewById(R.id.countdownTimer);
        dayButton = (Button) findViewById(R.id.buttonDaily);
        weekButton = (Button) findViewById(R.id.buttonWeekly);
        monthButton = (Button) findViewById(R.id.buttonMonthly);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        leftButton = (Button) findViewById(R.id.leftButton);
        rightButton = (Button) findViewById(R.id.rightButton);
        mAltitude = (TextView) findViewById(R.id.hightValueView);

        //TODO navigation menu
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu m = navigationView.getMenu();
        nickname = m.findItem(R.id.userNickname1);
        userGender = m.findItem(R.id.userGender);
        userAge = m.findItem(R.id.userAge);
        userHeight = m.findItem(R.id.userHeight);
        userWeight = m.findItem(R.id.userWeight);

        //TODO sensor
        sensorManager = null;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (mPressure == null) {
            Toast.makeText(this, "No pressure sensor!", Toast.LENGTH_LONG);
            return;
        }

        model = DAY_MODEL;
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
           // System.out.println("-----------account:" + username);
        }

        alertDialogBuilder=new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Finish!");
        alertDialogBuilder.setIcon(R.drawable.ic_menu_manage);
    }

    /**
     * 初始化工具、数据结构等内容
     */
    private void initData() {
        setSupportActionBar(toolbar);


        //TODO **set menu data**
        nickname.setTitle("Name: " + getIntent().getStringExtra("name"));
        if (getIntent().getStringExtra("gender").equals("F"))
            userGender.setTitle("Gender: Female");
        else
            userGender.setTitle("Gender: Male");
        userAge.setTitle("Age: " + getIntent().getStringExtra("age"));
        userHeight.setTitle("Height: " + getIntent().getStringExtra("height") + " cm");
        userWeight.setTitle("Weight: " + getIntent().getStringExtra("weight") + " lbs");

        //TODO INIT **LINE CHART**
        lineDataSets = new ArrayList<>();

        lineChart.setNoDataText("NO DATA!");//没有数据时显示的文字
        lineChart.setDrawGridBackground(true);//chart 绘图区后面的背景矩形将绘制
        lineChart.setBackgroundColor(Color.LTGRAY);
        lineChart.setTouchEnabled(true); // 设置是否可以触摸
        lineChart.setDragEnabled(true);// 是否可以拖拽
        lineChart.setScaleEnabled(false);// 是否可以缩放 x和y轴, 默认是true
        lineChart.setScaleXEnabled(true); //是否可以缩放 仅x轴
        lineChart.setScaleYEnabled(true); //是否可以缩放 仅y轴
        lineChart.setPinchZoom(true);  //设置x轴和y轴能否同时缩放。默认是否
        lineChart.setDoubleTapToZoomEnabled(true);//设置是否可以通过双击屏幕放大图表。默认是true
        lineChart.setHighlightPerDragEnabled(true);//能否拖拽高亮线(数据点与坐标的提示线)，默认是true
        lineChart.setDragDecelerationEnabled(true);//拖拽滚动时，手放开是否会持续滚动，默认是true（false是拖到哪是哪，true拖拽之后还会有缓冲）
        lineChart.setDragDecelerationFrictionCoef(0.99f);//与上面那个属性配合，持续滚动时的速度快慢，[0,1) 0代表立即停止。
        lineChart.animateY(2000);
        //X axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);//设置轴启用或禁用 如果禁用以下的设置全部不生效
        xAxis.setDrawAxisLine(true);//是否绘制轴线
        xAxis.setDrawGridLines(true);//设置x轴上每个点对应的线
        xAxis.setDrawLabels(true);//绘制标签  指x轴上的对应数值
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x轴的显示位置
        //Y axis
        YAxis rightAxis = lineChart.getAxisRight(); //设置图表右边的y轴禁用
        rightAxis.setEnabled(false); //获取左边的轴线
        YAxis leftAxis = lineChart.getAxisLeft();   //设置网格线为虚线效果
        leftAxis.enableGridDashedLine(10f, 10f, 0f);


        //TODO **CALENDAR**
        //current day
        calendar = Calendar.getInstance();
        System.out.println(calendar);
        //current year
        calendarYear = Calendar.getInstance();
        // current first day of week
        calendarMon = Calendar.getInstance();
        calendarMon.setFirstDayOfWeek(Calendar.MONDAY);
        calendarMon.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

    }

    /**
     * 各种监听器绑定
     */
    private void initListener() {
        //TODO
        navigationView.setNavigationItemSelectedListener(this);
        //TODO
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //TODO START
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButton.getText().equals("Start")) {

                    startButton.setText("Stop");
                    startButton.setTextColor(Color.YELLOW);
                    timerView.setBase(SystemClock.elapsedRealtime());
                    timerView.start();

                    finalOutput = 0.0;
                    task = new TimerTask() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = 1;
                            sensorHandler.sendMessage(message);
                        }
                    };
                    timer.schedule(task, 1000, 10000);
                } else {
                    startButton.setText("Start");
                    startButton.setTextColor(Color.WHITE);
                    timerView.stop();
                    task.cancel();

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username", username);
                    params.put("data", ((int)finalOutput + ""));
                    //确定地址并上传参数
                    requestData(path_DATA, params);

                    alertDialogBuilder.setMessage("You spend " + timerView.getText() + ", and raise up " + (int)finalOutput + "m.");
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });

        //TODO DAY BUTTON
        dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show the date on the screen
                model = DAY_MODEL;
                dateTextView.setText(dateView.format(calendar.getTime()));
                //组织参数
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("date", simpleDateFormat.format(calendar.getTime()));
                System.out.println("DATE: " +  simpleDateFormat.format(calendar.getTime()));
                //确定地址并上传参数
                requestData(path_DAY, params);
                //requestData("http://192.168.0.16/day.php", params);
            }
        });

        weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model = WEEK_MODEL;
                Calendar temp = Calendar.getInstance();
                temp.set(calendarMon.get(Calendar.YEAR), calendarMon.get(Calendar.MONTH), calendarMon.get(Calendar.DATE));
                temp.add(Calendar.DAY_OF_WEEK, 6);
                dateTextView.setText(dateView.format(calendarMon.getTime()) + "-" + dateView.format(temp.getTime()));
                //组织参数
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("date", simpleDateFormat.format(calendarMon.getTime()));

                //确定地址并上传参数
                requestData(path_WEEK, params);
            }
        });

        monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model = MONTH_MODEL;
                dateTextView.setText(calendarYear.get(Calendar.YEAR) + "");
                //组织参数
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("date", (calendarYear.get(Calendar.YEAR) + ""));

                //确定地址并上传参数
                requestData(path_MONTH, params);
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (model) {
                    case DAY_MODEL:
                        calendar.add(Calendar.DAY_OF_WEEK, -1);
                        dayButton.performClick();
                        break;
                    case WEEK_MODEL:
                        calendarMon.add(Calendar.DAY_OF_WEEK, -7);
                        weekButton.performClick();
                        break;
                    case MONTH_MODEL:
                        calendarYear.add(Calendar.YEAR, -1);
                        monthButton.performClick();
                        break;
                }
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (model) {
                    case DAY_MODEL:
                        calendar.add(Calendar.DAY_OF_WEEK, 1);
                        dayButton.performClick();
                        break;
                    case WEEK_MODEL:
                        calendarMon.add(Calendar.DAY_OF_WEEK, 7);
                        weekButton.performClick();
                        break;
                    case MONTH_MODEL:
                        calendarYear.add(Calendar.YEAR, 1);
                        monthButton.performClick();
                        break;
                }
            }
        });

        //TODO SENSOR
        pressureListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float sPV = event.values[0];
                //mPressureVal.setText(String.valueOf(sPV));
                DecimalFormat df = new DecimalFormat("0.00");
                df.getRoundingMode();
                //altitude
                height = 44330000 * (1 - (Math.pow((Double.parseDouble(df.format(sPV)) / 1013.25),
                        (float) 1.0 / 5255.0)));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(pressureListener, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //----------------网络请求部分--------------------------
    private static final String REQUEST_DATA_PATH = "REQ_PATH";//数据标识：路径
    private static final String REQUEST_DATA_DATA = "REQ_DATA";//数据标识：内容

    /**
     * 通用网络请求方法封装
     *
     * @param path   请求的接口地址
     * @param params 上传参数
     */
    private void requestData(final String path, final Map<String, String> params) {
        new Thread() {
            public void run() {
                try {
                    String response = HttpRequestUtil.PostRequest(path, params, null);

                    if (response != null && !response.isEmpty()) {
                        //请求后不在通用方法里做任何处理，传给Handler处理
                        Message mas = new Message();
                        //把数据通过bundle传递给handler
                        Bundle bundle = new Bundle();
                        bundle.putString(REQUEST_DATA_PATH, path);
                        bundle.putString(REQUEST_DATA_DATA, response);
                        mas.setData(bundle);
                        //请求成功，发送结果
                        mas.what = SUCCESS;
                        handler.sendMessage(mas);
                        return;
                    }
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

    //----------------接收网络请求结果部分--------------------------
    private static final int SUCCESS = 1131;//请求结果标识：请求成功
    private static final int ERROR = 1132;//请求结果标识：请求失败
    /**
     * handler接收网络请求成功后的数据
     */
    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            //当前Activity如果已经被销毁就不处理了
            if (isDestroyed())
                return;
            //正常处理
            if (msg.what == SUCCESS) {
                Bundle bundle = msg.getData();
                //请求的接口
                String path = bundle.getString(REQUEST_DATA_PATH);
                //收到的数据
                String reqString = bundle.getString(REQUEST_DATA_DATA);
                try {
                    switch (path) {
                        case path_DAY:
                            if (reqString != null && !reqString.isEmpty()) {
                                //push data to the line chart
                                ArrayList<Entry> array = new ArrayList<>();
                                List<Map<String, String>> data = formatData(reqString);
                                for (int i = 0; i < data.size(); i++) {
                                    int hour = Integer.parseInt(data.get(i).get("hour"));
                                    int value = Integer.parseInt(data.get(i).get("value"));
                                    System.out.println("GET DATA  H:" + hour + " V:" + value + "  ");
                                    array.add(new Entry(value, hour));
                                }
                                LineDataSet lineDataSetDay = new LineDataSet(array, "hour");
                                lineDataSetDay.setLineWidth(5f);//设置线宽
                                lineDataSetDay.setCircleSize(5f);//设置焦点圆心的大小
                                lineDataSetDay.setCircleColor(Color.BLACK);
                                lineDataSetDay.setValueTextSize(8f);

                                String xaxes[] = new String[24];
                                for (int i = 0; i < 24; i++) {
                                    xaxes[i] = i + "";
                                }
                                lineDataSets.clear();
                                lineDataSets.add(lineDataSetDay);
                                LineData l = new LineData(xaxes, lineDataSets);
                                lineChart.setData(l);
                                lineChart.invalidate();
                            }
                            break;

                        case path_WEEK:
                            if (reqString != null && !reqString.isEmpty()) {
                                //push data to the line chart
                                ArrayList<Entry> array = new ArrayList<>();
                                List<Map<String, String>> data = formatData(reqString);
                                for (int i = 0; i < data.size(); i++) {
                                    System.out.println(" MONDAY IS : " + calendarMon.get(Calendar.DAY_OF_MONTH));
                                    int day = Integer.parseInt(data.get(i).get("hour")) - calendarMon.get(Calendar.DAY_OF_MONTH);

                                    if(day <0) {

                                        day = Integer.parseInt(data.get(i).get("hour")) + 3;
                                    }

                                    int value = Integer.parseInt(data.get(i).get("value"));
                                    System.out.println("GET DATA  H:" + day + " V:" + value + "  ");
                                    array.add(new Entry(value, day));
                                }
                                LineDataSet lineDataSetWeek = new LineDataSet(array, "day");
                                lineDataSetWeek.setLineWidth(5f);//设置线宽
                                lineDataSetWeek.setCircleSize(5f);//设置焦点圆心的大小
                                lineDataSetWeek.setCircleColor(Color.BLACK);
                                lineDataSetWeek.setValueTextSize(8f);

                                String xaxes[] = new String[7];
                                xaxes[0] = "Mon";
                                xaxes[1] = "Tue";
                                xaxes[2] = "Wed";
                                xaxes[3] = "Thu";
                                xaxes[4] = "Fri";
                                xaxes[5] = "Sat";
                                xaxes[6] = "Sun";

                                lineDataSets.clear();
                                lineDataSets.add(lineDataSetWeek);
                                LineData l = new LineData(xaxes, lineDataSets);
                                lineChart.setData(l);
                                lineChart.invalidate();
                            }
                            break;

                        case path_MONTH:
                            if (reqString != null && !reqString.isEmpty()) {
                                //push data to the line chart
                                ArrayList<Entry> array = new ArrayList<>();
                                List<Map<String, String>> data = formatData(reqString);

                                for (int i = 0; i < data.size(); i++) {
                                    int month = Integer.parseInt(data.get(i).get("hour"))-1;
                                    int value = Integer.parseInt(data.get(i).get("value"));
                                    System.out.println("GET DATA  H:" + month + " V:" + value + "  ");
                                    array.add(new Entry(value, month));
                                }
                                LineDataSet lineDataSetMonth = new LineDataSet(array, "hour");
                                lineDataSetMonth.setLineWidth(5f);//设置线宽
                                lineDataSetMonth.setCircleSize(5f);//设置焦点圆心的大小
                                lineDataSetMonth.setCircleColor(Color.BLACK);
                                lineDataSetMonth.setValueTextSize(8f);

                                String xaxes[] = new String[12];
                                xaxes[0] = "JAN";
                                xaxes[1] = "FEB";
                                xaxes[2] = "MAR";
                                xaxes[3] = "APR";
                                xaxes[4] = "MAY";
                                xaxes[5] = "JUN";
                                xaxes[6] = "JUL";
                                xaxes[7] = "AUG";
                                xaxes[8] = "SEP";
                                xaxes[9] = "OCT";
                                xaxes[10] = "NOV";
                                xaxes[11] = "DEC";

                                lineDataSets.clear();
                                lineDataSets.add(lineDataSetMonth);
                                LineData l = new LineData(xaxes, lineDataSets);
                                lineChart.setData(l);
                                lineChart.invalidate();
                            }
                            break;
                    }
                } catch (Exception e) {

                }
            } else {
                Toast.makeText(UserActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //format data for day
    private List formatData(String JSONString) {
        System.out.println("ＪＳＯＮ:" + JSONString);
        List<Map<String, String>> data = new ArrayList<>();
        try {
            JSONObject jsonObj = new JSONObject(JSONString);
            for (int i = 0; i < 32; i++) {//从0开始挨个循环找数据
                String timeKey = "H" + String.format("%02d", i);//生成两位int数作为key
                System.out.printf("check key is :" + timeKey);
                if (JSONString.contains(timeKey)) {//contains---字符串内有没有括号内的子字符串，有则返回true
                    String value = jsonObj.getString(timeKey);
                    Map<String, String> map = new HashMap<>();
                    map.put("hour", timeKey.replace("H", ""));
                    map.put("value", value);
                    data.add(map);
                }
            }

        } catch (Exception e) {
        }
        return data;
    }

    //TODO SENSOR HANDLER
    Handler sensorHandler = new Handler() {
        public void handleMessage(Message msg) {
            //System.out.println("Altitude: "+ getHeight());
            //TODO
            secondPre = getHeight();
            if (firstPre == 0.0)
                firstPre = secondPre;
            result = Math.abs(secondPre - firstPre);
            firstPre = secondPre;
            finalOutput = finalOutput + result;
            DecimalFormat df = new DecimalFormat("00.00");
            df.getRoundingMode();
            mAltitude.setText(df.format(finalOutput));
            System.out.println("Result: " + result + "    First:" + firstPre + "   Second: " + secondPre);
            super.handleMessage(msg);
        }
    };

    public double getHeight() {
        return height;
    }

    @Override
    protected void onResume() {
        if (pressureListener == null)
            sensorManager.registerListener(pressureListener, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    protected void onPause() {
        //if (pressureListener != null)
         //   sensorManager.unregisterListener(pressureListener);
        //task.cancel();
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //drawer.setChildInsets(menu,true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.userNickname1) {
            return true;
        }
        if(id == R.id.userNickname1){

        }
        if(id == R.id.userAge){

        }
        if(id == R.id.userGender){

        }
        if(id == R.id.userWeight){

        }
        if(id == R.id.userHeight){

        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        /*
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        */
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
