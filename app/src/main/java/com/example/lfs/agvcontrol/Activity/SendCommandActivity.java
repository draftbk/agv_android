package com.example.lfs.agvcontrol.Activity;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Contacts;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lfs.agvcontrol.Application.MyApplication;
import com.example.lfs.agvcontrol.Model.MapPoint;
import com.example.lfs.agvcontrol.R;
import com.example.lfs.agvcontrol.Service.MyService;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendCommandActivity extends AppCompatActivity implements View.OnClickListener {
    private DataOutputStream writer;
    private Button sendButton,cancelButton;
    private TextView textStartPoint,textEndPoint,textPriority;
    private List<MapPoint> pointList;
    private EditText  textRemark;
    private String startPoint,endPoint,priority,remark;

    private Handler handler;
    private MyService.MySocketBinder mySocketBinder;
    private ServiceConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_command);
        initService();
        init();
    }
    private void initService() {
        connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mySocketBinder= (MyService.MySocketBinder) service;
                mySocketBinder.startSocket(MyApplication.connectIP, 2000,handler);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    private void init() {
        sendButton=findViewById(R.id.button_send);
        cancelButton=findViewById(R.id.button_cancel);
        textStartPoint=findViewById(R.id.text_start_point);
        textEndPoint=findViewById(R.id.text_end_point);
        textPriority=findViewById(R.id.text_priority);
        textRemark=findViewById(R.id.text_remark);
        sendButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        textStartPoint.setOnClickListener(this);
        textEndPoint.setOnClickListener(this);
        textPriority.setOnClickListener(this);
        initList();
        // 初始化handler
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                showToast(msg.obj.toString());
            }
        };
    }

    private void initList() {
        pointList=new ArrayList<MapPoint>();
        pointList.add(new MapPoint(0,0,"1号充电位"));
        pointList.add(new MapPoint(2,1,"1号工位"));
        pointList.add(new MapPoint(3,1,"1号工位"));
//        pointList.add(new MapPoint(53,0,"2号充电位"));
//        pointList.add(new MapPoint(40,0,"3号充电位"));
//        pointList.add(new MapPoint(8,3,"1号仓库"));
//        pointList.add(new MapPoint(11,1,"1号工位"));
//        pointList.add(new MapPoint(20,1,"2号工位"));
//        pointList.add(new MapPoint(29,1,"3号工位"));
//        pointList.add(new MapPoint(32,4,"1号成品库"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_start_point:
                String[] startItems = new String[pointList.size()];
                for(int i=0;i<pointList.size();i++){
                    startItems[i]=pointList.get(i).getName()+"(编号："+pointList.get(i).getId()+")";
                }
                final String[] finalStartItems = startItems;
                AlertDialog startDialog = new AlertDialog.Builder(this).setTitle("选择出发点")
                        .setItems(finalStartItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SendCommandActivity.this, finalStartItems[which], Toast.LENGTH_SHORT).show();
                                textStartPoint.setText(finalStartItems[which]);
                                startPoint=pointList.get(which).getId()+"";
                            }
                        }).create();
                startDialog.show();
                break;
            case R.id.text_end_point:
                String[] endItems = new String[pointList.size()];
                for(int i=0;i<pointList.size();i++){
                    endItems[i]=pointList.get(i).getName()+"(编号："+pointList.get(i).getId()+")";
                }
                final String[] finalEndItems = endItems;
                AlertDialog endDialog = new AlertDialog.Builder(this).setTitle("选择目标点")
                        .setItems(finalEndItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SendCommandActivity.this, finalEndItems[which], Toast.LENGTH_SHORT).show();
                                textEndPoint.setText(finalEndItems[which]);
                                endPoint=pointList.get(which).getId()+"";
                            }
                        }).create();
                endDialog.show();
                break;
            case R.id.text_priority:
                final String[] priorityItems ={"1","2","3"};
                AlertDialog priorityDialog = new AlertDialog.Builder(this).setTitle("选择优先级")
                        .setItems(priorityItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SendCommandActivity.this, priorityItems[which], Toast.LENGTH_SHORT).show();
                                textPriority.setText(priorityItems[which]);
                            }
                        }).create();
                priorityDialog.show();
                break;
            case R.id.button_send:
                if(blankIsEmpty()){
                    showToast("请完整填写信息");
                    break;
                }
                if(checkStartEqualEnd()){
                    showToast("起点终点不能是同一个点");
                    break;
                }

                try {
                    String message="s10000"+","+startPoint+","+endPoint+","
                            +textPriority.getText().toString()+","+textRemark.getText().toString();
                    message=new String(message.getBytes("UTF-8"));
                    showToast(mySocketBinder.sendMessage(message));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                break;
            case R.id.button_cancel:
                finish();
                break;
        }
    }

    private boolean blankIsEmpty() {
        if (textStartPoint.getText().toString().equals("")||textEndPoint.getText().toString().equals("")
                ||textPriority.getText().toString().equals("")||textRemark.getText().toString().equals("")){
            return true;
        }else {
            return false;
        }
    }

    private void showToast(String s) {
        Toast.makeText(SendCommandActivity.this,s,Toast.LENGTH_SHORT).show();
    }

    private boolean checkStartEqualEnd() {
        if(textStartPoint.getText().toString().equals(textEndPoint.getText().toString())){
            return true;
        }else {
            return false;
        }
    }
    /**
     *创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_command_activity_menu,menu); //通过getMenuInflater()方法得到MenuInflater对象，再调用它的inflate()方法就可以给当前活动创建菜单了，第一个参数：用于指定我们通过哪一个资源文件来创建菜单；第二个参数：用于指定我们的菜单项将添加到哪一个Menu对象当中。
        Switch switchShop=(Switch) menu.findItem(R.id.connect_switch).getActionView().findViewById(R.id.switchForActionBar);
        switchShop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
                if (isChecked) { //开店申请
                    showToast("打开连接");
                    Intent startService=new Intent(SendCommandActivity.this,MyService.class);
                    startService(startService);
                    Intent bindIntent=new Intent(SendCommandActivity.this,MyService.class);
                    //绑定服务
                    bindService(bindIntent,connection,BIND_AUTO_CREATE);
                    testSql();
                } else { //关店申请
                    showToast("关闭连接");
                    Intent stopService=new Intent(SendCommandActivity.this,MyService.class);
                    stopService(stopService);
                    //解绑service
                    unbindService(connection);
                }
            }
        });
        return true; // true：允许创建的菜单显示出来，false：创建的菜单将无法显示。
    }

    /**
     *菜单的点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_connect:
                showInputDialog();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private void showInputDialog() {
    /*@setView 装入一个EditView
     */
        final EditText editText = new EditText(SendCommandActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(SendCommandActivity.this);
        inputDialog.setTitle("输入对应IP地址").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputIP=editText.getText().toString();
                        Toast.makeText(SendCommandActivity.this,
                                inputIP,
                                Toast.LENGTH_SHORT).show();
                        MyApplication.connectIP=inputIP;
                    }
                }).show();
    }
    public void testSql()
    {
        //在android中操作数据库最好在子线程中执行，否则可能会报异常
        new Thread()
        {
            public void run() {
                try {
                    //注册驱动
                    Class.forName("com.mysql.jdbc.Driver");
                    String url = "jdbc:mysql://10.24.4.63:3306/agvsystem";
                    Connection conn = DriverManager.getConnection(url, "root", "19940829");
                    Statement stmt = conn.createStatement();
                    String sql = "select * from point";
                    ResultSet rs = stmt.executeQuery(sql);

                    while (rs.next()) {
                        Log.e("yzy", "field1-->"+rs.getString(1)+"  field2-->"+rs.getString(2));
                    }

                    rs.close();
                    stmt.close();
                    conn.close();
                    Log.e("yzy", "success to connect!");
                }catch(ClassNotFoundException e)
                {
                    Log.e("yzy", "fail to connect!"+"  "+e.getMessage());
                } catch (SQLException e)
                {
                    Log.e("yzy", "fail to connect!"+"  "+e.getMessage());
                }
            };
        }.start();

    }

}
