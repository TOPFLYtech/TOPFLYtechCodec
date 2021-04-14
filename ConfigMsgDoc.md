# How to use the class library to send configuration commands
## 1. Encode the string to be sent

&#8195;&#8195;When we need to send commands to the device through the server, we need to encode the string commands that need to be sent. For friends who use class libraries, you can directly use the methods of class libraries. For friends who do not use class libraries, you need to pay attention to the encoding of strings, and you need to use UTF-16LE encoding.

&#8195;&#8195;For example, send the VERSION# command.

&#8195;&#8195;First, you select the encoder first. If your protocol starts with 25, choose T880xPlusEncoder. If your protocol starts with 26, choose T880xdEncoder. If it is an agreement beginning with 27, select PersonalAssetMsgEncoder. Let's use 25 protocol as an example.
>Java:
```
T880xPlusEncoder t880xPlusEncoder = new T880xPlusEncoder(MessageEncryptType.NONE,"aes key");
byte[] needWriteToDevice = t880xPlusEncoder.getConfigSettingMsg(imei, "VERSION#");
```
>C#:
```
TopflytechCodec.T880xPlusEncoder t880xPlusEncoder = new TopflytechCodec.T880xPlusEncoder(MessageEncryptType.NONE, "aes key");
byte[] needWriteToDevice = t880xPlusEncoder.getConfigSettingMsg(imei, "VERSION#");
```
>Python:
```
t880xPlusEncoder = T880xPlusEncoder(MessageEncryptType.NONE,"aes key")
needWriteToDevice = t880xdEncoder.getConfigSettingMsg(imei,"VERSION#")
```
&#8195;&#8195;You will get the character array as follows: [25 25 81 00 10 00 01 08 65 28 40 41 14 62 04 01 56 45 52 53 49 4f 4e 23]

## 2. Analyze the information returned by the device into a string

&#8195;&#8195; The device will reply after receiving the instruction sent by the server. If you use the class library we provide, it will receive a ConfigMessage object. Among them, configResultContent is the reply of the device. For example, after sending VERSION# to the tlw1-10a device, the configResultContent received is"imei:865284041146204,set version ok,version:basic:v1.0.3,app:v4.5.5,build:2021-03-13,14:46:09,plt:2503ave,hw:v2.2,model:tlw1-10a,modem:bg96mar04a02m1g_01.002.02.003#".Then you can get the response result by parsing the string.

&#8195;&#8195; of course,You will get the character array from device as follows: [25 25 81 01 5a 00 01 08 65 28 40 41 14 62 04 01 49 00 4d 00 45 00 49 00 3a 00 38 00 36 00 35 00 32 00 38 00 34 00 30 00 34 00 31 00 31 00 34 00 36 00 32 00 30 00 34 00 2c 00 53 00 45 00 54 00 20 00 56 00 45 00 52 00 53 00 49 00 4f 00 4e 00 20 00 4f 00 4b 00 2c 00 56 00 65 00 72 00 73 00 69 00 6f 00 6e 00 3a 00 42 00 61 00 73 00 69 00 63 00 3a 00 56 00 31 00 2e 00 30 00 2e 00 33 00 2c 00 41 00 50 00 50 00 3a 00 56 00 34 00 2e 00 35 00 2e 00 35 00 2c 00 42 00 55 00 49 00 4c 00 44 00 3a 00 32 00 30 00 32 00 31 00 2d 00 30 00 33 00 2d 00 31 00 33 00 2c 00 31 00 34 00 3a 00 34 00 36 00 3a 00 30 00 39 00 2c 00 50 00 4c 00 54 00 3a 00 32 00 35 00 30 00 33 00 41 00 56 00 45 00 2c 00 48 00 57 00 3a 00 56 00 32 00 2e 00 32 00 2c 00 4d 00 4f 00 44 00 45 00 4c 00 3a 00 54 00 4c 00 57 00 31 00 2d 00 31 00 30 00 41 00 2c 00 4d 00 4f 00 44 00 45 00 4d 00 3a 00 42 00 47 00 39 00 36 00 4d 00 41 00 52 00 30 00 34 00 41 00 30 00 32 00 4d 00 31 00 47 00 5f 00 30 00 31 00 2e 00 30 00 30 00 32 00 2e 00 30 00 32 00 2e 00 30 00 30 00 33 00 23 00]