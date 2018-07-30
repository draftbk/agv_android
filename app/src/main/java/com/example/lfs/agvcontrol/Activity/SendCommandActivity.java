package com.example.lfs.agvcontrol.Activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.example.lfs.agvcontrol.Dialog.TaskListDialog;
import com.example.lfs.agvcontrol.Model.MapPoint;
import com.example.lfs.agvcontrol.Model.Task;
import com.example.lfs.agvcontrol.R;
import com.example.lfs.agvcontrol.Service.MyService;
import com.example.lfs.agvcontrol.Utils.Utils;

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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendCommandActivity extends AppCompatActivity implements View.OnClickListener {
    private DataOutputStream writer;
    private Button sendButton,cancelButton;
    private TextView textStartPoint,textEndPoint,textStartPointType,textEndPointType,textPriority;
    private List<MapPoint> pointList,tempStartPointList,tempEndPointList;
    private HashSet<Integer> pointTypeSet;
    private EditText  textContent,textRemark;
    private String startPoint,endPoint,priority,remark;
    private Switch switchShop;
    private Handler handler;
    private MyService.MySocketBinder mySocketBinder;
    private ServiceConnection connection;
    private TaskListDialog taskListDialog;
    private ArrayList<Task> taskList;
    private Task tempTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_command);
        initService();
        init();
    }
    private void initService() {
        getPointFromSql();
        connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mySocketBinder= (MyService.MySocketBinder) service;
                mySocketBinder.startSocket(MyApplication.connectIP, MyApplication.connectPort,handler);
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
        textStartPointType=findViewById(R.id.text_start_point_type);
        textEndPointType=findViewById(R.id.text_end_point_type);
        textEndPoint=findViewById(R.id.text_end_point);
        textPriority=findViewById(R.id.text_priority);
        textContent=findViewById(R.id.text_content);
        textRemark=findViewById(R.id.text_remark);
        sendButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        textStartPoint.setOnClickListener(this);
        textEndPoint.setOnClickListener(this);
        textStartPointType.setOnClickListener(this);
        textEndPointType.setOnClickListener(this);
        textPriority.setOnClickListener(this);
        initList();
        // 初始化handler
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what==0){
                    showToast("收到信息"+msg.obj.toString());
                    String[] message=msg.obj.toString().split(",");
                    //处理发送任务后的返回信息
                    if (message[0].equals("s10000")){
                        if (tempTask.getTaskId().equals(message[1])&&message[2].contains("1")){
                            showToast("任务发送成功");
                            taskList.add(tempTask);
                        }else {
                            showToast("任务发送失败");
                        }
                    }else if(message[0].equals("s10001")){
                        if (MyApplication.cancelId.equals(message[1])&&message[2].contains("1")){
                            showToast("任务撤销成功");
                            for(int i=0;i<taskList.size();i++){
                                if (taskList.get(i).getTaskId().equals(MyApplication.cancelId)){
                                    taskList.remove(i);
                                }
                            }
                        }else {
                            showToast("任务撤销失败");
                        }
                    }

                } else if (msg.what==1){
                    switchShop.performClick();
                }else if (msg.what==2){
                    if (msg.obj.toString().equals("connected")){
                        showToast("连接成功");
                        setSelfIp();
                    }else if (msg.obj.toString().equals("unconnected")){
                        showToast("连接失败，请重试");
                        switchShop.performClick();
                    }
                }
            }
        };

    }

    private void setSelfIp() {

    }

    private void initList() {
        pointList=new ArrayList<MapPoint>();
        pointTypeSet=new HashSet<Integer>();
        taskList=new ArrayList<Task>();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_start_point:
                if (pointList.size()==0){
                    showToast("请先连接再选择");
                    break;
                }
                tempStartPointList=new ArrayList<MapPoint>();
                for (int i=0;i<pointList.size();i++){
                    if (textStartPointType.getText().toString().equals("")||textStartPointType.getText().toString().equals("全部")){
                        tempStartPointList.add(pointList.get(i));
                    } else if (pointList.get(i).getType()==Integer.parseInt(textStartPointType.getText().toString())) {
                        tempStartPointList.add(pointList.get(i));
                    }
                }
                String[] startItems = new String[tempStartPointList.size()];
                for(int i=0;i<tempStartPointList.size();i++){
                    startItems[i] = tempStartPointList.get(i).getName();
                }
                final String[] finalStartItems = startItems;
                AlertDialog startDialog = new AlertDialog.Builder(this).setTitle("选择出发点")
                        .setItems(finalStartItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SendCommandActivity.this, finalStartItems[which], Toast.LENGTH_SHORT).show();
                                textStartPoint.setText(finalStartItems[which]);
                                startPoint=tempStartPointList.get(which).getId()+"";
                            }
                        }).create();
                startDialog.show();
                break;
            case R.id.text_end_point:
                if (pointList.size()==0){
                    showToast("请先连接再选择");
                    break;
                }
                tempEndPointList=new ArrayList<MapPoint>();
                for (int i=0;i<pointList.size();i++){
                    if (textEndPointType.getText().toString().equals("")||textEndPointType.getText().toString().equals("全部")){
                        tempEndPointList.add(pointList.get(i));
                    }  else if (pointList.get(i).getType()==Integer.parseInt(textEndPointType.getText().toString())) {
                        tempEndPointList.add(pointList.get(i));
                    }
                }
                String[] endItems = new String[tempEndPointList.size()];
                for(int i=0;i<tempEndPointList.size();i++){
                    endItems[i] = tempEndPointList.get(i).getName();
                }
                final String[] finalEndItems = endItems;
                AlertDialog endDialog = new AlertDialog.Builder(this).setTitle("选择目标点")
                        .setItems(finalEndItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SendCommandActivity.this, finalEndItems[which], Toast.LENGTH_SHORT).show();
                                textEndPoint.setText(finalEndItems[which]);
                                endPoint=tempEndPointList.get(which).getId()+"";
                            }
                        }).create();
                endDialog.show();
                break;
            case R.id.text_start_point_type:
                if (pointTypeSet.size()==0){
                    showToast("请先连接再选择");
                    break;
                }
                String[] startTypeItems = new String[pointTypeSet.size()+1];
                startTypeItems[0]="全部";
                int i_start_type=1;
                for(Iterator it = pointTypeSet.iterator(); it.hasNext();)
                {
                    startTypeItems[i_start_type]=it.next().toString();
                    i_start_type++;
                }
                final String[] finalStartTypeItems = startTypeItems;
                AlertDialog startTypeDialog = new AlertDialog.Builder(this).setTitle("选择出发点类型")
                        .setItems(finalStartTypeItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SendCommandActivity.this, finalStartTypeItems[which], Toast.LENGTH_SHORT).show();
                                textStartPointType.setText(finalStartTypeItems[which]);
                            }
                        }).create();
                startTypeDialog.show();
                break;
            case R.id.text_end_point_type:
                if (pointTypeSet.size()==0){
                    showToast("请先连接再选择");
                    break;
                }
                String[] endTypeItems = new String[pointTypeSet.size()+1];
                endTypeItems[0]="全部";
                int i_end_type=1;
                for(Iterator it = pointTypeSet.iterator(); it.hasNext();)
                {
                    endTypeItems[i_end_type]=it.next().toString();
                    i_end_type++;
                }
                final String[] finalEndTypeItems = endTypeItems;
                AlertDialog endTypeDialog = new AlertDialog.Builder(this).setTitle("选择目标点类型")
                        .setItems(finalEndTypeItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SendCommandActivity.this, finalEndTypeItems[which], Toast.LENGTH_SHORT).show();
                                textEndPointType.setText(finalEndTypeItems[which]);
                            }
                        }).create();
                endTypeDialog.show();
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
                if (Utils.isFastClick()) {
                    // 进行点击事件后的逻辑操作
                    if(blankIsEmpty()){
                        showToast("请完整填写信息");
                        break;
                    }
                    if(checkStartEqualEnd()){
                        showToast("起点终点不能是同一个点");
                        break;
                    }
                    showNormalDialog();
                }else {
                    showToast("按钮点击间隔为"+Utils.getMinClickDelayTime()+"请不要点击过于频繁");
                }
                break;
            case R.id.button_cancel:
                break;
        }
    }

    private boolean blankIsEmpty() {
        if (textStartPoint.getText().toString().equals("")||textEndPoint.getText().toString().equals("")
                ||textPriority.getText().toString().equals("")||textContent.getText().toString().equals("")){
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
        switchShop=(Switch) menu.findItem(R.id.connect_switch).getActionView().findViewById(R.id.switchForActionBar);
        switchShop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
                if (isChecked) { //开店申请
                    showToast("打开连接");
                    //获取当前自身IP，并存到Application里
                    MyApplication.getWIFILocalIpAdress(SendCommandActivity.this);
                    //页面跳转
                    Intent startService=new Intent(SendCommandActivity.this,MyService.class);
                    startService(startService);
                    Intent bindIntent=new Intent(SendCommandActivity.this,MyService.class);
                    //绑定服务
                    bindService(bindIntent,connection,BIND_AUTO_CREATE);
                } else { //关店申请
//                    showToast("关闭连接");
                    Intent stopService=new Intent(SendCommandActivity.this,MyService.class);
                    stopService(stopService);
                    //解绑service
                    unbindService(connection);
                }
            }
        });
        Message msg=new Message();
        msg.what=1;
        handler.sendMessage(msg);
        return true; // true：允许创建的菜单显示出来，false：创建的菜单将无法显示。
    }

    /**
     *菜单的点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_connect:
                if (switchShop.isChecked()){
                    showToast("先断开连接再设置ip");
                }else {
                    showSettingDialog();
                }
                break;
            case R.id.menu_task_list:
                taskListDialog=new TaskListDialog(SendCommandActivity.this,taskList,mySocketBinder);
                taskListDialog.show();
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

    private void showNormalDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(SendCommandActivity.this);
        normalDialog.setMessage("确认发送吗？");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //获取时间，用于组成任务id
                        Date date=new Date();
                        String dateStr=String.format("%tT%n",date).replaceAll("\r|\n", "");
                        tempTask=new Task(MyApplication.workerId,dateStr,textContent.getText().toString(),textStartPoint.getText().toString()
                                ,textEndPoint.getText().toString(),MyApplication.workerId+dateStr);
                        //...To-do
                        try {
                            String message="s10000"+","+MyApplication.workerId+dateStr+","+startPoint+","+endPoint+","
                                    +textPriority.getText().toString()+","+textContent.getText().toString()+","
                                    +textRemark.getText().toString()+","
                                    +MyApplication.workerId+","+MyApplication.selfIP;
                            message=new String(message.getBytes("UTF-8"));
                            showToast(mySocketBinder.sendMessage(message));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }

    private void showSettingDialog() {
    /*@setView 装入一个EditView
     */
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(SendCommandActivity.this);
        final View dialogView = LayoutInflater.from(SendCommandActivity.this)
                .inflate(R.layout.setting_dialog,null);
        inputDialog.setTitle("输入对应IP地址和端口号");
        inputDialog.setView(dialogView);
        final EditText ipEdit=dialogView.findViewById(R.id.edit_ip);
        ipEdit.setText(MyApplication.connectIP);
        final EditText portEdit=dialogView.findViewById(R.id.edit_port);
        portEdit.setText(MyApplication.connectPort+"");
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputIP=ipEdit.getText().toString();
                        String inputPort=portEdit.getText().toString();
                        Toast.makeText(SendCommandActivity.this,
                                inputIP,
                                Toast.LENGTH_SHORT).show();
                        MyApplication.saveIp(SendCommandActivity.this,inputIP);
                        MyApplication.savePort(SendCommandActivity.this,inputPort);
                    }
                }).show();
    }

    //从数据库获取点信息
    public void getPointFromSql()
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
                    // 更新 pointList
                    pointList.clear();
                    pointTypeSet.clear();
                    while (rs.next()) {
                        Log.e("slf", "field1-->"+rs.getInt(1)+"  field2-->"+rs.getString(2)
                                +"  field3-->"+rs.getInt(3));
                        pointList.add(new MapPoint(rs.getInt(1),rs.getInt(3),rs.getString(2)));
                        pointTypeSet.add(rs.getInt(3));
                    }
                    rs.close();
                    stmt.close();
                    conn.close();
                    Log.e("slf", "success to connect!");
                }catch(ClassNotFoundException e)
                {
                    Log.e("slf", "fail to connect!"+"  "+e.getMessage());
                } catch (SQLException e)
                {
                    Log.e("slf", "fail to connect!"+"  "+e.getMessage());
                }
            };
        }.start();

    }

}
