using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class DeviceTempCollectionMessage : Message
    {
        private int interval;
        private List<float> tempList;
        private DateTime date;
        private int type = 1; // 1:temp

        public int Type
        {
            get { return type; }
            set { type = value; }
        }

        public DateTime Date
        {
            get { return date; }
            set { date = value; }
        }

        public int Interval
        {
            get { return interval; }
            set { interval = value; }
        }

        public List<float> TempList
        {
            get { return tempList; }
            set { tempList = value; }
        }
    }
}
