using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    /// <summary>
    /// Driver Behavior Via GPS (AST Command Control, Default Disable the feature).Protocol number is 25 25 05.
    /// </summary>
    public class GpsDriverBehaviorMessage : Message
    {
        private int behaviorType;
        public int BehaviorType
        {
            get { return behaviorType; }
            set { behaviorType = value; }
        }
        private DateTime startDate;
        public System.DateTime StartDate
        {
            get { return startDate; }
            set { startDate = value; }
        }
        private double startLatitude;
        public double StartLatitude
        {
            get { return startLatitude; }
            set { startLatitude = value; }
        }
        private double startLongitude;
        public double StartLongitude
        {
            get { return startLongitude; }
            set { startLongitude = value; }
        }
        private double startAltitude;
        public double StartAltitude
        {
            get { return startAltitude; }
            set { startAltitude = value; }
        }
        private float startSpeed;
        public float StartSpeed
        {
            get { return startSpeed; }
            set { startSpeed = value; }
        }
        private int startAzimuth;
        public int StartAzimuth
        {
            get { return startAzimuth; }
            set { startAzimuth = value; }
        }
        private DateTime endDate;
        public System.DateTime EndDate
        {
            get { return endDate; }
            set { endDate = value; }
        }
        private double endLatitude;
        public double EndLatitude
        {
            get { return endLatitude; }
            set { endLatitude = value; }
        }
        private double endLongitude;
        public double EndLongitude
        {
            get { return endLongitude; }
            set { endLongitude = value; }
        }
        private double endAltitude;
        public double EndAltitude
        {
            get { return endAltitude; }
            set { endAltitude = value; }
        }
        private float endSpeed;
        public float EndSpeed
        {
            get { return endSpeed; }
            set { endSpeed = value; }
        }
        private int endAzimuth;
        public int EndAzimuth
        {
            get { return endAzimuth; }
            set { endAzimuth = value; }
        }
        private int startRpm;

        public int StartRpm
        {
            get { return startRpm; }
            set { startRpm = value; }
        }
        private int endRpm;

        public int EndRpm
        {
            get { return endRpm; }
            set { endRpm = value; }
        }
    }
}
