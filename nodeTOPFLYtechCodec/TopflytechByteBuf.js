var ByteUtils = require("./ByteUtils")
var TopflytechByteBuf = {
    selfBuf:[4096],
    readIndex:0,
    writeIndex:0,
    capacity:4096,
    markerReadIndex:0,
    putBuf:function (inBuf){
        if (this.capacity - this.writeIndex >= inBuf.length){
            for (var i = 0;i < inBuf.length;i++,this.writeIndex++){
                this.selfBuf[this.writeIndex] = inBuf[i];
            }
        }else{
            if (this.capacity - this.writeIndex + this.readIndex >= inBuf.length){
                var currentDataLength = this.writeIndex - this.readIndex;
                for (var i = 0;i < currentDataLength;i++){
                    this.selfBuf[i] = this.selfBuf[this.readIndex + i];
                }
                this.writeIndex = currentDataLength;
                this.readIndex = 0;
                this.markerReadIndex = 0;
                for (var i = 0;i < inBuf.length;i++,this.writeIndex++){
                    this.selfBuf[this.writeIndex] = inBuf[i];
                }
            }else{
                var needLength = ((this.writeIndex - this.readIndex + inBuf.length) / 4096 + 1) * 4096;
                var tmp = [needLength];
                for (var i = 0 ;i < this.writeIndex - this.readIndex;i++){
                    tmp[i] = this.selfBuf[this.readIndex + i];
                }
                this.selfBuf = tmp;
                this.capacity = needLength;
                this.writeIndex = this.writeIndex - this.readIndex;
                this.readIndex = 0;
                this.markerReadIndex = 0;
                for (var i = 0;i < inBuf.length;i++,this.writeIndex++){
                    this.selfBuf[this.writeIndex] = inBuf[i];
                }
            }
        }
    },
    getReadableBytes:function (){
        return this.writeIndex - this.readIndex;
    },
    getReadIndex:function (){
        return this.readIndex
    },
    getByte:function (index){
        if (index >= this.writeIndex - this.readIndex){
            return '0';
        }
        return this.selfBuf[this.readIndex + index];
    },
    markReaderIndex:function (){
        this.markerReadIndex = this.readIndex;
    },
    resetReaderIndex:function (){
        this.readIndex = this.markerReadIndex;
    },
    skipBytes:function (length){
        this.readIndex += length;
    },
    readBytes:function (length){
        if (length > this.getReadableBytes()){
            return null;
        }
        var result = ByteUtils.arrayOfRange(this.selfBuf, this.readIndex, this.readIndex + length);
        this.readIndex += length;
        return result;
    }
}
module.exports = TopflytechByteBuf;
