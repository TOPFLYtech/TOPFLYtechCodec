var crypto = require("crypto")
var ByteUtils = require("./ByteUtils")
var cryptoTool = {
    MessageEncryptType:{
        NONE:0,
        MD5:1,
        AES:2,
    },
    AES : {
        getAesLength:function (packageLength){
            if (packageLength <= 15){
                return packageLength;
            }
            return ((packageLength - 15) / 16 + 1) * 16 + 15;
        },
        //加密公共密钥 32位
        keys : 'topflytechAES',
            clearEncoding : 'binary',
            algorithm : 'aes-128-cbc',
            cipherEncoding : 'binary',
            ivParam:"topflytech201205",
            encode : function(data,keys){
            try{
                if(!keys) {
                    keys = this.keys
                }
                var md5Key = cryptoTool.md5(keys)
                var keyBytes = ByteUtils.hexStringToByte(md5Key)
                var secret = new Buffer(keyBytes)
                var cipher = crypto.createCipheriv('aes-128-cbc', secret, this.ivParam);
                var crypted = cipher.update(new Buffer(data), 'binary', 'hex');
                crypted += cipher.final('hex');
                // crypted = new Buffer(crypted, 'binary').toString('base64');
                return ByteUtils.hexStringToByte(crypted);
            }catch(e){
                console.log(e);
                return "";
            }
        },
        decode : function(data,keys){
            try{
                if(!keys) {
                    keys = this.keys
                }
                var md5Key = cryptoTool.md5(keys)
                var keyBytes = ByteUtils.hexStringToByte(md5Key)
                var secret = new Buffer(keyBytes)
                var decipher = crypto.createDecipheriv('aes-128-cbc', secret, this.ivParam);
                var decoded = decipher.update(new Buffer(data), 'hex', 'hex');
                decoded += decipher.final('hex');
                return ByteUtils.hexStringToByte(decoded);
            }catch(e){
                //console.log(e)
                return "";
            }
        }
    },
    md5: function(content) {
        var md5 = crypto.createHash('md5');
        md5.update(content);
        return md5.digest('hex');
    },
    decryptData:function (data,encryptType,aesKey){
        if (encryptType == this.MessageEncryptType.MD5){
            var realData = ByteUtils.arrayOfRange(data, 0, data.length - 8)
            var md5Data = ByteUtils.arrayOfRange(data, data.length - 8, data.length)
            var pathMd5 = this.md5(new Buffer(realData));
            if (pathMd5 == null){
                return null;
            }
            var pathMd5Byte = ByteUtils.hexStringToByte(pathMd5)
            pathMd5 = ByteUtils.arrayOfRange(pathMd5Byte, 4, 12);
            if (!ByteUtils.arrayEquals(pathMd5,md5Data)){
                return null;
            }
            return realData;
        }else if(encryptType == this.MessageEncryptType.AES){
            var head = ByteUtils.arrayOfRange(data,0,15)
            var aesData = ByteUtils.arrayOfRange(data,15,data.length)
            if (aesData == null || aesData.length == 0){
                return data;
            }
            var realData = null;
            try {
                realData = this.AES.decode(aesData, aesKey);
            } catch (e) {
                console.log(e)
            }
            if (realData == null){
                return null;
            }
            var result = []
            for(var i = 0;i < head.length;i++){
                result.push(head[i])
            }
            for(var i = 0;i < realData.length;i++){
                result.push(realData[i])
            }
            return result;
        }else {
            return data;
        }
    },
    encrypt:function (data,encryptType,aesKey){
        if (encryptType == this.MessageEncryptType.MD5){
            var pathMd5 = this.md5(new Buffer(data));
            if (pathMd5 == null){
                return null;
            }
            var pathMd5Byte = ByteUtils.hexStringToByte(pathMd5)
            var result = []
            for(var i = 0;i < data.length;i++){
                result.push(data[i])
            }
            for(var i = 0;i < pathMd5Byte.length;i++){
                result.push(pathMd5Byte[i])
            }
            return result
        }else if (encryptType == this.MessageEncryptType.AES){
            if(!aesKey){
                return null;
            }
            var head = ByteUtils.arrayOfRange(data,0,15);
            var realData = ByteUtils.arrayOfRange(data,15,data.length);
            if (realData == null || realData.length == 0) {
                return data;
            }
            var aesData = null;
            try{
                aesData = this.AES.decode(realData,aesKey)
            }catch (e){
                console.log(e)
            }
            if (aesData == null){
                return null;
            }
            var result = []
            for(var i = 0;i < head.length;i++){
                result.push(head[i])
            }
            for(var i = 0;i < aesData.length;i++){
                result.push(aesData[i])
            }
            return result
        }else{
            return data;
        }
    }
}
module.exports = cryptoTool;



