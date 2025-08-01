//
//  MyQueue.swift
//  TFTElock
//
//  Created by jeech on 2021/7/15.
//  Copyright © 2021 com.tftiot. All rights reserved.
//

import Foundation

public struct MsgQueue{
    fileprivate var heap = [[UInt8]]()
    /// How many elements the Priority Queue stores
    public var count: Int { return heap.count }
    
    /// true if and only if the Priority Queue is empty
    public var isEmpty: Bool { return heap.isEmpty }
    
    public mutating func push(_ element: [UInt8]) {
        heap.append(element)
    }
    
    public mutating func pop() -> [UInt8]? {
        if heap.isEmpty { return nil }
        return heap.removeFirst()
    }
    
    public mutating func remove(_ item: [UInt8]) {
        if let index = heap.firstIndex(of: item) {
            heap.swapAt(index, heap.count - 1)
            heap.removeLast()
        }
    }
    
    /// Removes all occurences of a particular item. Finds it by value comparison using ==. O(n)
    /// Silently exits if no occurrence found.
    ///
    /// - parameter item: The item to remove.
    public mutating func removeAll(_ item: [UInt8]) {
        var lastCount = heap.count
        remove(item)
        while (heap.count < lastCount) {
            lastCount = heap.count
            remove(item)
        }
    }
    
    public mutating func clear() {
        heap.removeAll(keepingCapacity: false)
    }
    
    /// Get a look at the current highest priority item, without removing it. O(1)
    ///
    /// - returns: The element with the highest priority in the PriorityQueue, or nil if the PriorityQueue is empty.
    public func peek() -> [UInt8]? {
        return heap.first
    }
    
}
