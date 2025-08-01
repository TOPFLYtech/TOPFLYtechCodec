import paho.mqtt.client as mqtt


class MqttClient:
    def __init__(self,  imei, handle_message_callback=None):
        self.broker_url = "192.168.1.155"
        self.port = 9083
        self.imei = imei
        self.subTopic = "%s_S" % self.imei
        self.pubTopic = "%s_R" % self.imei
        self.client = mqtt.Client()
        self.handle_message_callback = handle_message_callback or self.default_handle_message


        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        self.client.on_disconnect = self.on_disconnect
        self.client.on_error = self.on_error

    def byte2HexString(self,byteArray, index):
        return ''.join('{:02x}'.format(x) for x in byteArray[index:])
    def hexString2Bytes(self,hexStrByte):
        hexStr = hexStrByte.decode('ascii')
        bytesArray = bytes.fromhex(hexStr)
        return bytesArray

    def connect(self):
        try:
            self.client.connect(self.broker_url,self.port)
            self.client.loop_start()
            print("Connected to MQTT Broker at %s" % self.broker_url)
        except Exception as e:
            print("Connection failed: %s" % e)

    def disconnect(self):
        self.client.loop_stop()
        self.client.disconnect()
        print("Disconnected from MQTT Broker")

    def publish(self, message):
        if not self.client.is_connected():
            raise Exception("Not connected to MQTT Broker")

        sendHexStr = self.byte2HexString(message, 0)
        asciiMessage = sendHexStr.encode('ascii')
        self.client.publish(self.pubTopic, asciiMessage)
        print("Published message to subTopic %s: %s" % (self.pubTopic, asciiMessage))

    def on_connect(self, client, userdata, flags, rc):
        print("MQTT Connected with result code %d" % rc)
        client.subscribe(self.subTopic)
        print("Subscribed to subTopic: %s" % self.subTopic)

    def on_message(self, client, userdata, msg):
        try:
            asciiMessage = msg.payload
            print("Received message on subTopic %s: %s" % (msg.topic, asciiMessage))
            hexBytesArray = self.hexString2Bytes(asciiMessage)
            self.handle_message_callback(hexBytesArray)
        except Exception as e:
            print("Connection failed: %s" % e)

    def on_disconnect(self, client, userdata, rc):
        print("Disconnected from MQTT Broker")

    def on_error(self, client, userdata, level, buf):
        print("MQTT Error: %s" % buf)

    def default_handle_message(self, message):
        print("Default handling for message ")









