package com.example.lfs.agvcontrol.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.lfs.agvcontrol.Model.Task;
import com.example.lfs.agvcontrol.R;

import java.util.ArrayList;

/**
 * Created by lfs on 2018/7/16.
 */

public class TaskListDialog extends Dialog {
    private final Context mContext;
    private ListView mListView;
    private ArrayList<Task> taskList;
    public TaskListDialog(Context context, ArrayList<Task> taskList) {
        super(context);
        mContext = context;
        this.taskList=taskList;
        initView();
        initListView();
    }

    private void initView() {
        View contentView = View.inflate(mContext, R.layout.task_list_dialog, null);
        mListView = (ListView) contentView.findViewById(R.id.lv);
        setContentView(contentView);
    }

    private void initListView() {
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_expandable_list_item_1);
        for (int i = 0; i < taskList.size(); i++) {
            stringArrayAdapter.add(taskList.get(i).getContent());
        }
        mListView.setAdapter(stringArrayAdapter);
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




}
