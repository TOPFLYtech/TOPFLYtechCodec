var moment= require("moment")
var ByteUtils = {
    IMEI:{
      decode:function (bytes,index){
          if (bytes != null && index > 0 && (bytes.length - index) >= 8) {
              var str = ByteUtils.bytes2HexString(bytes, index);
              return str.substring(1, 16);
          }
          return ""
      } ,
      encode(imei){
          if(imei.length != 15){
              return []
          }
          return this.hexStringToByte("0" + imei)
      }
    },
    hexToFloat: function (hex) {
        var s = hex >> 31 ? -1 : 1;
        var e = (hex >> 23) & 0xFF;
        return s * (hex & 0x7fffff | 0x800000) * 1.0 / Math.pow(2, 23) * Math.pow(2, (e - 127))
    },
    flipHexString: function (hexValue, length) {
        var h = hexValue.substr(0, 2);
        for (var i = 0; i < length; ++i) {
            h += hexValue.substr(2 + (length - 1 - i) * 2, 2);
        }
        return h;
    },
    hexStringArrayToFloat: function (input) {
        let hex = this.flipHexString('0x' + input, 8);
        if (hex == '0x00000000') {
            return 0;
        }
        return this.hexToFloat(hex)
    },
    bytes2Float:function (bytes,index){
        if(bytes.length < index + 4){
            return 0
        }
        var changeArray = this.arrayOfRange(bytes,index,index+4)
        return this.hexStringArrayToFloat(this.bytes2HexString(changeArray,0))
    },
    bytes2HexString:function(bytes,index){
        var result = ""
        for(var i = index;i < bytes.length;i++){
            var item = bytes[i].toString(16);
            if(item.length == 1){
                item = "0" + item
            }
            result += item
        }
        return result
    },
    charArrayToStr: function (charCodeArray, codeType) {
        var curCharCode, secondCode;
        var resultStr = [];
        if (codeType && codeType.toLowerCase() == 'ascii') {
            for (var i = 0; i < charCodeArray.length; i = i + 2) {
                curCharCode = charCodeArray[i];
                resultStr.push(String.fromCharCode(curCharCode));
                secondCode = charCodeArray[i + 1];
                if (secondCode != 0) {
                    resultStr.push(String.fromCharCode(secondCode));
                }
            }
        } else {
            for (var i = 0; i < charCodeArray.length; i = i + 1) {
                curCharCode = charCodeArray[i];
                resultStr.push(String.fromCharCode(curCharCode));
            }
        }
        return resultStr.join("");
    },
    hexStringToByte:function (hexStr){
        var len = hexStr.length;
        if(len % 2 != 0){
            return ""
        }
        var result = []
        for (var i = 0; i < len; i = i + 2) {
            var curCharCode = parseInt(hexStr.substr(i, 2), 16);
            result.push(curCharCode);
        }
        return result;
    },
    strToBytes:function(str,encoding){
        var bytes = [];
        var buff = new Buffer(str, encoding);
        for(var i= 0; i< buff.length; i++){
            var byteint = buff[i];
            bytes.push(byteint);
        }
        return bytes;
    } ,
    bin2String:function (array)
    {
        return String.fromCharCode.apply(String, array);
    },
    byteToShort: function (input,index) {
        if(input.length < index + 1){
            return;
        }
        return input[index + 1] + (input[index] << 8);
    },
    arrayOfRange: function (source, from, to) {
        var result = []
        for (var i = from; i < to && i < source.length; i++) {
            result.push(source[i])
        }
        return result;
    },
    arrayEquals:function (item1,item2){
      if(item1.length != item2.length){
          return false
      }
      for(var i = 0 ;i < item1.length;i++){
          if(item1[i] != item2[i]){
              return false
          }
      }
      return true
    },
    getGTM0Date: function(bytes,startIndex){
        var dateStr = this.bytes2HexString(bytes,startIndex)
        var year = parseInt("20" + dateStr.substring(0,2))
        var curDate = moment.utc()
        if(year  > curDate.year){
            year = year - 100;
        }
        var month = parseInt(dateStr.substring(2,4)) - 1
        var day = parseInt(dateStr.substring(4,6))
        var hour = parseInt(dateStr.substring(6,8))
        var minute = parseInt(dateStr.substring(8,10))
        var second = parseInt(dateStr.substring(10,12))
        return moment.utc([year,month,day,hour,minute,second])
    },
    byteToLong: function (bytes,index) {
        if (bytes.length < 4 + index) {
            return null
        }
        return bytes[index + 3] + ( bytes[index + 2]<< 8) + ( bytes[index+ 1]<< 16) + (bytes[index]<< 24);
    },
    short2Bytes:function (number){
        var bytes = [2]
        for (var i = 1; i >= 0; i--) {
            bytes[i] = number % 256
            number >>= 8;
        }
        return bytes;
    },
    stringToByte:function (str,codeType){
        var bytes = new Array();
        var len, c;
        len = str.length;
        for (var i = 0; i < len; i++) {
            c = str.charCodeAt(i);
            bytes.push(c)
            if(codeType == "ascii"){
                bytes.push(0x00)
            }
        }
        return bytes;
    }
}
module.exports = ByteUtils;
