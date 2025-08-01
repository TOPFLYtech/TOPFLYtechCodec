using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class InnerGeoDataMessage : Message
    {
        private DateTime date;
        private List<InnerGeofence> geoList = new List<InnerGeofence>();
        private int lockGeofenceEnable;

        public int LockGeofenceEnable
        {
            get { return lockGeofenceEnable; }
            set { lockGeofenceEnable = value; }
        }

        public DateTime Date
        {
            get { return date; }
            set { date = value; }
        }

        public List<InnerGeofence> GeoList
        {
            get { return geoList; }
            set { geoList = value; }
        }

        public void AddGeoPoint(InnerGeofence geo)
        {
            this.geoList.Add(geo);
        }
    }
}
