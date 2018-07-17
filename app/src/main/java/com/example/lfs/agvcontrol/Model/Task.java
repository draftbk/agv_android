package com.example.lfs.agvcontrol.Model;

import java.util.Date;

/**
 * Created by lfs on 2018/7/16.
 */

public class Task {
    private String sendByWho;
    private String startDate;
    private String content;
    private String startPoint;
    private String aimPoint;
    private String taskId;

    public Task(String sendByWho, String startDate, String content, String startPoint, String aimPoint, String taskId) {
        this.sendByWho = sendByWho;
        this.startDate = startDate;
        this.content = content;
        this.startPoint = startPoint;
        this.aimPoint = aimPoint;
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }


    public String getSendByWho() {
        return sendByWho;
    }

    public void setSendByWho(String sendByWho) {
        this.sendByWho = sendByWho;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getAimPoint() {
        return aimPoint;
    }

    public void setAimPoint(String aimPoint) {
        this.aimPoint = aimPoint;
    }
}

