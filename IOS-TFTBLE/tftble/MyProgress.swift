//
//  MyProgress.swift
//  tftble
//
//  Created by jeech on 2019/12/27.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
class MyProgress:UISlider{
    override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        return false
    }
  override func trackRect(forBounds bounds: CGRect) -> CGRect {
      let rect = super.trackRect(forBounds: bounds)
      return CGRect.init(x: rect.origin.x, y: rect.origin.x, width: bounds.size.width, height: 10)
  }
}
