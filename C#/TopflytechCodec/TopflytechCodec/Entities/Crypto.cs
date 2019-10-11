using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Security.Cryptography;
using System.IO;

namespace TopflytechCodec.Entities
{
    class Crypto
    {
        private const String ivParam = "topflytech201205";
        public static byte[] AESEncrypt(byte[] toEncrypt, string key)
        {
            byte[] keyArray = System.Text.Encoding.Default.GetBytes(key);
            byte[] ivArray = System.Text.Encoding.Default.GetBytes(ivParam);
            byte[] toEncryptArray = toEncrypt;
            RijndaelManaged rDel = new RijndaelManaged();
            MD5 md5 = new MD5CryptoServiceProvider();
            byte[] output = md5.ComputeHash(keyArray);
            rDel.Key = output;
            rDel.IV = ivArray;
            rDel.Mode = CipherMode.CBC;
            rDel.Padding = PaddingMode.PKCS7;
            ICryptoTransform cTransform = rDel.CreateEncryptor();
            byte[] resultArray = cTransform.TransformFinalBlock(toEncryptArray, 0, toEncryptArray.Length);
            return resultArray;
        }
        public static byte[] AESDecrypt(byte[] toDecrypt, string key)
        {
            byte[] keyArray = System.Text.Encoding.Default.GetBytes(key);
            byte[] ivArray = System.Text.Encoding.Default.GetBytes(ivParam);
            byte[] toEncryptArray = toDecrypt;
            MD5 md5 = new MD5CryptoServiceProvider();
            byte[] output = md5.ComputeHash(keyArray);
            RijndaelManaged rDel = new RijndaelManaged();
            rDel.Key = output;
            rDel.IV = ivArray;
            rDel.Mode = CipherMode.CBC;
            rDel.Padding = PaddingMode.PKCS7;
            ICryptoTransform cTransform = rDel.CreateDecryptor();
            byte[] resultArray = cTransform.TransformFinalBlock(toEncryptArray, 0, toEncryptArray.Length);
            return resultArray;
        }


        public static byte[] MD5(byte[] data)
        { 
            RijndaelManaged rDel = new RijndaelManaged();
            MD5 md5 = new MD5CryptoServiceProvider();
            byte[] output = md5.ComputeHash(data);
            byte[] result = new byte[8];
            Array.Copy(output, 4, result, 0, 8);
            return result; 
        }

        public static int GetAesLength(int packageLength)
        {
            if (packageLength <= 15)
            {
                return packageLength;
            }
            return ((packageLength - 15) / 16 + 1) * 16 + 15;
        }


        public static byte[] DecryptData(byte[] data, int encryptType, string aesKey)
        {
            if (encryptType == MessageEncryptType.MD5)
            {
                byte[] realData = new byte[data.Length - 8];
                byte[] md5Data = new byte[8];
                Array.Copy(data, 0, realData, 0, data.Length - 8);
                Array.Copy(data, data.Length - 8, md5Data, 0, 8);
                byte[] pathMd5 = Crypto.MD5(realData);
                if (pathMd5 == null)
                {
                    return null;
                }
                if (!Utils.ArrayEquals(md5Data, pathMd5))
                {
                    return null;
                }
                return realData;
            }
            else if (encryptType == MessageEncryptType.AES)
            {
                if (data.Length <= 15)
                {
                    return data;
                }
                byte[] head = new byte[15];
                byte[] aesData = new byte[data.Length - 15];
                Array.Copy(data, 0, head, 0, 15);
                Array.Copy(data, 15, aesData, 0, data.Length - 15);
                byte[] realData = Crypto.AESDecrypt(aesData, aesKey);
                if (realData == null)
                {
                    return null;
                }
                MemoryStream memoryStream = new MemoryStream();
                try
                {
                    memoryStream.Write(head, 0, head.Length);
                    memoryStream.Write(realData, 0, realData.Length);
                    return memoryStream.ToArray();
                }
                finally
                {
                    memoryStream.Close();
                }
            }
            else
            {
                return data;
            }
        }
    }
}
