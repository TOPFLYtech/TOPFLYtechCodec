package com.topflytech.lockActive.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Utils {
    public static boolean isDebug = false;
    public static String textViewSuffix = "TV";
    public static String linearLayoutSuffix = "LL";
    public static String buttonSuffix = "BTN";
    public static String editTextSuffix = "ET";
    public static String switchButtonSuffix = "SB";
    public static HashMap<Integer,String> controlFunc = new HashMap<Integer,String>(){{
        put(-999,"connectBle");
        put(1,"readBG9xStatus");
        put(11,"readMcuVersion");
        put(12,"readOtaVersion");
        put(13,"readObdVersion");
        put(14,"readGpsVersion");
        put(15,"readWifiVersion");
        put(16,"readImei");
        put(17,"readSpeed");
        put(19,"readAlarmStatus");
        put(20,"readAccStatus");
        put(21,"readChargeStatus");
        put(22,"readNetworkStatus");
        put(23,"readGpsValidStatus");
        put(24,"readBatteryVoltage");
        put(25,"readSolarVoltage");
        put(26,"readExternalVoltage");
        put(27,"readTemp");
        put(1001,"readGsensorValue");
        put(1002,"readDin0");
        put(1003,"readDin1");
        put(1004,"readDin2");
        put(1005,"readDin3");
        put(1006,"readDin4");
        put(1007,"readDin5");
        put(1008,"readAin0");
        put(1009,"readAin1");
        put(1010,"readAin2");
        put(1011,"readAin3");
        put(1012,"readAin4");
        put(1013,"readBleSensorCount");
        put(1014,"readBleSensor");
        put(2001,"addBleSensor");
        put(2002,"delBleSensor");
        put(2003,"rebootDevice");
        put(2004,"rebootBG9x");
        put(2005,"powerOff");
        put(2006,"resetFactory");
        put(2007,"checkNewVersion");
        put(2016,"changeUnclockPwd");
        put(4001,"accOnTypeControl");
        put(4002,"voutControl");
        put(4003,"dout0Control");
        put(4004,"dout1Control");
        put(4005,"dout2Control");
        put(4013,"tempSensorUploadIntervalControl");
        put(4014,"doorSensorUploadIntervalControl");
        put(4015,"relaySensorUploadIntervalControl");
        put(4016,"fuelSensorUploadIntervalControl");
        put(11001,"readImsi");
        put(11002,"readIccid");
        put(11003,"readOperator");
        put(11004,"readNetworkStandard");
        put(11005,"readNetworkFrequencyBand");
        put(11006,"readRssi");
        put(11007,"readSimStatus");
        put(11008,"readIp1ConnectStatus");
        put(11009,"readIp2ConnectStatus");
        put(11010,"readIp3ConnectStatus");
        put(11011,"readIp4ConnectStatus");
        put(11013,"readAlarmOpen40_79");
        put(11012,"readAlarmOpen0_39");
        put(12001,"searchOrderControl");
        put(12002,"doubleIPModeControl");
        put(12004,"heartbeatIntervalControl");
        put(12005,"msgHead01Control");
        put(12006,"msgHead02Control");
        put(12007,"msgHead03Control");
        put(12008,"msgHead04Control");
        put(12009,"msgHead05Control");
        put(12010,"msgHead06Control");
        put(12011,"msgHead07Control");
        put(12012,"msgHead08Control");
        put(12013,"msgHead09Control");
        put(12014,"msgHead10Control");
        put(12015,"msgHead11Control");
        put(12016,"msgHead12Control");
        put(12017,"msgHead13Control");
        put(12018,"msgHead14Control");
        put(12031,"ip1SetControl");
        put(12032,"ip2SetControl");
        put(12033,"ip3SetControl");
        put(12035,"apnAddressSetControl");
        put(12036,"apnUsernameSetControl");
        put(12037,"apnPwdSetControl");
        put(12038,"socketProtocolSetControl");
        put(12039,"mqttServerSetControl");
        put(12040,"mqttUsernameControl");
        put(12041,"mqttPwdControl");
        put(12042,"mqttCertificateSetControl");
        put(12043,"mqttClientUrlControl");
        put(12044,"mqttUserUrlControl");
        put(12045,"mqttSslControl");
        put(12046,"timerSetControl");
        put(12047,"timer1SetControl");
        put(12049,"alarmControl");
    }};

    public static String[] editItemFuncsIsBool = new String[]{"4003","4004","4005","12002","12005_0","12005_1",
        "12006_0","12006_1","12007_0","12007_1","12008_0","12008_1","12009_0","12009_1","12010_0","12010_1",
        "12011_0","12011_1","12012_0","12012_1","12013_0","12013_1","12014_0","12014_1","12015_0","12015_1",
        "12016_0","12016_1","12017_0","12017_1","12018_0","12018_1","12045"};
    public static String[] editItemFuncsIsString = new String[]{"12031_0","12032_0","12033_0","12035","12036",
            "12037","12039_0","12040","12041","12042","12043","12044"};
    public static String[] editItemFuncsIsInteger = new String[]{"4001_1","4001_2","4013_0","4013_1","4014","4015",
            "4016","12004","12031_1","12032_1","12033_1","12039_1","12046_0","12046_1","12046_2","12046_3",
            "12047_0","12047_1","12047_2","12047_3"};
    public static String[] editItemListViewStartFromOne = new String[]{"4001_0","12038"};
//    public static HashMap<String,Integer[]> funcListviewItems = new HashMap<String,Integer[]>(){{
//        put("4001_0",new Integer[]{R.string.shock,R.string.external_voltage,R.string.ignition_signal});
//        put("4002",new Integer[]{R.string.close,R.string.output_5v,R.string.output_12v});
//        put("12038",new Integer[]{R.string.tcp,R.string.udp,R.string.mqtt});
//        put("12001",new Integer[]{R.string.M1_NB_GSM, R.string.GSM, R.string.M1, R.string.NB,
//                R.string.M1_NB, R.string.NB_M1, R.string.M1_GSM, R.string.GSM_M1, R.string.NB_GSM,
//                R.string.GSM_NB, R.string.M1_GSM_NB, R.string.NB_M1_GSM, R.string.NB_GSM_M1,
//                R.string.GSM_M1_NB, R.string.GSM_NB_M1});
//    }};
//    public static HashMap<Integer,Integer> bleSensorDesc = new HashMap<Integer,Integer>(){{
//        put(1,R.string.tempHumiditySensor);
//        put(2,R.string.doorSensor);
//        put(3,R.string.relaySensor);
//        put(4,R.string.tireSensor);
//        put(5,R.string.tzTempHumiditySensor);
//        put(6,R.string.sosTagSensor);
//        put(7,R.string.idTagSensor);
//        put(8,R.string.fuelSensor);
//    }};
    // domain func : domain : port = 0:1
    //funcCode,min,max,byteCount,Special value
    public static HashMap<String,Integer[]> editItemFuncsIsIntLimits = new HashMap<String,Integer[]>(){{
        put("4001_1",new Integer[]{0,65535,2});
        put("4001_2",new Integer[]{0,65535,2});
        put("4013_0",new Integer[]{300,60000,2});
        put("4013_1",new Integer[]{300,60000,2});
        put("4014",new Integer[]{300,60000,2});
        put("4015",new Integer[]{300,60000,2});
        put("4016",new Integer[]{300,60000,2});
        put("12004",new Integer[]{0,255,1});
        put("12031_1",new Integer[]{0,65535,2});
        put("12032_1",new Integer[]{0,65535,2});
        put("12033_1",new Integer[]{0,65535,2});
        put("12039_1",new Integer[]{0,65535,2});
        put("12046_0",new Integer[]{3,60000,2,0});
        put("12046_1",new Integer[]{3,86400,4,0});
        put("12046_2",new Integer[]{1,180,1,0});
        put("12046_3",new Integer[]{1,60000,2,0});
        put("12047_0",new Integer[]{3,60000,2,0});
        put("12047_1",new Integer[]{3,86400,4,0});
        put("12047_2",new Integer[]{1,180,1,0});
        put("12047_3",new Integer[]{1,60000,2,0});
    }};
    public static HashMap<Integer,Integer> multiValueCountMap = new HashMap<Integer,Integer>(){{
        put(4001,3);
        put(4013,2);
        put(12005,2);
        put(12006,2);
        put(12007,2);
        put(12008,2);
        put(12009,2);
        put(12010,2);
        put(12011,2);
        put(12012,2);
        put(12013,2);
        put(12014,2);
        put(12015,2);
        put(12016,2);
        put(12017,2);
        put(12018,2);
        put(12031,2);
        put(12032,2);
        put(12033,2);
        put(12039,2);
        put(12046,4);
        put(12047,4);
    }};
    public static Integer[] statusPanelFunc = new Integer[]{11,12,13,14,15,16,17,18,19,20,21,22,23,24,
            25,26,27,1001,11001,11002,11003,11004,11005,11006,11007,11008,11009,11010,11011,12001};
    public static Integer[] networkPanelFunc = new Integer[]{12002,12004,12005,12006,12007,12008,12009,
            12010,12011,12012,12013,12014,12015,12016,12017,12018,12031,12032,12033,12034,12035,12036,12037,
            12038,12039,12040,12041,12042,12043,12044,12045,12046,12047};
    public static Integer[] advancedPanelFunc = new Integer[]{4001,2003,2004,2005,2006,2007};
    public static Integer[] sensorsPanelFunc = new Integer[]{4013,4014,4015,4016,1014};
    public static Integer[] ioPanelFunc = new Integer[]{1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,4002,4003,4004,4005};
    public static Integer[] alertsPanelFunc = new Integer[]{11012,11013};
    public static Integer[] onlyBtnClickFuncsShowEdit = new Integer[]{1014,11012,2003,2004,2005,2006,2007};
    public static Integer[] onlyBtnClickFuncs = new Integer[]{1014,11012,2003,2004,2005,2006,2007};
    public static Integer[] needSetOtherPageFuncs = new Integer[]{1013,1014,11012,11013,12049};
    public static Integer[] notSupportCurrentFuncs = new Integer[]{17,18,19};

    public static Integer[] needBatchConfigFuncs = new Integer[]{4001,4002,4003,4004,4005,4013,4014,4015,4016,12001,12002,12004,
            12005,12006,12007,12008,12009,12010,12011,12012,12013,12014,12015,12016,12017,12018,12031,12032,12033,12035,12036,12037,
            12038,12039,12040,12041,12042,12043,12044,12045,12046,12047,12049};

    public static Integer[] tc008SupportAlarm = new Integer[]{1,3,6,7,8,9,12,13,14,15,16,17,18,19,21,25,26,29,30,31,40,41,
            42,43,44,45,46,47,48,49,50,51,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,73,74,75
    };
    public static Integer[] tc009SupportAlarm = new Integer[]{1,6,7,8,9,12,13,16,17,18,19,21,25,26,29,30,31,70,73,74};

    public static Integer[] tc008Funcs = new Integer[]{11 ,12 ,16 ,19 ,20 ,21 ,22 ,23 ,24 ,26 ,27 ,1001 ,
            1002 ,1003 ,1004 ,1005 ,1006 ,1007 ,1008 ,1009 ,1010 ,1013 ,1014 ,11001 ,11002 ,11003 ,
            11004 ,11005 ,11006 ,11007 ,11008 ,11009 ,11012 ,11013,2001 ,2002 ,2003 ,2004 ,
            2005 ,2006 ,2007 ,4001 ,4002 ,4003 ,4004 ,4005 ,4013 ,4014 ,4015 ,4016 ,12001,12002 ,12004 ,12005,
            12007 ,12009 ,12010 ,12011 ,12012 ,12013 ,12015 ,12017 ,12018 ,12031 ,12032 ,12033 ,
            12035 ,12036 ,12037 ,12038 ,12039 ,12040 ,12041 ,12042 ,12043 ,12044 ,12045 ,12046 ,
            12047 ,12049  };
    public static Integer[] tc009Funcs = new Integer[]{11 ,12 ,16 ,19 ,20 ,21 ,22 ,23 ,24 ,25 ,27 ,1013 ,
            1014 ,11001 ,11002 ,11003 ,11004 ,11005 ,11006 ,11007 ,11008 ,11009 ,11012,2001 ,
            2002 ,2003 ,2004 , 2005 ,2006 ,2007,4013 ,4014 ,4015 ,4016 ,12001,12002 ,12004 ,12005,12006 ,12007 ,
            12008 ,12009 ,12010 ,12011 ,12015 ,12031 ,12032 ,12033 ,12035 ,12036 ,12037 ,
            12038 ,12039 ,12040 ,12041 ,12042 ,12043 ,12044 ,12045 ,12046 ,12049  };
    public static Integer[] tc010Funcs = new Integer[]{11 ,12 ,16 ,18 ,19 ,20 ,21 ,22 ,23 ,24 ,26 ,27 ,1001 ,
            1002 ,1003 ,1004 ,1005 ,1006 ,1007 ,1008 ,1009 ,1010 ,1013 ,1014 ,11001 ,11002 ,11003 ,
            11004 ,11005 ,11006 ,11007 ,11008 ,11009 ,11013,2001 ,2002 ,2003 ,2004 , 2005 ,2006 ,
            2007,4001 ,4002 ,4003 ,4004 ,4005 ,4013 ,4014 ,4015 ,4016 ,12001,12002 ,12004 ,12005,12006 ,12007 ,12008 ,
            12009 ,12010 ,12011 ,12012 ,12013 ,12015 ,12031 ,12032 ,12033 ,12035 ,12036 ,12037 ,12038 ,
            12039 ,12040 ,12041 ,12042 ,12043 ,12044 ,12045 ,12046 ,12047 ,12049  };
    public static Integer[] tc011Funcs = new Integer[]{11 ,12 ,13 ,14 ,16 ,18 ,19 ,20 ,21 ,22 ,23 ,24 ,26 ,27 ,
            1013 ,1014 ,11001 ,11002 ,11003 ,11004 ,11005 ,11006 ,11007 ,11008 ,11009 ,11013,2001 ,
            2002 ,2003 ,2004 , 2005 ,2006 ,2007,4001 ,4013 ,4014 ,4015 ,4016 ,12001,12002 ,12004 ,12005,12006 ,12007 ,
            12008 ,12009 ,12010 ,12011 ,12012 ,12013 ,12015 ,12031 ,12032 ,12033 ,12035 ,12036 ,12037 ,12038 ,
            12039 ,12040 ,12041 ,12042 ,12043 ,12044 ,12045 ,12046 ,12049};
    public static Integer[] tc013Funcs = new Integer[]{11 ,12 ,15 ,16 ,19 ,20 ,21 ,22 ,23 ,24 ,26 ,27 ,1013 ,1014 ,
            11001 ,11002 ,11003 ,11004 ,11005 ,11006 ,11007 ,11008 ,11009 ,11013 ,12001,2001 ,2002 ,2003 ,
            2004 , 2005 ,2006 ,2007};
    public static Integer[] tc015Funcs = new Integer[]{4001 ,4013 ,4014 ,4015 ,4016 ,12002 ,12004 ,12006 ,12007 ,
            12008 ,12009 ,12010 ,12011 ,12012 ,12013 ,12015 ,12031 ,12032 ,12033 ,12035 ,12036 ,12037 ,
            12038 ,12039 ,12040 ,12041 ,12042 ,12043 ,12044 ,12045 ,12046 ,12049 ,2001 ,2002 ,2003 ,2004 ,
            2005 ,2006 ,2007,4013 ,4014 ,4015 ,4016 ,12001,12002 ,12004 ,12005,12006 ,12007 ,12008 ,12009 ,12010 ,12011 ,
            12012 ,12013 ,12015 ,12031 ,12032 ,12033 ,12035 ,12036 ,12037 ,12038 ,12039 ,12040 ,12041 ,12042 ,
            12043 ,12044 ,12045 ,12046 ,12047 ,12049 };

    public static Integer[] getProtocolFunc(String protocolType){
        if (protocolType.equals("tc008")){
            return Utils.tc008Funcs;
        }else if (protocolType.equals("tc009")){
            return Utils.tc009Funcs;
        }else if (protocolType.equals("tc010")){
            return Utils.tc010Funcs;
        }else if (protocolType.equals("tc011")){
            return Utils.tc011Funcs;
        }else if (protocolType.equals("tc013")){
            return Utils.tc013Funcs;
        }else if (protocolType.equals("tc015") || protocolType.equals("SolarGuardX 200") || protocolType.equals("SolarGuardX 110")){
            return Utils.tc015Funcs;
        }else {
            return new Integer[]{};
        }
    }
    public static HashMap<Integer,String> modelTypeMap = new HashMap<Integer,String>(){{
        put(68,"TLW2-12BL");
        put(77,"TLP2-SFB");
    }};

    public static HashMap<Integer,String> modelProtocolMap = new HashMap<Integer,String>(){{
        put(68,"tc008");
        put(77,"tc009");
    }};

//    public static HashMap<String,Integer> funcUnitStrMap = new HashMap<String,Integer>(){{
//        put("4001_1",R.string.mv);
//        put("4001_2",R.string.mv);
//        put("4013_0",R.string.seconds);
//        put("4013_1",R.string.seconds);
//        put("4014",R.string.seconds);
//        put("4015",R.string.seconds);
//        put("4016",R.string.seconds);
//        put("12004",R.string.minutes);
//    }};


    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static HashMap<Integer,String> alarmNumbDesc = new HashMap<Integer,String>(){{
        put(1,"disassemble");
        put(2,"uncover");
        put(3,"sos");
        put(4,"unboxing");
        put(5,"fallDown");
        put(6,"lowPower");
        put(7,"lowPowerRecovery");
        put(8,"highTemp");
        put(9,"shock");
        put(10,"collision");
        put(11,"tilt");
        put(12,"usbConnection");
        put(13,"usbDisconnect");
        put(14,"enterElectronicFence");
        put(15,"leaveElectronicFence");
        put(16,"ignition");
        put(17,"parking");
        put(18,"enterIdle");
        put(19,"exitIdle");
        put(20,"bootUp");
        put(21,"disassembleRecovery");
        put(22,"openCoverRecovery");
        put(23,"unboxingRecovery");
        put(24,"fallRecovery");
        put(25,"highTempRecovery");
        put(26,"shockRecovery");
        put(27,"collisionRecovery");
        put(28,"tiltRecovery");
        put(29,"shutdown");
        put(30,"lowTemp");
        put(31,"lowTempRecovery");
        put(32,"32");
        put(33,"33");
        put(34,"34");
        put(35,"35");
        put(36,"36");
        put(37,"37");
        put(38,"38");
        put(39,"39");

        put(40,"din0On");
        put(41,"din0Off");
        put(42,"din1On");
        put(43,"din1Off");
        put(44,"din2On");
        put(45,"din2Off");
        put(46,"din3On");
        put(47,"din3Off");
        put(48,"din4On");
        put(49,"din4Off");

        put(50,"din5On");
        put(51,"din5Off");
        put(52,"din6On");
        put(53,"din6Off");
        put(54,"ain0Inc");
        put(55,"ain0StopInc");
        put(56,"ain0Dec");
        put(57,"ain0StopDec");
        put(58,"ain1Inc");
        put(59,"ain1StopInc");

        put(60,"ain1Dec");
        put(61,"ain1StopDec");
        put(62,"ain2Inc");
        put(63,"ain2StopInc");
        put(64,"ain2Dec");
        put(65,"ain2StopDec");
        put(66,"signalInterference");
        put(67,"interferenceRecovery");
        put(68,"externalPowerDisconnected");
        put(69,"externalPowerRecovery");

        put(70,"overSpeed");
        put(71,"trailer");
        put(72,"72");
        put(73,"googleInstruction");
        put(74,"antitheft");
        put(75,"externalPowerLower");
        put(76,"76");
        put(77,"77");
        put(78,"78");
        put(79,"79");

    }};

//    public static byte[] getAlarmDataWriteContent(AlarmData alarmData){
//        byte[] content = new byte[2];
//        content[0] = (byte)alarmData.getAlarmIndex();
//        content[1] = (alarmData.isOpen() ? (byte)0x01 : 0x00);
//        return content;
//    }
//
//    public static byte[] getBleSensorWriteContent(BleSensorData bleSensorData){
//        byte[] bleMac = MyByteUtils.hexString2Bytes(bleSensorData.getMac());
//        byte[] content;
//        if (bleSensorData.getType() == 5){
//            content = new byte[5];
//            content[0] = (byte)bleSensorData.getType();
//            for(int i = 1;i < 5;i++){
//                content[i] = bleMac[i - 1];
//            }
//        }else{
//            content = new byte[7];
//            content[0] = (byte)bleSensorData.getType();
//            for(int i = 1;i < 7;i++){
//                content[i] = bleMac[i - 1];
//            }
//        }
//        return content;
//    }

    public static Integer[] getSupportAlarms(String protocol){
        if (protocol.equals("tc008")){
            return tc008SupportAlarm;
        }else if(protocol.equals("tc009")){
            return tc009SupportAlarm;
        }
        return new Integer[]{};
    }

//    public static String getFuncStr(String funcStr, Context context){
//        Integer func = null;
//        Integer funcIndex = null;
//        String[] funcAndIndex = funcStr.split("_");
//        String newCmdStr;
//        if (funcAndIndex.length == 2){
//            func = Integer.valueOf(funcAndIndex[0]);
//            funcIndex = Integer.valueOf(funcAndIndex[1]);
//            String cmdStr = Utils.controlFunc.get(func);
//            newCmdStr = cmdStr + "_" + funcIndex;
//        }else{
//            func = Integer.valueOf(funcStr);
//            String cmdStr = Utils.controlFunc.get(func);
//            newCmdStr = cmdStr;
//        }
//        int id = Utils.getResId(newCmdStr,R.string.class);
//        if (id != -1){
//            return context.getString(id);
//        }else{
//            id = Utils.getResId(newCmdStr+"_0",R.string.class);
//            if(id != -1){
//                return context.getString(id);
//            }
//            return newCmdStr;
//        }
//    }

    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
//            e.printStackTrace();
            return -1;
        }
    }
    public static boolean isDomainOrPortSet(Integer func){
        if (func.equals(12031) || func.equals(12032) || func.equals(12033)| func.equals(12039)){
            return true;
        }
        return false;
    }
    public static byte[] getDomainByte(boolean isIpModeBool,String domain){
        if (domain.length() == 0){
            return new byte[]{0x00,0x00,0x00,0x00};
        }
        if (isIpModeBool){
            String[] ipSplit = domain.split("\\.");
            byte[] content = new byte[]{0x00,0x00,0x00,0x00};
            for(int i = 0;i < ipSplit.length;i++){
                content[i] = (byte)Integer.valueOf(ipSplit[i]).intValue();
            }
            return content;
        }else{
            return domain.getBytes();
        }
    }
    public static boolean isIpMode(String domain){
        if (domain == null || domain.length() <= 0){
            return false;
        }
        String[] ipSplit = domain.split("\\.");
        if (ipSplit.length != 4){
            return false;
        }
        for(String ipSplitItem : ipSplit){
            if (isNumeric(ipSplitItem)){
                continue;
            }else{
                return false;
            }
        }
        return true;
    }
    public static boolean isNumeric(String str) {
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;//异常 说明包含非数字。
        }
        return true;
    }
    public static byte[] getReadCmdContent(int cmdCode,byte[] content){
        byte[] cmdByte = MyByteUtils.short2Bytes(cmdCode);
        if (content != null && content.length > 0){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(new byte[]{0x20,cmdByte[0],cmdByte[1]});
                outputStream.write(content.length);
                outputStream.write(content);
                return outputStream.toByteArray();
            }catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new byte[]{0x20,cmdByte[0],cmdByte[1]};
    }

    public static byte[] getAllDataBytes(ArrayList<BleRespData> bleRespDataList){
        for(int i = 0;i < bleRespDataList.size();i++){
            for(int j = 0;j < bleRespDataList.size() - 1;j++){
                BleRespData temp1 = bleRespDataList.get(i);
                BleRespData temp2 = bleRespDataList.get(j);
                if (temp1.getIndex() < temp2.getIndex()){
                    BleRespData temp = temp2;
                    bleRespDataList.set(j,temp1);
                    bleRespDataList.set(i,temp);
                }
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            for(int i = 0;i < bleRespDataList.size();i++){
                outputStream.write(bleRespDataList.get(i).getData());
            }
            return outputStream.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new byte[]{};
    }

    public static ArrayList<BleRespData> parseRespContent(byte[] content){
        ArrayList<BleRespData> result = new ArrayList<>();
        if (content.length < 4){
            return result;
        }
        int index = 0;
        while (index < content.length){
            byte head = content[index];
            int controlCode = MyByteUtils.bytes2Short(content,index+1);
            int len = content[index + 3] & 0x7f;
            int type = (head & 0x40) == 0x40 ? BleRespData.WRITE_TYPE : BleRespData.READ_TYPE;
            boolean isEnd = (head & 0x20) == 0x20;
            int serialNo = head & 0xf;
            BleRespData bleRespData = new BleRespData();
            bleRespData.setControlCode(controlCode);
            bleRespData.setType(type);
            bleRespData.setEnd(isEnd);
            bleRespData.setIndex(serialNo);
            if ((content[index + 3] & 0x80) == 0x80) {
                bleRespData.setType(BleRespData.ERROR_TYPE);
                bleRespData.setErrorCode((int)(content[index + 3] & 0x7f));
                result.add(bleRespData);
                index +=  4;
            }else{
                if (index + 4 + len <= content.length){
                    byte[] data = Arrays.copyOfRange(content,index+4,index+4+len);
                    index += len + 4;
                    bleRespData.setData(data);
                    result.add(bleRespData);
                }else{
                    break;
                }

            }
        }
        return result;
    }

    public static ArrayList<byte[]> getWriteCmdContent(int cmdCode,byte[] content,String pwd){
        ArrayList<byte[]> result = new ArrayList<>();
        byte[] cmdByte = MyByteUtils.short2Bytes(cmdCode);
        int len  = content.length;
        int count = (len / 16) + (len % 16 == 0 ? 0 : 1);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            if (content.length > 0){
                for(int i = 0;i < count;i++){
                    int copyEnd = (i + 1) * 16 > len ? len : (i+1)* 16;
                    byte[] contentItem = Arrays.copyOfRange(content,i * 16,copyEnd);
                    byte cmdHead = 0x60;
                    if (i == count - 1){
                        cmdHead = (byte) (0x60 | i);
                    }else{
                        cmdHead = (byte) (0x40 | i);
                    }
                    outputStream.reset();
                    outputStream.write(cmdHead);
                    outputStream.write(cmdByte);
                    if(pwd != null && pwd.trim().length() > 0 && i == 0){
                        outputStream.write(pwd.trim().getBytes());
                    }
                    outputStream.write(contentItem.length);
                    outputStream.write(contentItem, 0, contentItem.length);
                    byte[] resultItem = outputStream.toByteArray();
                    result.add(resultItem);
                }
            }else{
                result.add(new byte[]{0x60,cmdByte[0],cmdByte[1]});
            }
            return result;
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
