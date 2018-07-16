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


    public Task(String sendByWho, String startDate, String content, String startPoint, String aimPoint) {
        this.sendByWho = sendByWho;
        this.startDate = startDate;
        this.content = content;
        this.startPoint = startPoint;
        this.aimPoint = aimPoint;
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

