//
//  TFTBleManager.m
//  HBuilder
//
//  Created by china topflytech on 2024/3/18.
//  Copyright © 2024 DCloud. All rights reserved.
//

#import "TFTBleManager.h"
#import "BleDeviceData.h"
#import "Utils.h"
#import <QMUIKit.h>
#import <AudioToolbox/AudioToolbox.h>
@implementation TFTBleManager
NSString * pwd = @"654321";
int notificationCount = 0;

NSString * const BLE_NOTIFY_VALUE = @"tft_antilost_notify";
const int BLE_STATUS_OF_CLOSE = -1;
const int BLE_STATUS_OF_DISCONNECT = 0;
const int BLE_STATUS_OF_CONNECTING = 1;
const int BLE_STATUS_OF_CONNECT_SUCC = 2;
const int BLE_STATUS_OF_SCANNING = 3;
- (void)setupUUIDs {
    NSString *serviceUUIDString = @"27760001-999C-4D6A-9FC4-C7272BE10900";
    _serviceUUID = [CBUUID UUIDWithString:serviceUUIDString];
    
    NSString *characteristicUUIDString = @"27763561-999C-4D6A-9FC4-C7272BE10900";
    _characteristicUUID = [CBUUID UUIDWithString:characteristicUUIDString];
}
- (void)setImei:(NSString *)imei{
    _imei = imei;
}
//-(void) initPlayer{
//    NSString *path = [[NSBundle mainBundle] pathForResource:@"laba" ofType:@"caf"];
//        NSURL *audioURL = [NSURL fileURLWithPath:path];
//        
//        // 初始化 AVPlayer
//        self.player = [AVPlayer playerWithURL:audioURL];
//}
- (instancetype)init {
    self = [super init];
    if (self) {
        NSDictionary *options = @{CBCentralManagerOptionShowPowerAlertKey: @YES,
                                  CBCentralManagerOptionRestoreIdentifierKey: @"tftBleAntiLost"};
        _centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil options:options];
        
        [self setupUUIDs];
        _allDeviceMap = [NSMutableDictionary dictionary];
        [self initControlFunc];
        _supportAntiLostModels = [NSMutableArray arrayWithObjects:MODEL_KNIGHTX_100, MODEL_KNIGHTX_300, MODEL_V0V_X10, nil];
        _pendingNotificationRequests = [NSMutableArray array];
        self.connectedPeripheralDict = [NSMutableDictionary dictionary];
        self.breakConnectImeiArray = [NSMutableArray array];
        self.lastConnectedPeripheralArray = [NSMutableArray array];
        self.bleIdImeiMap = [NSMutableDictionary dictionary];
        self.imeiConnectStatusMap = [NSMutableDictionary dictionary];
        [self initNeedConnectDeviceFromStore];
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        center.delegate = self;
        [self startReadingRSSI];
        NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:BLE_NOTIFY_VALUE];
        
        int soundWarning = [defaults integerForKey:@"soundWarning"];
        int shockWarning = [defaults integerForKey:@"shockWarning"];
        int notificationDuration = [defaults integerForKey:@"notificationDuration"];
        int notificationCount = [defaults integerForKey:@"notificationCount"];
        self.warningCount = notificationCount;
        self.warningDuration = notificationDuration;
        self.needSound = soundWarning == 1;
        self.needShock = shockWarning == 1;
    }
    return self;
}
- (void)initControlFunc {
    _controlFunc = [NSMutableDictionary dictionaryWithCapacity:4];
    
    NSMutableDictionary<NSString *, NSNumber *> *antiLostConnMap = [NSMutableDictionary dictionary];
    [antiLostConnMap setObject:@120 forKey:@"read"];
    [antiLostConnMap setObject:@120 forKey:@"write"];
    
    NSMutableDictionary<NSString *, NSNumber *> *configParamMap = [NSMutableDictionary dictionary];
    [configParamMap setObject:@122 forKey:@"read"];
    [configParamMap setObject:@121 forKey:@"write"];
    
    NSMutableDictionary<NSString *, NSNumber *> *searchModeMap = [NSMutableDictionary dictionary];
    [searchModeMap setObject:@124 forKey:@"read"];
    [searchModeMap setObject:@123 forKey:@"write"];
    
    NSMutableDictionary<NSString *, NSNumber *> *silenceModeMap = [NSMutableDictionary dictionary];
    [silenceModeMap setObject:@126 forKey:@"read"];
    [silenceModeMap setObject:@125 forKey:@"write"];
    NSMutableDictionary<NSString *, NSNumber *> *activelyDisconnectMap = [NSMutableDictionary dictionary];
    [activelyDisconnectMap setObject:@127 forKey:@"write"];
    [_controlFunc setObject:antiLostConnMap forKey:@"antiLostConn"];
    [_controlFunc setObject:configParamMap forKey:@"configParam"];
    [_controlFunc setObject:searchModeMap forKey:@"searchMode"];
    [_controlFunc setObject:silenceModeMap forKey:@"silenceMode"];
    [_controlFunc setObject:activelyDisconnectMap forKey:@"activelyDisconnect"];
}

-(void) saveBleIdMapToStore{
    NSMutableDictionary *originalDict = [NSMutableDictionary dictionary];
    for(NSUUID * idKey in self.bleIdImeiMap){
        NSString *imei = [self.bleIdImeiMap objectForKey:idKey];
        [originalDict setObject:imei forKey:[idKey UUIDString]];
    }
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:BLE_NOTIFY_VALUE];
    [defaults setObject:originalDict forKey:@"connectImeiDict"];
    [defaults synchronize];
}
-(void) initBleIdMapFromStore{
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:BLE_NOTIFY_VALUE];
    NSDictionary *savedDict = [defaults dictionaryForKey:@"connectImeiDict"];
    [self.bleIdImeiMap removeAllObjects];
    for (NSString *key in savedDict) {
        NSString *imei = [savedDict objectForKey:key];
        NSUUID *uuid = [[NSUUID alloc] initWithUUIDString:key];
        [self.bleIdImeiMap setObject:imei forKey:uuid];
       
    }
}
-(void) saveNeedConnectDeviceToStore{
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:BLE_NOTIFY_VALUE];
    [defaults setObject:self.imeiConnectStatusMap forKey:@"needConnectImeiDict"];
    [defaults synchronize];
}
-(void) initNeedConnectDeviceFromStore{
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:BLE_NOTIFY_VALUE];
    NSDictionary *savedDict = [defaults dictionaryForKey:@"needConnectImeiDict"];
    [self.imeiConnectStatusMap removeAllObjects];
    for (NSString *imei in savedDict) {
        [self.imeiConnectStatusMap setObject:[NSNumber numberWithInt:BLE_STATUS_OF_DISCONNECT] forKey:imei];
    }
}
- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    
    if (central.state == CBManagerStatePoweredOn) {
        NSLog(@"Bluetooth is ON and ready to scan.");
        // 在这里调用扫描方法
        _isBleAvailable = true;
        
        [self initBleIdMapFromStore];
        NSLog(@"connect imei:%@",self.bleIdImeiMap);
        
        if(self.lastConnectedPeripheralArray.count > 0){
            NSMutableArray<NSString*> * notInPathImei = [NSMutableArray array];
            
            for (NSUUID* macId in self.bleIdImeiMap) {
                NSString *imei = self.bleIdImeiMap[macId];
                bool find = false;
                for (CBPeripheral *lastConnectItem in self.lastConnectedPeripheralArray) {
                    if([lastConnectItem.identifier isEqual:macId]){
                        find = true;
                        break;
                    }
                }
                if(!find){
                    if([self.imeiConnectStatusMap objectForKey:imei] != nil){
                        [notInPathImei addObject:imei];
                    }
                   
                }
            }
            for (CBPeripheral *lastConnectItem in self.lastConnectedPeripheralArray) {
                NSString *imei = [self.bleIdImeiMap objectForKey:lastConnectItem.identifier];
                if([self.imeiConnectStatusMap objectForKey:imei] == nil){
                    [self.bleIdImeiMap removeObjectForKey:lastConnectItem.identifier];
                    continue;
                }
                [self stopScanning];
                _isConnectingBle = true;
          
                NSLog(@"willRestoreState reconnect %@ %@",lastConnectItem.identifier,imei);
                [self connectStatusCallback:imei];
                lastConnectItem.delegate = self;
                [self.centralManager connectPeripheral:lastConnectItem options:@{CBConnectPeripheralOptionNotifyOnDisconnectionKey:@(YES)}];
            }
            for (NSString* imei in notInPathImei) {
                if (imei != nil && [imei length] > 0) {
                    NSLog(@"connectImei:%@",imei);
                    [self connectDevice:imei];
                }else {
                    NSLog(@"字符串为 nil 或者为空");
                }
            }
        }else{
            for (NSUUID* macId in self.bleIdImeiMap) {
                NSString *imei = self.bleIdImeiMap[macId];
                if (imei != nil && [imei length] > 0) {
                    NSLog(@"connectImei@",imei);
                    [self connectDevice:imei];
                }else {
                    NSLog(@"字符串为 nil 或者为空");
                }
            }
        }
        
        
    } else {
        NSLog(@"Bluetooth is disabled.");
        _isBleAvailable = false;
        _isScanning = false;
        _isConnectingBle = false;
        NSDictionary *immutableDict = [ self.imeiConnectStatusMap copy];
         for(NSString * imei in immutableDict){
            int status = [[self.imeiConnectStatusMap objectForKey:imei] intValue];
            if(status == BLE_STATUS_OF_CONNECT_SUCC){
                [self.breakConnectImeiArray addObject:imei];
                [self doLostMobileWarning];
            }
            self.imeiConnectStatusMap[imei] = [NSNumber numberWithInt:BLE_STATUS_OF_DISCONNECT];
            [self connectStatusCallback:imei];
        } 
    }
    
    
}
- (void)startScanning {
    _isScanning = true;
    NSDictionary *immutableDict = [ self.imeiConnectStatusMap copy];
     for(NSString * imei in immutableDict){
        [self connectStatusCallback:imei];
    }
    // 可以传入服务UUID数组以过滤特定类型设备，这里假设扫描所有设备
    [_centralManager scanForPeripheralsWithServices:nil  options:@{CBConnectPeripheralOptionNotifyOnDisconnectionKey:@(YES)}];
}


- (void)stopScanning {
    self.isScanning = false;
    NSDictionary *immutableDict = [ self.imeiConnectStatusMap copy];
     for(NSString * imei in immutableDict){
        [self connectStatusCallback:imei];
    }
    [_centralManager stopScan];
}

- (void)centralManager:(CBCentralManager *)central willRestoreState:(NSDictionary<NSString *,id> *)dict {
    NSLog(@"willRestoreState");
    NSArray *peripherals = dict[CBCentralManagerRestoredStatePeripheralsKey];
    [self.lastConnectedPeripheralArray removeAllObjects];
    if (peripherals.count > 0) {
        
        for (CBPeripheral * item in peripherals) {
            [self.lastConnectedPeripheralArray addObject:item];
        }
        //        self.connectedPeripheral.delegate = self;
        //        [self.centralManager connectPeripheral:self.connectedPeripheral options:@{CBConnectPeripheralOptionNotifyOnDisconnectionKey:@(YES)}];
        //        [self.centralManager connectPeripheral:self.connectedPeripheral options:nil];
    }
}


- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData RSSI:(NSNumber *)RSSI {
    //        NSLog(@"Discovered peripheral %@ with data: %@ and RSSI: %@ dBm", peripheral.name, advertisementData, RSSI);
    NSInteger rssi = -9999;
    if (RSSI) {
        rssi = [RSSI intValue];
    }
    NSString *mac = peripheral.identifier.UUIDString;
    
    // 检查是否存在kCBAdvDataServiceData
    if (advertisementData[@"kCBAdvDataServiceData"]) {
        NSDictionary *dict = (NSDictionary *)advertisementData[@"kCBAdvDataServiceData"];
        
        CBUUID *key1 = [CBUUID UUIDWithString:@"BEAF"];
        CBUUID *key2 = [CBUUID UUIDWithString:@"DEAF"];
        if (dict[key1] && dict[key2]) {
            NSString *name = advertisementData[@"kCBAdvDataLocalName"];
            NSData *imeiData = nil, *versionData = nil;
            if (dict[key1]) {
                imeiData = dict[key1];
            }
            if (dict[key2]) {
                versionData = dict[key2];
            }
            
            [self parseBleDataWithDeviceName:name imeiData:imeiData versionData:versionData peripheral:peripheral  rssi:rssi mac:mac];
        }
        //        else if (dict[key2]) {
        //            if ([_allDeviceMap objectForKey:mac]) {
        //                BleDeviceData *retrievedDevice = _allDeviceMap[mac];
        //                retrievedDevice.peripheral = peripheral;
        //                retrievedDevice.rssi = rssi;
        //                [self tryToConnect];
        //            }
        //        }
    }
}

- (void)parseBleDataWithDeviceName:(NSString *)deviceName
                          imeiData:(NSData *)imeiData
                       versionData:(NSData *)versionData
                        peripheral:(CBPeripheral *)peripheral
                              rssi:(int)rssi
                               mac:(NSString *)mac {
    
    if (imeiData || versionData) {
        NSString * imei = @"";
        if (imeiData) {
            NSRange subrange = NSMakeRange(1, imeiData.length - 1); // 从索引3开始截取到末尾
            NSData *subData = [imeiData subdataWithRange:subrange];
            imei = [[NSString alloc] initWithData:subData encoding:NSUTF8StringEncoding];
        }
        if (imei.length == 0){
            return;
        }
        NSString *model = @"";
        
        if (versionData) {
            uint8_t protocolByte;
            [versionData getBytes:&protocolByte range:NSMakeRange(0, 1)];
            model = [BleDeviceData parseModelWithProtocolByte:protocolByte];
        }
        if ([_allDeviceMap objectForKey:imei]) {
            BleDeviceData *retrievedDevice = _allDeviceMap[mac];
            retrievedDevice.peripheral = peripheral;
            retrievedDevice.rssi = rssi;
            retrievedDevice.model = model;
            retrievedDevice.deviceName = deviceName;
            retrievedDevice.imei = imei;
            NSLog(@" t %@ ,%@",imei,model,deviceName);
        }else{
            if (model.length == 0){
                return;
            }
            if ([self.supportAntiLostModels containsObject:model]) {
                
            } else {
                return;
            }
            BleDeviceData *bleDeviceData = [[BleDeviceData alloc] init];
            bleDeviceData.imei = imei;
            bleDeviceData.model = model;
            bleDeviceData.mac = mac;
            bleDeviceData.peripheral = peripheral;
            bleDeviceData.deviceName = deviceName;
            bleDeviceData.rssi = rssi;
            NSLog(@" t %@ ,%@",imei,model,deviceName);
            [_allDeviceMap setObject:bleDeviceData forKey:imei];
        }
        if([self.imeiConnectStatusMap objectForKey:imei]){
            NSLog(@"check connect %@ ,%@",imei,model,deviceName);
            [self checkNeedConnect:imei];
        }
        
        
    }
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    //    self.connectedPeripheral = peripheral;
    //    self.lastConnectedPeripheral = nil;
    for (CBPeripheral *lastConnectItem in self.lastConnectedPeripheralArray) {
        if([peripheral.identifier isEqual:lastConnectItem.identifier]){
            [self.lastConnectedPeripheralArray removeObject:lastConnectItem];
            break;
        }
    }
    peripheral.delegate = self;
    if (peripheral.state == CBPeripheralStateConnected) {
        [peripheral discoverServices:nil];
    } else {
        NSLog(@"Peripheral is not connected.");
    }
    NSLog(@"connectPeripheral");
}

- (void)peripheral:(CBPeripheral *)peripheral didReadRSSI:(NSNumber *)RSSI error:(NSError *)error {
    if (error) {
        NSLog(@"读取RSSI出错: %@", error.localizedDescription);
    } else {
        //        NSLog(@"设备的RSSI: %@", RSSI);
        if(self.didReceiveRssi != nil){
            NSString * imei = [self.bleIdImeiMap objectForKey:peripheral.identifier];
            if(imei != nil){
                self.didReceiveRssi(imei,[RSSI intValue]);
            }
            
        }
    }
}
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    NSLog(@"didDiscoverServices");
    if (!error) {
        for (CBService *service in peripheral.services) {
            NSLog(@"find services: %@", service);
            if([service.UUID isEqual:self.serviceUUID]){
                NSLog(@"discoverCharacteristics services: %@", service);
                [peripheral discoverCharacteristics:@[self.characteristicUUID] forService:service];
            }
            
        }
    } else {
        NSLog(@"Error discovering services: %@", error.localizedDescription);
    }
}
- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(nullable NSError *)error{
    //disconnect
    NSLog(@"Bluetooth is disconnect.");
    if(error){
        
    }else{
        //active disconnect
        
    }
    _isScanning = false;
    _isConnectingBle = false;
   
    NSString * imei = [self.bleIdImeiMap objectForKey:peripheral.identifier];
    if(imei != nil){
        [self.breakConnectImeiArray addObject:imei];
        [self doLostMobileWarning];
        [self.imeiConnectStatusMap setObject:[NSNumber numberWithInt:BLE_STATUS_OF_DISCONNECT] forKey:imei];
        [self connectStatusCallback:imei];
        [self.connectedPeripheralDict removeObjectForKey:imei];
    }
    [self.bleIdImeiMap removeObjectForKey:peripheral.identifier];
}
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    if (!error) {
        for (CBCharacteristic *characteristic in service.characteristics) {
            NSLog(@"find characteristic: %@", characteristic);
            if ([characteristic.UUID isEqual:self.characteristicUUID]) {
                NSLog(@"didDiscoverCharacteristicsForService characteristic: %@", characteristic);
                self.characteristicToWrite = characteristic;
                [peripheral setNotifyValue:YES forCharacteristic:characteristic];
                [peripheral readValueForCharacteristic:characteristic];
            }
        }
    } else {
        NSLog(@"Error discovering characteristics: %@", error.localizedDescription);
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateNotificationStateForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error{
    if(error != nil){
        [self.centralManager cancelPeripheralConnection:peripheral];
        return;
    }
    if(characteristic.isNotifying){
        NSLog(@"didUpdateNotificationStateForCharacteristic");
       
       
        NSLog(@"notify id:%@   count:%d",peripheral.identifier,[self.bleIdImeiMap count]);
        NSString * imei = [self.bleIdImeiMap objectForKey:peripheral.identifier];
       
        if(imei){
            [self.imeiConnectStatusMap setObject:[NSNumber numberWithInt:BLE_STATUS_OF_CONNECT_SUCC] forKey:imei];
            [self saveBleIdMapToStore];
            [self saveNeedConnectDeviceToStore];
            [self.breakConnectImeiArray removeObject:imei];
            if([self.breakConnectImeiArray count] == 0){
                self.isCurWarning = false;
            }
            [self.connectedPeripheralDict setObject:peripheral forKey:imei];
            //            NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:BLE_NOTIFY_VALUE];
            //            // 设置字符串值
            //            NSLog(@"save connect imei:%@",_curConnectImei);
            //            [defaults setObject:_curConnectImei forKey:@"connectImei"];
            //            // 保存更改
            //            [defaults synchronize];
            
            [self antiLostConnConfig:imei open:1];
            [self connectStatusCallback:imei];
        }
        // 获取 NSUserDefaults 实例
        
    }else{
        [self.centralManager cancelPeripheralConnection:peripheral];
    }
}

// dealloc方法
- (void)dealloc {
    NSLog(@"YourViewController is being deallocated");
    [self stopReadingRSSI];
    self.centralManager.delegate = nil; // 避免潜在的回调问题
    // self.connectedPeripheral.delegate = nil;  避免潜在的回调问题
    for(NSString*imei in self.connectedPeripheralDict){
        CBPeripheral * item = self.connectedPeripheralDict[imei];
        item.delegate = nil;
    }
    // 其他清理代码
}


// 启动定时器来定期读取RSSI
- (void)startReadingRSSI {
    self.rssiTimer = [NSTimer scheduledTimerWithTimeInterval:2.0 // 设置你想要的间隔
                                                      target:self
                                                    selector:@selector(readRSSIValue)
                                                    userInfo:nil
                                                     repeats:YES];
}

// 读取RSSI的方法
- (void)readRSSIValue {
    for(NSString*imei in self.connectedPeripheralDict){
        int status = [[self.imeiConnectStatusMap objectForKey:imei] intValue];
        if(status == BLE_STATUS_OF_CONNECT_SUCC){
            CBPeripheral * item = self.connectedPeripheralDict[imei];
            [item readRSSI];
        }
    }
    NSDate *now = [NSDate date];
    NSDate *futureDate = [now dateByAddingTimeInterval:9];
    NSMutableArray * removeImeiList = [NSMutableArray array];
    for(NSString * imei in self.disconnectImeiMap){
        NSDate * disconnectDate = self.disconnectImeiMap[imei];
        if ([futureDate compare:disconnectDate] == NSOrderedDescending) {
            NSLog(@"The future date is greater than the current date by 10 seconds.");
            [removeImeiList addObject:imei];
        } else {
            NSLog(@"The future date is not greater than the current date by 10 seconds.");
        }
    }
    for(NSString * imei in removeImeiList){
        [self doActivelyDisconnectCb:imei];
    }
    
    NSDictionary *immutableDict = [ self.imeiConnectStatusMap copy];
    bool isNeedScan = false;
    for(NSString * imei in immutableDict){
        int status = [[immutableDict objectForKey:imei] intValue];
        if(status == BLE_STATUS_OF_CONNECTING || status == BLE_STATUS_OF_DISCONNECT){
            _isNeedConnectDevice = true;
            isNeedScan = true;
            [self startScanning];
            break;
        }
    }
    if(!isNeedScan){
        [self stopScanning];
    }
}

// 停止定时器
- (void)stopReadingRSSI {
    [self.rssiTimer invalidate];
    self.rssiTimer = nil;
}
-(void)connectStatusCallback:(NSString*) imei{
    if(self.didReceiveBleStatus != nil){
        int connectStatus = [self getConnectStatus:imei];
        self.didReceiveBleStatus(imei,connectStatus);
        
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    if (characteristic.isNotifying && characteristic.value) {
        NSString* imei = [self.bleIdImeiMap objectForKey:peripheral.identifier];
        NSLog(@"receive :%@ ,imei %@",characteristic.value,imei);
        self.didReceiveDataCallback(imei,characteristic.value);
    }
}

//- (void)sendData:(NSData *)data {
//    if (self.connectedPeripheral != nil){
//        [self.connectedPeripheral writeValue:data forCharacteristic:self.characteristicToWrite type:CBCharacteristicWriteWithResponse];
//    }
//
//}


-(void) disconnect:(NSString*) imei{
    NSLog(@"do disconnect");
    NSNumber * status = [self.imeiConnectStatusMap objectForKey:imei];
    if(status != nil && [status intValue] == BLE_STATUS_OF_CONNECT_SUCC){
        [self doActivelyDisconnect:imei];
    }
    [self.imeiConnectStatusMap setObject:[NSNumber numberWithInt:BLE_STATUS_OF_DISCONNECT] forKey:imei];
    [self connectStatusCallback:imei];
    [self.disconnectImeiMap setObject:[NSDate date] forKey:imei];
    [self.imeiConnectStatusMap removeObjectForKey:imei];
    [self saveNeedConnectDeviceToStore];
}
-(void) disconnectAll{
    NSDictionary *immutableDict = [ self.imeiConnectStatusMap copy];
    for(NSString * imei in immutableDict){
        int status = [[immutableDict objectForKey:imei] intValue];
       
        if(status == BLE_STATUS_OF_CONNECT_SUCC){
            [self doActivelyDisconnect:imei];
        }
        [self.imeiConnectStatusMap setObject:[NSNumber numberWithInt:BLE_STATUS_OF_DISCONNECT] forKey:imei];
        [self connectStatusCallback:imei];
        [self.disconnectImeiMap setObject:[NSDate date] forKey:imei];
        [self.imeiConnectStatusMap removeObjectForKey:imei];
    }
    [self saveNeedConnectDeviceToStore];
}

-(void)connect:(NSString*) imei{
    //    _curConnectImei = imei;
    if(![self.imeiConnectStatusMap objectForKey:imei]){
        [self.imeiConnectStatusMap setObject:[NSNumber numberWithInt:BLE_STATUS_OF_DISCONNECT] forKey:imei];
    }
    _isNeedConnectDevice = true;
    [self checkNeedConnect:imei];
}
-(void) checkNeedConnect:(NSString*)imei{
    if(!_isNeedConnectDevice){
        return;
    }
    if([self.allDeviceMap objectForKey:imei]){
        [self connectDeviceBle:imei];
    }
 
    
}
-(void) connectDevice:(NSString*)imei{
    [self connect:imei];
    int curConnectStatus = [self getConnectStatus:imei];
    if(curConnectStatus == BLE_STATUS_OF_CONNECTING){
        return;
    }
    if(curConnectStatus == BLE_STATUS_OF_CONNECT_SUCC){
        [self disconnect:imei];
    }
    if(curConnectStatus != BLE_STATUS_OF_SCANNING){
        [self startScanning];
    }
}
-(void) connectDeviceBle:(NSString*)imei{
//    if(_isConnectingBle){
//        return;
//    }
    [self stopScanning];
    _isConnectingBle = true;
    [self.imeiConnectStatusMap setObject:[NSNumber numberWithInt:BLE_STATUS_OF_CONNECTING] forKey:imei];
    [self connectStatusCallback:imei];
    if ([_allDeviceMap objectForKey:imei]) {
        BleDeviceData * bleDeviceData = _allDeviceMap[imei];
        bleDeviceData.peripheral.delegate = self;
        [self.bleIdImeiMap setObject:imei forKey: bleDeviceData.peripheral.identifier ];
        NSLog(@"connect ble id :%@",bleDeviceData.peripheral.identifier);
        [self.centralManager connectPeripheral:bleDeviceData.peripheral options:@{CBConnectPeripheralOptionNotifyOnDisconnectionKey:@(YES)}];
        
    }
}

- (void)readData:(NSString*) imei cmdHead:(NSInteger)cmdHead {
    NSNumber * status = [self.imeiConnectStatusMap objectForKey:imei];
    if (status == nil || [status intValue] != BLE_STATUS_OF_CONNECT_SUCC) {
        return;
    }
    
    NSArray<NSNumber *> *uint8Array = [Utils getInterActiveCmd:pwd cmdHead:cmdHead content:@[]];
    NSData *data =   [self dataFromArray:uint8Array];
    CBPeripheral* connectedPeripheral = [self.connectedPeripheralDict objectForKey:imei];
    if(connectedPeripheral != nil){
        [connectedPeripheral writeValue:data forCharacteristic:self.characteristicToWrite type:CBCharacteristicWriteWithResponse];    }
    
}

- (void)writeStrDataWithCmdHead:(NSString*) imei cmdHead:(NSInteger)cmdHead dataStr:(NSString *)dataStr {
    NSNumber * status = [self.imeiConnectStatusMap objectForKey:imei];
    if (status == nil || [status intValue] != BLE_STATUS_OF_CONNECT_SUCC) {
        return;
    }
    
    const char *strChars = [dataStr cStringUsingEncoding:NSUTF8StringEncoding];
    NSUInteger strLength = strlen(strChars);
    NSMutableArray<NSNumber *> *dataArray = [NSMutableArray arrayWithCapacity:strLength];
    for (NSUInteger i = 0; i < strLength; i++) {
        [dataArray addObject:@(strChars[i])];
    }
    
    NSArray<NSNumber *> *uint8Array = [Utils getInterActiveCmd:pwd cmdHead:cmdHead content:dataArray];
    NSData *data =   [self dataFromArray:uint8Array];
    CBPeripheral* connectedPeripheral = [self.connectedPeripheralDict objectForKey:imei];
    if(connectedPeripheral != nil){
        [connectedPeripheral writeValue:data forCharacteristic:self.characteristicToWrite type:CBCharacteristicWriteWithResponse];
    }
    
}
-(NSData *)dataFromArray:(NSArray<NSNumber *>*) array {
    NSMutableData *data = [NSMutableData dataWithCapacity:array.count];
    for (NSNumber *num in array) {
        uint8_t byte = [num unsignedCharValue];
        [data appendBytes:&byte length:1];
    }
    return data;
}
- (void)writeArrayDataWithCmdHead:(NSString*) imei cmdHead:(NSInteger)cmdHead content:(NSArray<NSNumber *> *)contentArray {
    NSNumber * status = [self.imeiConnectStatusMap objectForKey:imei];
    if (status == nil || [status intValue] != BLE_STATUS_OF_CONNECT_SUCC) {
        return;
    }
    
    NSArray<NSNumber *> *uint8Array = [Utils getInterActiveCmd:pwd cmdHead:cmdHead content:contentArray];
    NSData *data =   [self dataFromArray:uint8Array];
    
    NSLog(@"%@", uint8Array);
    CBPeripheral* connectedPeripheral = [self.connectedPeripheralDict objectForKey:imei];
    if(connectedPeripheral != nil){
        [connectedPeripheral writeValue:data forCharacteristic: self.characteristicToWrite type:CBCharacteristicWriteWithResponse];
    }
    
}

- (void)antiLostConnConfig:(NSString*) imei open:(NSInteger)open {
    NSMutableDictionary<NSString *, NSNumber *> *antiLostConnParams = _controlFunc[@"antiLostConn"];
    NSNumber *writeCmd = antiLostConnParams[@"write"];
    
    NSNumber *openNumber = @(open);
    NSArray<NSNumber *> *contentArray = @[openNumber];
    [self writeArrayDataWithCmdHead:imei cmdHead:writeCmd.unsignedIntegerValue content:contentArray];
}

- (void)setAntiLostBleStatus:(NSString*) imei twoWayAntiLost:(NSInteger)twoWayAntiLost singleVibrationDurationTime:(NSInteger)singleVibrationDurationTime repeatTime:(NSInteger)repeatTime {
    NSMutableDictionary<NSString *, NSNumber *> *configParam = _controlFunc[@"configParam"];
    NSNumber *writeCmd = configParam[@"write"];
    
    NSArray<NSNumber *> *contentArray = @[@(twoWayAntiLost), @(singleVibrationDurationTime), @(repeatTime)];
    [self writeArrayDataWithCmdHead:imei cmdHead:writeCmd.unsignedIntegerValue content:contentArray];
}

- (void)setAntiLostBleSearchMode:(NSString*) imei searchMode:(NSInteger)searchMode {
    NSMutableDictionary<NSString *, NSNumber *> *searchModeDict = _controlFunc[@"searchMode"];
    NSNumber *writeCmd = searchModeDict[@"write"];
    
    NSNumber *searchModeNumber = @(searchMode);
    NSArray<NSNumber *> *contentArray = @[searchModeNumber];
    [self writeArrayDataWithCmdHead:imei cmdHead:writeCmd.unsignedIntegerValue content:contentArray];
}

- (void)setAntiLostBleSilenceMode:(NSString*) imei silenceMode:(NSInteger)silenceMode {
    NSMutableDictionary<NSString *, NSNumber *> *silenceModeDict = _controlFunc[@"silenceMode"];
    NSNumber *writeCmd = silenceModeDict[@"write"];
    
    NSNumber *silenceModeNumber = @(silenceMode);
    NSArray<NSNumber *> *contentArray = @[silenceModeNumber];
    [self writeArrayDataWithCmdHead:imei cmdHead:writeCmd.unsignedIntegerValue content:contentArray];
}
-(void) doActivelyDisconnect:(NSString*) imei {
    NSMutableDictionary<NSString *, NSNumber *> *silenceModeDict = _controlFunc[@"activelyDisconnect"];
    NSNumber *writeCmd = silenceModeDict[@"write"];
    [self writeArrayDataWithCmdHead:imei cmdHead:writeCmd.unsignedIntegerValue content:@[]];
}

-(void) doActivelyDisconnectCb:(NSString *)imei{
    [self.disconnectImeiMap removeObjectForKey:imei];
    //    _curConnectImei = @"";
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:BLE_NOTIFY_VALUE];
    for(NSUUID * macId in self.bleIdImeiMap){
        NSString* imeiItem = [self.bleIdImeiMap objectForKey:macId];
        if([imeiItem isEqual:imei]){
            [self.bleIdImeiMap removeObjectForKey:macId];
            break;
        }
    }
    
    [self saveBleIdMapToStore];
    [self saveNeedConnectDeviceToStore];
    _isConnectingBle = false;
    _isNeedConnectDevice = false;
    [self.imeiConnectStatusMap setObject:[NSNumber numberWithInt:BLE_STATUS_OF_DISCONNECT] forKey:imei];
    [self connectStatusCallback:imei];
    if([self.connectedPeripheralDict objectForKey:imei]){
        CBPeripheral * connectItem = [self.connectedPeripheralDict objectForKey:imei];
        [self.centralManager cancelPeripheralConnection:connectItem];
    }
    
    [self.connectedPeripheralDict removeObjectForKey:imei];
    [self.imeiConnectStatusMap removeObjectForKey:imei];
}

- (int)getConnectStatus:(NSString*)imei{
    if( !_isBleAvailable){
        return BLE_STATUS_OF_CLOSE;
    }
    NSNumber * curDeviceConnectStatus = [self.imeiConnectStatusMap objectForKey:imei];
    if(curDeviceConnectStatus){
        int status = [curDeviceConnectStatus intValue];
        if(status  == BLE_STATUS_OF_CONNECT_SUCC){
            return BLE_STATUS_OF_CONNECT_SUCC;
        }else if(status  == BLE_STATUS_OF_CONNECTING){
            return BLE_STATUS_OF_CONNECTING;
        }
    }
    if(_isScanning){
        return BLE_STATUS_OF_SCANNING;
    }
    return BLE_STATUS_OF_DISCONNECT;
}
//-(NSString*)getCurConnectImei{
//    return _curConnectImei;
//}


-(void)doLostMobileWarning{
   
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(7 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self makeLostMobileWarning];
    });
}
-(void)makeLostMobileWarning{
    if([self.breakConnectImeiArray count] == 0){
        return;
    }
    if(_isCurWarning){
        return;
    }
    _isCurWarning = true;
    [self startSendingPeriodicNotifications];
}

- (void)startSendingPeriodicNotifications {
    notificationCount = 0;
    while (notificationCount < _warningCount) {
        if(!_isCurWarning){
            return;
        }
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)((_warningDuration+3) *(notificationCount) * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            NSLog(@"sendPeriodicNotification");
            [self sendPeriodicNotification];
            if(_needShock)
            {
//                [self warningPlaySound];
                [self startVibrationLoopWithCount:_warningDuration * 3];
            }
        });
        notificationCount++;
    }
    
}
//-(void) warningPlaySound{
//    if(!_isCurWarning){
//        return;
//    }
//    [self.player pause];
//    UIApplicationState state = [UIApplication sharedApplication].applicationState;
//
//    if (state == UIApplicationStateActive) {
//        NSLog(@"App is in the foreground.");
//        [self.player play];
//        // 应用在前台
//    } else {
//        NSLog(@"App is in the background.");
//        // 应用在后台或者处于非活跃状态
//    }
//}

- (void)sendPeriodicNotification {
    if(!_isCurWarning){
        return;
    }
    UNMutableNotificationContent *content = [[UNMutableNotificationContent alloc] init];
    content.title = NSLocalizedString(@"lost_connect", @"Device disconnected");
    content.body = NSLocalizedString(@"lost_connect_desc",@"Lost connection with the Bluetooth device. Please check the device status or reconnect.");
    if(_needSound){
        content.sound =   [UNNotificationSound soundNamed:@"laba.caf"];
    }
    
    // 创建触发器（立即触发）
    UNTimeIntervalNotificationTrigger *trigger = [UNTimeIntervalNotificationTrigger triggerWithTimeInterval:5 repeats:NO];
    
    // 创建通知请求
    NSString *requestIdentifier = @"tftAntilostBleDisconnectedNotification";
    UNNotificationRequest *notificationRequest = [UNNotificationRequest requestWithIdentifier:requestIdentifier content:content trigger:trigger];
    
    // 添加通知请求到通知中心
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center addNotificationRequest:notificationRequest withCompletionHandler:^(NSError * _Nullable error) {
        if (error) {
            NSLog(@"Failed to schedule Bluetooth disconnected notification: %@", error);
        } else {
            NSLog(@"add notification succ");
            
        }
    }];
}
- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler {
    // 用户点击了通知
    NSLog(@"User tapped on notification: %@", response.notification.request.identifier);
    _isCurWarning = false;
    // 取消尚未触发的其他通知
    NSArray<NSString *> *identifiersToRemove = [self.pendingNotificationRequests valueForKeyPath:@"tftAntilostBleDisconnectedNotification"];
    [center removePendingNotificationRequestsWithIdentifiers:identifiersToRemove];
    [self.pendingNotificationRequests removeAllObjects];
    
    // 处理用户点击通知的逻辑...
    // ...
    
    completionHandler();
}

+(void)requestNotificationPermission{
    if ([UNUserNotificationCenter class] != nil) { // iOS 10+ 使用新 API
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        center.delegate = self;
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionAlert | UNAuthorizationOptionSound | UNAuthorizationOptionBadge)
                              completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (!error) {
                NSLog(@"Local notification permission granted.");
            }
        }];
    }
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler {
    // 在这里处理通知，例如显示一个警告框
    NSLog(@"Receive a notification");
    completionHandler(UNNotificationPresentationOptionSound |UNNotificationPresentationOptionAlert);
}

- (void)startVibrationLoopWithCount:(NSInteger)count {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        for (int i = 0; i < count; i++) {
            if(!_isCurWarning){
                return;
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
            });
            [NSThread sleepForTimeInterval:0.2];
        }
    });
}

-(NSString *) getCurConnectDeviceModel:(NSString *)imei{
    NSNumber * status = [self.imeiConnectStatusMap objectForKey:imei];
    if (status == nil || [status intValue] != BLE_STATUS_OF_CONNECT_SUCC) {
        return @"";
    }
    BleDeviceData * deviceItem = [self.allDeviceMap objectForKey:imei];
    if(deviceItem != nil){
        return deviceItem.model;
    }else{
        return @"";
    }
}
@end
