package com.lzf.myowriststrap.bean;

import com.thalmic.myo.Arm;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;

/**
 * Created by MJCoder on 2019-04-18.
 */

public class Orientation {

    private String dateTime;
    private String content;
    private Arm arm;
    private XDirection xDirection;
    private Pose pose;
    private Quaternion orientation;
    private double x;
    private double y;
    private double z;
    private double w;
    private double roll;
    private double pitch;
    private double yaw;


    public Orientation(String dateTime, String content, Arm arm, XDirection xDirection, Pose pose, Quaternion orientation, double x, double y, double z, double w, double roll, double pitch, double yaw) {
        this.dateTime = dateTime;
        this.content = content;
        this.arm = arm;
        this.xDirection = xDirection;
        this.pose = pose;
        this.orientation = orientation;
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Arm getArm() {
        return arm;
    }

    public void setArm(Arm arm) {
        this.arm = arm;
    }

    public XDirection getxDirection() {
        return xDirection;
    }

    public void setxDirection(XDirection xDirection) {
        this.xDirection = xDirection;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public Quaternion getOrientation() {
        return orientation;
    }

    public void setOrientation(Quaternion orientation) {
        this.orientation = orientation;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    @Override
    public String toString() {
        return "Orientation{" +
                "dateTime='" + dateTime + '\'' +
                ", content='" + content + '\'' +
                ", arm=" + arm +
                ", xDirection=" + xDirection +
                ", pose=" + pose +
                ", orientation=" + orientation +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                ", roll=" + roll +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                '}';
    }
}
