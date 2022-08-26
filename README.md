# TOPFLYtechCodec
&#8195;&#8195;TOPFLYtech's device access platform's decoding library allows TOPFLYtech's devices to quickly access the platform.  
&#8195;&#8195;We provide Java, C#, Python ,Node codec libraries, you can directly access your platform, or you can put a partial copy of the code selection into your platform. If you are not sure about byte conversion, please refer to the BytesUtils class inside.  
&#8195;&#8195;We also provide a demo to facilitate testing device interaction with the platform.  
&#8195;&#8195;We also have an online device message decoder to verify the data sent by the device. Please visit: https://www.topflytech.com/Decoder.html

&#8195;&#8195;To purchase equipment from TOPFLYtech, please visit: http://www.topflytech.com  


&#8195;&#8195;If you do not know the corresponding relationship between the protocol and the Lib library, please refer to https://github.com/TOPFLYtech/TOPFLYtechCodec/blob/master/TOPFLYtechCodec.bmp .You can download the picture and zoom in to see the code structure.


&#8195;&#8195;If you need to see the algorithm for generating unique IDs on the Android side, please check https://github.com/TOPFLYtech/TOPFLYtechCodec/blob/master/Android-lock/app/src/main/java/com/topflytech/lockActive/data/ UniqueIDTool.java, if you need to see the algorithm for generating unique IDs on the IOS side, please check https://github.com/TOPFLYtech/TOPFLYtechCodec/blob/master/ios-lock/TFT%20Elock/TFT%20Elock/UniqueIDTool.swift, The IOS side needs to support the keyChain function.You need to open Keychain share first, select Target -> Capabilities -> Keychain Groups of the project. Turn this option on.