using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using TopflytechCodec.Entities;

namespace TopflytechCodec
{
    public class PersonalAssetMsgDecoder
    {
        private static int HEADER_LENGTH = 3;

        private static byte[] SIGNUP = { 0x27, 0x27, 0x01 };

        private static byte[] DATA = { 0x27, 0x27, 0x02 };

        private static byte[] HEARTBEAT = { 0x27, 0x27, 0x03 };

        private static byte[] ALARM = { 0x27, 0x27, 0x04 };
        private static byte[] CONFIG = { 0x27, 0x27, (byte)0x81 };

        private static byte[] NETWORK_INFO_DATA = { 0x27, 0x27, 0x05 };

        private int encryptType = 0;
        private String aesKey;
        public PersonalAssetMsgDecoder(int messageEncryptType, String aesKey)
        {
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
        }

        private static bool match(byte[] bytes)
        {
            return Utils.ArrayEquals(SIGNUP, bytes)
                    || Utils.ArrayEquals(HEARTBEAT, bytes)
                    || Utils.ArrayEquals(DATA, bytes)
                    || Utils.ArrayEquals(ALARM, bytes)
                    || Utils.ArrayEquals(CONFIG , bytes)
                    || Utils.ArrayEquals(NETWORK_INFO_DATA, bytes);
        }
        private TopflytechByteBuf decoderBuf = new TopflytechByteBuf();


        public List<Message> decode(byte[] buf)
        {
            decoderBuf.PutBuf(buf);
            List<Message> messages = new List<Message>();
            if (decoderBuf.GetReadableBytes() < (HEADER_LENGTH + 2))
            {
                return messages;
            }
            byte[] bytes = new byte[3];
            while (decoderBuf.GetReadableBytes() > 5)
            {
                decoderBuf.MarkReaderIndex();
                bytes[0] = decoderBuf.GetByte(0);
                bytes[1] = decoderBuf.GetByte(1);
                bytes[2] = decoderBuf.GetByte(2);
                if (match(bytes))
                {
                    decoderBuf.SkipBytes(HEADER_LENGTH);
                    byte[] lengthBytes = decoderBuf.ReadBytes(2);
                    int packageLength = BytesUtils.Bytes2Short(lengthBytes, 0);
                    if (encryptType == MessageEncryptType.MD5)
                    {
                        packageLength = packageLength + 8;
                    }
                    else if (encryptType == MessageEncryptType.AES)
                    {
                        packageLength = Crypto.GetAesLength(packageLength);
                    }
                    decoderBuf.ResetReaderIndex();
                    if (packageLength > decoderBuf.GetReadableBytes())
                    {
                        break;
                    }
                    byte[] data = decoderBuf.ReadBytes(packageLength);
                    data = Crypto.DecryptData(data, encryptType, aesKey);
                    Message message = build(data);
                    if (message != null)
                    {
                        messages.Add(message);
                    }

                }
                else
                {
                    decoderBuf.SkipBytes(1);
                }
            }
            return messages;
        }

        public Message build(byte[] bytes){
        if (bytes != null && bytes.Length > HEADER_LENGTH
                && (bytes[0] == 0x27 && bytes[1] == 0x27)) {
            switch (bytes[2]) {
                case 0x01:
                    SignInMessage signInMessage = parseLoginMessage(bytes);
                    return signInMessage;
                case 0x03:
                    HeartbeatMessage heartbeatMessage = parseHeartbeat(bytes);
                    return heartbeatMessage;
                case 0x02:
                case 0x04:
                    LocationMessage locationMessage = parseDataMessage(bytes);
                    return locationMessage;
                case 0x05:
                    NetworkInfoMessage networkInfoMessage = parseNetworkInfoMessage(bytes);
                    return networkInfoMessage;
                case (byte)0x81:
                    Message message =  parseInteractMessage(bytes);
                    return message;
                default:
                    return null;
            }
        }
        return null;
    }


        private LocationMessage parseDataMessage(byte[] data) {
        int serialNo = BytesUtils.Bytes2Short(data, 5);
        String imei = BytesUtils.IMEI.Decode(data, 7);
        DateTime date = Utils.getGTM0Date(data, 17);
        bool isGpsWorking = (data[15] & 0x20) == 0x00;
        bool isHistoryData = (data[15] & 0x80) != 0x00;
        bool latlngValid = (data[15] & 0x40) == 0x40;
        int satelliteNumber = data[15] & 0x1F;
        double altitude = latlngValid ? BytesUtils.Bytes2Float(data,23) : 0;
        double longitude = latlngValid ? BytesUtils.Bytes2Float(data,27) : 0;
        double latitude = latlngValid ? BytesUtils.Bytes2Float(data,31) : 0;
        float speedf = 0.0f;
        if (latlngValid)
        {
            byte[] bytesSpeed = Utils.ArrayCopyOfRange(data, 35, 37);
            String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
            speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));
        }
        int azimuth = latlngValid ? BytesUtils.Bytes2Short(data, 37) : 0;
        int axisXDirect = (data[39] & 0x80) == 0x80 ? 1 : -1;
        float axisX = ((data[39] & 0x7F & 0xff) + (((data[40] & 0xf0) >> 4) & 0xff) /10.0f) * axisXDirect;

        int axisYDirect = (data[40] & 0x08) == 0x08 ? 1 : -1;
        float axisY = (((((data[40] & 0x07) << 4) & 0xff) + (((data[41] & 0xf0) >> 4) & 0xff)) + (data[41] & 0x0F & 0xff)/10.0f)* axisYDirect;

        int axisZDirect = (data[42] & 0x80) == 0x80 ? 1 : -1;
        float axisZ = ((data[42] & 0x7F & 0xff) + (((data[43] & 0xf0) >> 4) & 0xff) /10.0f) * axisZDirect;

        byte[] batteryPercentBytes = new byte[]{data[44]};
        String batteryPercentStr = BytesUtils.Bytes2HexString(batteryPercentBytes, 0);
        int batteryPercent = 100;
        if(batteryPercentStr.ToLower().Equals("ff")){
            batteryPercent = 100;
        }else{
            batteryPercent = Convert.ToInt32(batteryPercentStr);
            if (0 == batteryPercent)
            {
                batteryPercent = 100;
            }
        }

        float deviceTemp = (data[45] & 0x7F) * ((data[45] & 0x80) == 0x80 ? -1 : 1);
        byte[] lightSensorBytes = new byte[]{data[46]};
        String lightSensorStr = BytesUtils.Bytes2HexString(lightSensorBytes, 0);
        float lightSensor = 0;
        if (lightSensorStr.ToLower().Equals("ff"))
        {
            lightSensor = -1;
        }else{
            lightSensor = Convert.ToInt32(lightSensorStr) / 10.0f;

        }

        byte[] batteryVoltageBytes = new byte[]{data[47]};
        String batteryVoltageStr = BytesUtils.Bytes2HexString(batteryVoltageBytes, 0);
        float batteryVoltage = 0;
        if(batteryVoltageStr.ToLower().Equals("ff")){
            batteryVoltage = -1;
        }else{
            batteryVoltage = Convert.ToInt32(batteryVoltageStr) / 10.0f;
        }
        byte[] solarVoltageBytes = new byte[]{data[48]};
        String solarVoltageStr = BytesUtils.Bytes2HexString(solarVoltageBytes, 0);
        float solarVoltage = 0;
        if(solarVoltageStr.ToLower().Equals("ff")){
            solarVoltage = -1;
        }else{
            solarVoltage = Convert.ToInt32(solarVoltageStr) / 10.0f;
        }

        long mileage = BytesUtils.Byte2Int(data, 49);
        int status = BytesUtils.Bytes2Short(data, 53);
        int network = (status & 0x7F0) >> 4;
        int accOnInterval = BytesUtils.Bytes2Short(data, 55);
        int accOffInterval = BytesUtils.Bytes2Short(data, 57);
        int angleCompensation = (int) data[61];
        int distanceCompensation = BytesUtils.Bytes2Short(data, 62);
        int heartbeatInterval = (int) data[64];
        bool isUsbCharging = (status & 0x8000) == 0x8000;
        bool isSolarCharging = (status & 0x8) == 0x8;
        bool iopIgnition = (status & 0x4) == 0x4;
        byte alarmByte = data[16];
        int originalAlarmCode = (int) alarmByte;
        byte[] command = new byte[HEADER_LENGTH];
        Array.Copy(data, 0, command, 0, HEADER_LENGTH);
        bool isAlarmData = command[2] == 0x04;
        LocationMessage locationMessage;
        if (isAlarmData){
            locationMessage = new LocationAlarmMessage();
        }else {
            locationMessage = new LocationInfoMessage();
        }
        locationMessage.OrignBytes=data;
        locationMessage.SerialNo=serialNo;
        locationMessage.NetworkSignal=network;
        locationMessage.Imei=imei;
        locationMessage.IsSolarCharging=isSolarCharging;
        locationMessage.IsUsbCharging=isUsbCharging;
        locationMessage.SamplingIntervalAccOn=accOnInterval;
        locationMessage.SamplingIntervalAccOff=accOffInterval;
        locationMessage.AngleCompensation=angleCompensation;
        locationMessage.DistanceCompensation=distanceCompensation;
        locationMessage.GpsWorking=isGpsWorking;
        locationMessage.IsHistoryData=isHistoryData;
        locationMessage.SatelliteNumber=satelliteNumber;
        locationMessage.HeartbeatInterval=heartbeatInterval;
        locationMessage.OriginalAlarmCode=originalAlarmCode;
        locationMessage.Alarm=getEvent(alarmByte);
        locationMessage.Mileage=mileage;
        locationMessage.IopIgnition=iopIgnition;
        locationMessage.IOP=iopIgnition ? 0x4000L :0x0000L;
        locationMessage.BatteryCharge=batteryPercent; 
        locationMessage.Date=date;
        locationMessage.LatlngValid=latlngValid;
        locationMessage.Altitude=altitude;
        locationMessage.Latitude=latitude;
        locationMessage.Longitude=longitude;
        if(locationMessage.LatlngValid) {
            locationMessage.Speed=speedf;
        } else {
            locationMessage.Speed=0.0f;
        }
        locationMessage.Azimuth=azimuth;
        locationMessage.AxisX=axisX;
        locationMessage.AxisY=axisY;
        locationMessage.AxisZ=axisZ;
        locationMessage.DeviceTemp=deviceTemp;
        locationMessage.LightSensor=lightSensor;
        locationMessage.BatteryVoltage=batteryVoltage;
        locationMessage.SolarVoltage=solarVoltage;
        return locationMessage;
    }
    private static int getEvent(byte alarmCodeByte) {
        if(alarmCodeByte == 0x01){
            return Event.ALARM_DEVICE_REMOVE;
        }else if(alarmCodeByte == 0x02){
            return Event.ALARM_DEVICE_CASE_OPEN;
        }else if(alarmCodeByte == 0x03){
            return Event.ALARM_SOS;
        }else if(alarmCodeByte == 0x04){
            return Event.ALARM_BOX_OPEN;
        }else if(alarmCodeByte == 0x05){
            return Event.ALARM_FALL_DOWN;
        }else if(alarmCodeByte == 0x06){
            return Event.ALARM_LOW_BATTERY;
        }else if(alarmCodeByte == 0x07){
            return Event.ALARM_BATTERY_POWER_RECOVER;
        }else if(alarmCodeByte == 0x08){
            return Event.ALARM_INNER_TEMP_HIGH;
        }else if(alarmCodeByte == 0x09){
            return Event.ALARM_MOVE;
        }else if(alarmCodeByte == 0x10){
            return Event.ALARM_COLLISION;
        }else if(alarmCodeByte == 0x11){
            return Event.ALARM_INCLINE;
        }else if(alarmCodeByte == 0x12){
            return Event.ALARM_USB_RECHARGE_START;
        }else if(alarmCodeByte == 0x13){
            return Event.ALARM_USB_RECHARGE_END;
        }else if(alarmCodeByte == 0x14){
            return Event.ALARM_GEOFENCE_IN;
        }else if(alarmCodeByte == 0x15){
            return Event.ALARM_GEOFENCE_OUT;
        }else if(alarmCodeByte == 0x16){
            return Event.IGNITION;
        }else if(alarmCodeByte == 0x17){
            return Event.PARKING;
        }else if(alarmCodeByte == 0x18){
            return Event.IDLE_START;
        }else if(alarmCodeByte == 0x19){
            return Event.IDLE_END;
        }else if(alarmCodeByte == 0x20){
            return Event.ADDRESS_REQUESTED;
        }else if(alarmCodeByte == 0x21){
            return Event.ALARM_DEVICE_MOUNTED;
        }else if(alarmCodeByte == 0x22){
            return Event.ALARM_DEVICE_CASE_CLOSED;
        }else if(alarmCodeByte == 0x23){
            return Event.ALARM_BOX_CLOSED;
        }else if(alarmCodeByte == 0x24){
            return Event.ALARM_FALL_DOWN_REC;
        }else if(alarmCodeByte == 0x25){
            return Event.ALARM_INNER_TEMP_HIGH_REC;
        }else if(alarmCodeByte == 0x26){
            return Event.ALARM_MOVE_REC;
        }else if(alarmCodeByte == 0x27){
            return Event.ALARM_COLLISION_REC;
        }else if(alarmCodeByte == 0x28){
            return Event.ALARM_INCLINE_REC;
        }else if(alarmCodeByte == 0x29){
            return Event.ALARM_POWER_ON;
        }else if(alarmCodeByte == 0x30){
            return Event.ALARM_INNER_TEMP_LOW;
        }else if(alarmCodeByte == 0x31){
            return Event.ALARM_INNER_TEMP_LOW_REC;
        }
        return Event.NONE;
    }
    

    private SignInMessage parseLoginMessage(byte[] bytes)  {
        int serialNo = BytesUtils.Bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.Decode(bytes, 7);
        String str = BytesUtils.Bytes2HexString(bytes, 15);
        String software = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 15, 17), 0);
        software = String.Format("V{0}.{1}.{2}", Convert.ToInt32(software.Substring(1, 1), 16), Convert.ToInt32(software.Substring(2, 1), 16), Convert.ToInt32(software.Substring(3, 1), 16));
        String firmware = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 20, 22), 0);
        firmware = String.Format("V{0}.{1}.{2}", firmware.Substring(0, 1), firmware.Substring(1, 1), firmware.Substring(2, 2));
        String hardware = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 22, 23), 0);
        hardware = String.Format("V{0}.{1}", hardware.Substring(0, 1), hardware.Substring(1, 1));
        SignInMessage signInMessage = new SignInMessage();
        signInMessage.SerialNo=serialNo;
        signInMessage.Imei=imei;
        signInMessage.Software=software;
        signInMessage.Firmware=firmware;
        signInMessage.Hareware=hardware;
        signInMessage.OrignBytes=bytes;
        return signInMessage;
    }
    private HeartbeatMessage parseHeartbeat(byte[] bytes)
    {
        int serialNo = BytesUtils.Bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.Decode(bytes, 7);
        HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
        heartbeatMessage.OrignBytes = bytes;
        heartbeatMessage.SerialNo = serialNo;
        heartbeatMessage.Imei = imei;
        return heartbeatMessage;
    }

    private NetworkInfoMessage parseNetworkInfoMessage(byte[] bytes)
    {
        NetworkInfoMessage networkInfoMessage = new NetworkInfoMessage();
        int serialNo = BytesUtils.Bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.Decode(bytes, 7);
        DateTime gmt0 = Utils.getGTM0Date(bytes, 15);
        int networkOperatorLen = bytes[21];
        int networkOperatorStartIndex = 22;
        byte[] networkOperatorByte = Utils.ArrayCopyOfRange(bytes, networkOperatorStartIndex, networkOperatorStartIndex + networkOperatorLen);
        String networkOperator = Encoding.GetEncoding("UTF-16LE").GetString(networkOperatorByte); ;
        int accessTechnologyLen = bytes[networkOperatorStartIndex + networkOperatorLen];
        int accessTechnologyStartIndex = networkOperatorStartIndex + networkOperatorLen + 1;
        byte[] accessTechnologyByte = Utils.ArrayCopyOfRange(bytes, accessTechnologyStartIndex, accessTechnologyStartIndex + accessTechnologyLen);
        String accessTechnology = System.Text.Encoding.UTF8.GetString(accessTechnologyByte);
        int bandLen = bytes[accessTechnologyStartIndex + accessTechnologyLen];
        int bandStartIndex = accessTechnologyStartIndex + accessTechnologyLen + 1;
        byte[] bandLenByte = Utils.ArrayCopyOfRange(bytes, bandStartIndex, bandStartIndex + bandLen);
        String band = System.Text.Encoding.UTF8.GetString(bandLenByte);
        int msgLen = BytesUtils.Bytes2Short(bytes, 3);
        if (msgLen > bandStartIndex + bandLen)
        {
            int IMSILen = bytes[bandStartIndex + bandLen];
            int IMSIStartIndex = bandStartIndex + bandLen + 1;
            byte[] IMSILenByte = Utils.ArrayCopyOfRange(bytes, IMSIStartIndex, IMSIStartIndex + IMSILen);
            String IMSI = System.Text.Encoding.UTF8.GetString(IMSILenByte);
            networkInfoMessage.Imsi = IMSI;
            if (msgLen > IMSIStartIndex + IMSILen)
            {
                int iccidLen = bytes[IMSIStartIndex + IMSILen];
                int iccidStartIndex = IMSIStartIndex + IMSILen + 1;
                byte[] iccidLenByte = Utils.ArrayCopyOfRange(bytes, iccidStartIndex, iccidStartIndex + iccidLen);
                String iccid = System.Text.Encoding.UTF8.GetString(iccidLenByte);
                networkInfoMessage.Iccid = iccid;
            }
        }
        networkInfoMessage.SerialNo = serialNo;
        networkInfoMessage.Imei = imei;
        networkInfoMessage.OrignBytes = bytes;
        networkInfoMessage.Date = gmt0;
        networkInfoMessage.AccessTechnology = accessTechnology;
        networkInfoMessage.NetworkOperator = networkOperator;
        networkInfoMessage.Band = band;

        return networkInfoMessage;
    }

    private Message parseInteractMessage(byte[] bytes)
    {
        int serialNo = BytesUtils.Bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.Decode(bytes, 7);
        byte protocol = bytes[15];
        byte[] data = new byte[bytes.Length - 16];
        Array.Copy(bytes, 16, data, 0, bytes.Length - 16);
        String messageData;
        switch (protocol)
        {
            case 0x01:
                {
                    ConfigMessage configMessage = new ConfigMessage();
                    messageData = Encoding.GetEncoding("UTF-16LE").GetString(data);
                    configMessage.ConfigResultContent = messageData;
                    configMessage.OrignBytes = bytes;
                    configMessage.SerialNo = serialNo;
                    configMessage.Imei = imei;
                    return configMessage;
                }
            case 0x03:
                {
                    ForwardMessage forwardMessage = new ForwardMessage();
                    messageData = Encoding.GetEncoding("UTF-16LE").GetString(data);
                    forwardMessage.Content = messageData;
                    forwardMessage.OrignBytes = bytes;
                    forwardMessage.SerialNo = serialNo;
                    forwardMessage.Imei = imei;
                    return forwardMessage;
                }
            case 0x05:
                {
                    USSDMessage ussdMessage = new USSDMessage();
                    messageData = Encoding.GetEncoding("UTF-16lE").GetString(data);
                    ussdMessage.Content = messageData;
                    ussdMessage.OrignBytes = bytes;
                    ussdMessage.SerialNo = serialNo;
                    ussdMessage.Imei = imei;
                    return ussdMessage;
                }
            default:
                throw new Exception("Error config message");
        }

    }

    }
}
