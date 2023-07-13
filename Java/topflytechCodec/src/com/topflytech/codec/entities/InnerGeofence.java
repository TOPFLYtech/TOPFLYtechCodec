package com.topflytech.codec.entities;
 
import java.util.ArrayList;

public class InnerGeofence {
    private int id;
    private int type; //when type is -1 ,empty
    private float radius = -1; //polygon's radius is -1
    private ArrayList<Double[]> points = new ArrayList<>();// lat,lng

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public ArrayList<Double[]> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Double[]> points) {
        this.points = points;
    }

    public void addPoint(double lat,double lng){
        this.points.add(new Double[]{lat,lng});
    }
}
