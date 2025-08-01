const net = require('net');
const csv = require('csvtojson')
const iconv = require('iconv-lite');
const co = require("co")
const fs = require("fs")
function parseData(cb){
    const converter = csv()
        .fromFile('D:\\860112047166873.csv',{encoding:'binary'})
        .then((json) => {
            //binary和fromFile中的文件读取方式要一致
            var buf = new Buffer(JSON.stringify(json), 'binary');//第一个参数格式是字符串
            var str = iconv.decode(buf, 'GBK');//原文编码我这是GBK
            str=JSON.parse(JSON.stringify(str))//解码后为字符串，需要先转成json字符串
            var data=eval(str)
            //输出结果：[{id:'123456',content:'这是内容'}]
            cb(data)

        })
}
function  hexStringToByte(hexStr){
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
}
function replaceAll(input,findKey,replaceKey){
    while(input.indexOf(findKey) != -1){
        input = input.replace(findKey,replaceKey)
    }
    return input;
}
function parseOurSrcData(){
    var result = []
    try {
        // read contents of the file
        const data = fs.readFileSync('D:\\mnt\\867730059211163.txt', 'UTF-8');

        // split the contents by new line
        const lines = data.split(/\r?\n/);
        // print all lines
        lines.forEach((line) => {
            // console.log(line);
            if(line.indexOf("receive") != -1){
                var content = line.substr(43,line.length - 44)
                result.push(replaceAll(content," ",""))
            }

        });
    } catch (err) {
        console.error(err);
    }
    return result
}

const client = net.createConnection({
    port:1001,
    host:'192.168.1.53'
});
//当套字节与服务端连接成功时触发connect事件
client.on('connect', () =>{
    // client.write('他乡踏雪');//向服务端发送数据.
    co(function *(){
        var allData = []
        // yield function(next){
        //     parseData(function (data){
        //         for(var i in data){
        //             allData.push(data[i].payload)
        //         }
        //         next()
        //     })
        // }
        allData = parseOurSrcData()
        for(var i in allData){
            var item = allData[i]
            client.write(Buffer.from(hexStringToByte(item)))
            yield function (next){
                setTimeout(function (){
                    next()
                },1)
            }
        }
    })
});
//使用data事件监听服务端响应过来的数据
client.on('data', (chunk) => {
    console.log(chunk.toString());
});
client.on('error', (err)=>{
    console.log(err);
});
client.on('close', ()=>{
    console.log('客户端断开连接');
});



