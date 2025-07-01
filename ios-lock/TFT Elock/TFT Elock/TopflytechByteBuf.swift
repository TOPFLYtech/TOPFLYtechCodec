//
//  TopflytechByteBuf .swift
//  TFT Elock
//
//  Created by china topflytech on 2023/5/16.
//  Copyright © 2023 com.tftiot. All rights reserved.
//

import Foundation
class TopflytechByteBuf {
    private var selfBuf = [UInt8](repeating: 0, count: 4096)
    private var readIndex = 0
    private var writeIndex = 0
    private var capacity = 4096
    private var markerReadIndex = 0
    
    func putBuf(_ inData: [UInt8]) {
        if capacity - writeIndex >= inData.count {
            for i in 0..<inData.count {
                selfBuf[writeIndex + i] = inData[i]
            }
            writeIndex += inData.count
        } else {
            if capacity - writeIndex + readIndex >= inData.count {
                let currentDataLength = writeIndex - readIndex
                for i in 0..<currentDataLength {
                    selfBuf[i] = selfBuf[readIndex + i]
                }
                writeIndex = currentDataLength
                readIndex = 0
                markerReadIndex = 0
                for i in 0..<inData.count {
                    selfBuf[writeIndex + i] = inData[i]
                }
                writeIndex += inData.count
            } else {
                let needLength = ((writeIndex - readIndex + inData.count) / 4096 + 1) * 4096
                var tmp = [UInt8](repeating:0, count: needLength)
                for i in 0..<writeIndex-readIndex {
                    tmp[i] = selfBuf[readIndex + i]
                }
                selfBuf = tmp
                capacity = needLength
                writeIndex -= readIndex
                readIndex = 0
                markerReadIndex = 0
                for i in 0..<inData.count {
                    selfBuf[writeIndex + i] = inData[i]
                }
                writeIndex += inData.count
            }
        }
    }
    
    func getReadableBytes() -> Int {
        return writeIndex - readIndex
    }
    
    func getReadIndex() -> Int {
        return readIndex
    }
    
    func getByte(at index: Int) -> UInt8 {
        if index >= writeIndex - readIndex {
            return 0
        }
        return selfBuf[readIndex + index]
    }
    
    func markReaderIndex() {
        markerReadIndex = readIndex
    }
    
    func resetReaderIndex() {
        readIndex = markerReadIndex
    }
    
    func skipBytes(_ length: Int) {
        readIndex += length
    }
    
    func readBytes(_ length: Int) -> [UInt8]? {
        if length > getReadableBytes() {
            return nil
        }
        let result = Array(selfBuf[readIndex..<readIndex+length])
        readIndex += length
        return result
    }
}
