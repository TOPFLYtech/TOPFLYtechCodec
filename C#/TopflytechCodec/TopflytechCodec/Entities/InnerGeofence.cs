using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class InnerGeofence
    {
        private int id;
        private int type;
        private float radius = -1;
        private List<double[]> points = new List<double[]>();

        public int Id
        {
            get { return id; }
            set { id = value; }
        }

        public int Type
        {
            get { return type; }
            set { type = value; }
        }

        public float Radius
        {
            get { return radius; }
            set { radius = value; }
        }

        public List<double[]> Points
        {
            get { return points; }
            set { points = value; }
        }

        public void AddPoint(double lat, double lng)
        {
            this.points.Add(new double[] { lat, lng });
        }
    }
}
