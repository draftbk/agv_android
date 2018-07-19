package com.example.lfs.agvcontrol.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lfs.agvcontrol.Activity.SendCommandActivity;
import com.example.lfs.agvcontrol.Adapter.TaskAdapter;
import com.example.lfs.agvcontrol.Application.MyApplication;
import com.example.lfs.agvcontrol.Model.Task;
import com.example.lfs.agvcontrol.R;
import com.example.lfs.agvcontrol.Service.MyService;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by lfs on 2018/7/16.
 */

public class TaskListDialog extends Dialog {
    private final Context mContext;
    private ListView mListView;
    private ArrayList<Task> taskList;
    private MyService.MySocketBinder mySocketBinder;
    public TaskListDialog(Context context, ArrayList<Task> taskList, MyService.MySocketBinder mySocketBinder) {
        super(context);
        mContext = context;
        this.taskList=taskList;
        this.mySocketBinder=mySocketBinder;
        initView();
        initListView();
    }

    private void initView() {
        View contentView = View.inflate(mContext, R.layout.task_list_dialog, null);
        mListView = (ListView) contentView.findViewById(R.id.lv);
        setContentView(contentView);
    }

    private void initListView() {
        TaskAdapter adapter = new TaskAdapter(getContext(), R.layout.task_item, taskList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                showNormalDialog(position);
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            return;
        }
        setHeight();
    }

    private void setHeight() {
        Window window = getWindow();
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        WindowManager.LayoutParams attributes = window.getAttributes();
        if (window.getDecorView().getHeight() >= (int) (displayMetrics.heightPixels * 0.6)) {
            attributes.height = (int) (displayMetrics.heightPixels * 0.6);
        }
        window.setAttributes(attributes);
    }

    private void showNormalDialog(final int position){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getContext());
        normalDialog.setMessage("确认撤销吗？");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        showToast("撤销中");
                        try {
                            MyApplication.cancelId=taskList.get(position).getTaskId();
                            String message="s10000"+","+taskList.get(position).getTaskId();
                            message=new String(message.getBytes("UTF-8"));
                            showToast(mySocketBinder.sendMessage(message));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        // 关闭本dialog
                        dismiss();
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

    private void showToast(String s) {
        Toast.makeText(getContext(),s,Toast.LENGTH_SHORT).show();
    }

}
