//
//  Sequence+KeyPath.swift
//  Charts
//
//  Created by Jacob Christie on 2020-12-15.
//

extension Sequence {
    func indexed() -> EnumeratedSequence<Self> {
        enumerated()
    }

    func max<T>(
        by keyPath: KeyPath<Element, T>,
        areInIncreasingOrder: (T, T) -> Bool
    ) -> Element? {
        self.max { areInIncreasingOrder($0[keyPath: keyPath], $1[keyPath: keyPath]) }
    }

    func max<T: Comparable>(by keyPath: KeyPath<Element, T>) -> Element? {
        max(by: keyPath, areInIncreasingOrder: <)
    }
}

extension Collection {
    func partitioningIndex(where belongsInSecondPartition: (Element) -> Bool) -> Index {
        var lowerBound = startIndex
        var upperBound = endIndex

        while lowerBound != upperBound {
            let distance = self.distance(from: lowerBound, to: upperBound)
            let mid = index(lowerBound, offsetBy: distance / 2)
            if belongsInSecondPartition(self[mid]) {
                upperBound = mid
            } else {
                lowerBound = index(after: mid)
            }
        }

        return lowerBound
    }
}
