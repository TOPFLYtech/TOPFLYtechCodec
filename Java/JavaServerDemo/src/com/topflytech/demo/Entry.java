package com.topflytech.demo;


import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Entry {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:application-context.xml");
        try {
            //Can deal 25 25 head message server
            new DeviceServer(NoObdDevicePipelineFactory.class).startup(10001);
            //Can deal 26 26 head message server
            new DeviceServer(ObdDevicePipelineFactory.class).startup(10002);
            //Can deal 27 27 head message server
            new DeviceServer(PersonalDevicePipelineFactory.class).startup(10003);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Device Server start.......");
    }
}
