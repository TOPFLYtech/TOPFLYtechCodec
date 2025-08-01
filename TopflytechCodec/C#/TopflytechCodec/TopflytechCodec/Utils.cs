using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec
{
    class Utils
    {

        public static DateTime getGTM0Date(byte[] bytes, int startIndex)
        {
            String datetime = BytesUtils.GetDateStr(bytes, startIndex);
            int year = Convert.ToInt32(datetime.Substring(0, 4));
            int month = Convert.ToInt32(datetime.Substring(4, 2));
            int day = Convert.ToInt32(datetime.Substring(6, 2));
            int hour = Convert.ToInt32(datetime.Substring(8, 2));
            int minutes = Convert.ToInt32(datetime.Substring(10, 2));
            int seconds = Convert.ToInt32(datetime.Substring(12, 2));
            return new DateTime(year, month, day, hour, minutes, seconds, DateTimeKind.Utc);
        }

        public static bool ArrayEquals(byte[] b1, byte[] b2)
        {
            if (b1.Length != b2.Length) return false;
            if (b1 == null || b2 == null) return false;
            for (int i = 0; i < b1.Length; i++)
                if (b1[i] != b2[i])
                    return false;
            return true;
        }

        public static byte[] ArrayCopyOfRange(byte[] source,int from,int to)
        {
            byte[] result = new byte[to - from];
            Array.Copy(source, from, result, 0, to - from);
            return result;
        }
        
    }
}
