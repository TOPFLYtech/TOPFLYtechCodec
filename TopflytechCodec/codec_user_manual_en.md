# TOPFLYtechCodec Codec User Manual (Java / Node / Python / C#)

## 1. Scope and Protocol Families

This manual covers all codec implementations in this repository:

- Java: `Java/topflytechCodec`
- Node: `nodeTOPFLYtechCodec`
- Python (3.9): `pythonTopflytechCodec-3.9`
- C#: `C#/TopflytechCodec/TopflytechCodec`

Protocol families and typical device models:

- `2525` (Vehicle Tracking Protocol)
  - Typical models: `PioneerX 100`, `PioneerX 101`, `TLW2-6BL`, etc.
- `2626` (OBD CAN Protocol)
  - Typical models: `TorchX 100`, `TorchX 110`, `TorchX 310`, `TLD2-D`, etc.
- `2727` (Asset Tracking Protocol)
  - Typical models: `SolarX 110`, `SolarGuardX 110`, etc.
- `2828` (PTT protocol, appendix)

## 2. Protocol Number Definition (Most Important for Customer Troubleshooting)

Unified definition: `Protocol Number = Protocol Header (2 bytes) + Message Type (1 byte)`, represented as a 6-digit hex string.

Examples:

- `252512` = header `0x25 0x25` + message type `0x12`
- `262602` = header `0x26 0x26` + message type `0x02`
- `272724` = header `0x27 0x27` + message type `0x24`

How to extract from raw frame HEX:

1. Byte 1~2: protocol header (`2525/2626/2727/2828`)
2. Byte 3: message type (e.g., `01/02/12/...`)
3. Concatenate as protocol number (e.g., `252512`)

## 3. Class Definitions and Layered Structure

## 3.1 Entity Base Class (Java as reference, other languages follow the same model)

Core base class: `com.topflytech.codec.entities.Message`

Common fields:

- `imei`
- `serialNo`
- `orignBytes`
- `isNeedResp`
- `protocolHeadType`
- `encryptType`
- extended transport fields: `protocol`, `linkType`, `recvDate`, `postResp`

Common derived message classes:

- `SignInMessage`
- `HeartbeatMessage`
- `LocationInfoMessage` / `LocationAlarmMessage` (base: `LocationMessage`)
- `GpsDriverBehaviorMessage`
- `AccelerationDriverBehaviorMessage`
- `AccidentAccelerationMessage`
- `RS232Message` / `RS485Message` / `OneWireMessage`
- `ObdMessage`
- `BluetoothPeripheralDataMessage`
- `NetworkInfoMessage`
- `WifiMessage` / `WifiWithDeviceInfoMessage`
- `LockMessage` / `SubLockMessage`
- `InnerGeoDataMessage`
- `DeviceTempCollectionMessage`

## 3.2 Decoder Classes and Protocol Binding

Java (same responsibilities in other languages):

- `Decoder`: `2525` (also handles some `2323/2525` vehicle frames)
- `ObdDecoder`: `2626`
- `PersonalAssetMsgDecoder`: `2727`
- `PTTDecoder`: `2828`

Matching encoders:

- `T880xEncoder` / `T880xPlusEncoder` / `T880xdEncoder`: wrappers for vehicle/OBD replies
- `PersonalAssetMsgEncoder`: wrapper for asset protocol replies
- `PTTEncoder`: wrapper for PTT protocol replies
- low-level common encoder: `Encoder`

Node equivalents:

- Decoders: `Decoder.js`, `ObdDecoder.js`, `PersonalAssetDecoder.js`, `PTTDecoder.js`
- Encoders: `Encoder.js`, `ObdEncoder.js`, `PersonalAssetEncoder.js`, `PTTEncoder.js`

Python equivalents (`TopflytechCodec.py`):

- Decoders: `Decoder`, `ObdDecoder`, `PersonalAssetMsgDecoder`, `PTTDecoder`
- Encoders: `T880xEncoder`, `T880xPlusEncoder`, `T880xdEncoder`, `PersonalAssetMsgEncoder`, `PTTEncoder`

C# equivalents:

- Decoders: `Decoder.cs`, `ObdDecoder.cs`, `PersonalAssetMsgDecoder.cs`, `PTTDecoder.cs`
- Encoders: `T880XEncoder.cs`, `T880xPlusEncoder.cs`, `T880xdEncoder.cs`, `PersonalAssetMsgEncoder.cs`, `PTTEncoder.cs`

## 4. Entry Points and Calling Methods by Language

## 4.1 Java

```java
Decoder decoder2525 = new Decoder(MessageEncryptType.NONE, "");
ObdDecoder decoder2626 = new ObdDecoder(MessageEncryptType.NONE, "");
PersonalAssetMsgDecoder decoder2727 = new PersonalAssetMsgDecoder(MessageEncryptType.NONE, "");

List<Message> list = decoder2525.decode(rawBytes);
for (Message m : list) {
    int protocolHeadType = m.getProtocolHeadType();
    String imei = m.getImei();
}
```

Encoding reply (example):

```java
T880xPlusEncoder enc2525 = new T880xPlusEncoder(MessageEncryptType.NONE, "");
byte[] reply = enc2525.getLocationAlarmMsgReply(msg.getImei(), true, msg.getSerialNo(),
        msg.getOriginalAlarmCode(), msg.getProtocolHeadType());
```

## 4.2 Node.js

```javascript
const Decoder = require('./Decoder');
const ObdDecoder = require('./ObdDecoder');
const PersonalAssetDecoder = require('./PersonalAssetDecoder');

Decoder.encryptType = 0; // NONE
Decoder.aesKey = "";
const list = Decoder.decode(buffer2525);
```

Encoding reply (example):

```javascript
const Encoder = require('./Encoder');
const reply = Encoder.getLocationAlarmMsgReply(imei, true, serialNo, alarmCode, protocolHeadType, 0, "");
```

## 4.3 Python 3.9

```python
from TopflytechCodec import *

decoder2525 = Decoder(MessageEncryptType.NONE, "")
decoder2626 = ObdDecoder(MessageEncryptType.NONE, "")
decoder2727 = PersonalAssetMsgDecoder(MessageEncryptType.NONE, "")

messages = decoder2525.decode(raw_bytes)
for m in messages:
    print(m.imei, m.protocolHeadType)
```

Encoding reply (example):

```python
enc = T880xPlusEncoder(MessageEncryptType.NONE, "")
reply = enc.getLocationAlarmMsgReply(msg.imei, True, msg.serialNo, msg.originalAlarmCode, msg.protocolHeadType)
```

## 4.4 C#

```csharp
var decoder2525 = new Decoder(MessageEncryptType.NONE, "");
var decoder2626 = new ObdDecoder(MessageEncryptType.NONE, "");
var decoder2727 = new PersonalAssetMsgDecoder(MessageEncryptType.NONE, "");

List<Message> messages = decoder2525.decode(rawBytes);
foreach (var m in messages) {
    var imei = m.Imei;
    var protocolHeadType = m.ProtocolHeadType;
}
```

Encoding reply (example):

```csharp
var enc = new T880xPlusEncoder(MessageEncryptType.NONE, "");
byte[] reply = enc.getLocationAlarmMsgReply(msg.Imei, true, msg.SerialNo, msg.OriginalAlarmCode, msg.ProtocolHeadType);
```

## 5. Protocol Number -> Decoded Data Type Mapping (Core Section)

The following mapping is based on the latest Java implementation and should be aligned by all other languages.

Notes:

- `Message(dynamic)` means the frame is further dispatched by content.
- `252581/262681/272781` are interactive/config containers; common child messages: `ConfigMessage`, `ForwardMessage`, `USSDMessage`.

## 5.1 `2525` (Vehicle Tracking Protocol)

| Protocol No. | Message Type | Decoded Object |
|---|---|---|
| `252501` | Sign-in | `SignInMessage` |
| `252531` | Secondary sign-in | `SignInMessage` |
| `252503` | Heartbeat | `HeartbeatMessage` |
| `252502` | Location | `LocationInfoMessage/LocationAlarmMessage` (via `parseDataMessage`) |
| `252504` | Alarm location | `LocationAlarmMessage` (via `parseDataMessage`) |
| `252516` | Sensor location | `LocationMessage` |
| `252518` | Sensor alarm location | `LocationMessage` |
| `252505` | GPS driver behavior | `GpsDriverBehaviorMessage` |
| `252506` | Acceleration driver behavior | `AccelerationDriverBehaviorMessage` |
| `252507` | Crash acceleration | `AccidentAccelerationMessage` |
| `252509` | RS232 | `RS232Message` |
| `252510` | Bluetooth data | `BluetoothPeripheralDataMessage` |
| `252511` | Network info | `NetworkInfoMessage` |
| `252512` | 2nd-gen Bluetooth data | `BluetoothPeripheralDataMessage` |
| `252513` | 2nd-gen location | `LocationMessage` (`parseSecondDataMessage`) |
| `252514` | 2nd-gen alarm location | `LocationMessage` (`parseSecondDataMessage`) |
| `252533` | 2nd-gen sensor location | `LocationMessage` (`parseSecondDataMessage`) |
| `252534` | 2nd-gen sensor alarm location | `LocationMessage` (`parseSecondDataMessage`) |
| `252515` | WiFi | `WifiMessage` |
| `252521` | RS485 | `RS485Message` |
| `252522` | OBD passthrough | `ObdMessage` |
| `252523` | OneWire | `OneWireMessage` |
| `252540` | Debug | `DebugMessage` |
| `252544` | Manual CAN | `ManualCANMessage` |
| `252562` | New encrypted indefinite location | `Message(dynamic, DecoderHelper.parseLocationMessage)` |
| `252564` | New encrypted indefinite alarm | `Message(dynamic, DecoderHelper.parseLocationMessage)` |
| `252581` | Interactive/config | `Message(dynamic, parseInteractMessage)` |

## 5.2 `2626` (OBD CAN Protocol)

| Protocol No. | Message Type | Decoded Object |
|---|---|---|
| `262601` | Sign-in | `SignInMessage` |
| `262631` | Secondary sign-in | `SignInMessage` |
| `262603` | Heartbeat | `HeartbeatMessage` |
| `262602` | Location | `LocationInfoMessage/LocationAlarmMessage` |
| `262604` | Alarm location | `LocationAlarmMessage` |
| `262616` | Sensor location | `LocationMessage` |
| `262618` | Sensor alarm location | `LocationMessage` |
| `262605` | GPS driver behavior | `GpsDriverBehaviorMessage` |
| `262606` | Acceleration driver behavior | `AccelerationDriverBehaviorMessage` |
| `262607` | Crash acceleration | `AccidentAccelerationMessage` |
| `262609` | OBD data | `ObdMessage` |
| `262610` | Bluetooth data | `BluetoothPeripheralDataMessage` |
| `262611` | Network info | `NetworkInfoMessage` |
| `262612` | 2nd-gen Bluetooth data | `BluetoothPeripheralDataMessage` |
| `262615` | WiFi | `WifiMessage` |
| `262662` | New encrypted indefinite location | `Message(dynamic, DecoderHelper.parseLocationMessage)` |
| `262664` | New encrypted indefinite alarm | `Message(dynamic, DecoderHelper.parseLocationMessage)` |
| `262681` | Interactive/config | `Message(dynamic, parseInteractMessage)` |

## 5.3 `2727` (Asset Tracking Protocol)

| Protocol No. | Message Type | Decoded Object |
|---|---|---|
| `272701` | Sign-in | `SignInMessage` |
| `272731` | Secondary sign-in | `SignInMessage` |
| `272703` | Heartbeat | `HeartbeatMessage` |
| `272702` | Location | `LocationInfoMessage/LocationAlarmMessage` |
| `272704` | Alarm location | `LocationAlarmMessage` |
| `272705` | Network info | `NetworkInfoMessage` |
| `272710` | Bluetooth data | `BluetoothPeripheralDataMessage` |
| `272712` | 2nd-gen Bluetooth data | `BluetoothPeripheralDataMessage` |
| `272715` | WiFi | `WifiMessage` |
| `272717` | Main lock data | `LockMessage` |
| `272727` | Sub-lock data | `SubLockMessage` |
| `272720` | Inner geofence data | `InnerGeoDataMessage` |
| `272724` | WiFi + device info | `Message(dynamic, parseWifiWithDeviceInfoMessage)` |
| `272725` | WiFi + device info | `Message(dynamic, parseWifiWithDeviceInfoMessage)` |
| `272726` | Device temp collection batch | `DeviceTempCollectionMessage` |
| `272762` | New encrypted indefinite location | `Message(dynamic, DecoderHelper.parseLocationMessage)` |
| `272764` | New encrypted indefinite alarm | `Message(dynamic, DecoderHelper.parseLocationMessage)` |
| `272781` | Interactive/config | `Message(dynamic, parseInteractMessage)` |

## 6. Encoder API and Protocol Labels

## 6.1 Encoders for `2525`

- Java: `T880xEncoder` / `T880xPlusEncoder`
- Node: `Encoder.js`
- Python: `T880xEncoder` / `T880xPlusEncoder`
- C#: `T880XEncoder` / `T880xPlusEncoder`

Common reply methods:

- `getSignInMsgReply`
- `getHeartbeatMsgReply`
- `getLocationMsgReply`
- `getLocationAlarmMsgReply`
- `getGpsDriverBehaviorMsgReply`
- `getAccelerationDriverBehaviorMsgReply`
- `getAccelerationAlarmMsgReply`
- `getBluetoothPeripheralMsgReply`
- `getRS232MsgReply`
- `getNetworkMsgReply`
- `getWifiMsgReply`
- `getRs485MsgReply`
- `getOneWireMsgReply`
- `getObdMsgReply`

## 6.2 Encoders for `2626`

- Java: `T880xdEncoder`
- Node: `ObdEncoder.js`
- Python: `T880xdEncoder`
- C#: `T880xdEncoder`

OBD-focused methods:

- `getObdMsgReply`
- `getObdConfigSettingMsg`
- `getClearObdErrorCodeMsg`
- `getObdVinMsg`
- plus common heartbeat/location/bluetooth/network reply methods

## 6.3 Encoders for `2727`

- Java: `PersonalAssetMsgEncoder`
- Node: `PersonalAssetEncoder.js`
- Python: `PersonalAssetMsgEncoder`
- C#: `PersonalAssetMsgEncoder`

Common reply methods:

- `getSignInMsgReply`
- `getHeartbeatMsgReply`
- `getLocationMsgReply`
- `getLocationAlarmMsgReply`
- `getLockMsgReply`
- `getSubLockMsgReply`
- `getWifiMsgReply`
- `getBluetoothPeripheralMsgReply`
- `getInnerGeoDataMsgReply`
- `getDeviceTempCollectionMsgReply`
- `getWifiWithDeviceInfoReply / getWifiWithDeviceInfoMsgReply`

## 7. Recommended Field Mapping for Customer Integration

For customer integration and troubleshooting, prioritize these objects/fields:

1. `SignInMessage`
- `imei`, `serialNo`, `software`, `firmware`, `hardware`, `platform`
- OBD family extras: `obdSoftware`, `obdHardware`, `obdBootVersion`, `obdDataVersion`

2. `LocationMessage` (`LocationInfoMessage/LocationAlarmMessage`)
- time/location: `date`, `latlngValid`, `latitude`, `longitude`, `altitude`, `speed`
- status: `iopIgnition`, `relayStatus`, `antitheftedStatus`, `batteryCharge`
- extensions: `protocolHeadType`, `originalAlarmCode`

3. `ObdMessage`
- `obdData` raw payload and decoded fields (DTC/VIN/clear-code result, etc.)

4. `BluetoothPeripheralDataMessage`
- `messageType`, sensor lists (tire, door, temp, fuel, driver sign-in, etc.)

5. `LockMessage/SubLockMessage`
- lock status, battery, unlock records, charging state, positioning state

## 8. Recommended Server Processing Flow

1. Select decoder by frame header:
- `2525/2323` -> `Decoder`
- `2626` -> `ObdDecoder`
- `2727` -> `PersonalAssetMsgDecoder`
- `2828` -> `PTTDecoder`

2. Decode and read child message type + `protocolHeadType`.

3. Send reply using encoder of the same protocol family:
- keep `imei` unchanged
- use original `serialNo`
- for APIs requiring `protocolHeadType`, pass the original type (`0x02/0x04/0x13/0x14/0x24/0x25`, etc.)

4. Serialize decoded object into business layer (recommended: JSON + protocol number field).

## 9. Appendix: `2828` PTT Protocol Mapping

| Protocol No. | Decoded Object |
|---|---|
| `282803` | `HeartbeatMessage` |
| `282804` | `TalkStartMessage` |
| `282805` | `TalkEndMessage` |
| `282806` | `VoiceMessage` |

Encoder: `PTTEncoder` (available in all four languages)

