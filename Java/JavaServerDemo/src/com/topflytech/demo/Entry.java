package com.topflytech.demo;


import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Entry {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:application-context.xml");
        try {
            new DeviceServer(NoObdDevicePipelineFactory.class).startup(10001);
            new DeviceServer(ObdDevicePipelineFactory.class).startup(10002);
            new DeviceServer(PersonalDevicePipelineFactory.class).startup(10003);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Device Server start.......");
    }
}
