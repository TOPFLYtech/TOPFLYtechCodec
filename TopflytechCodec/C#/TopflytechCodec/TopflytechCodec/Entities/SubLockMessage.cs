using System;

namespace TopflytechCodec.Entities
{
    public class SubLockMessage : Message
    {
        public bool LatlngValid { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public double Altitude { get; set; }
        public float Speed { get; set; }
        public int Azimuth { get; set; }
        public int LockType { get; set; }
        public string LockId { get; set; }
        public DateTime Date { get; set; }

        public bool GpsWorking { get; set; }
        public int SatelliteNumber { get; set; }
        public bool IsHistoryData { get; set; }

        public bool Is_4g_lbs { get; set; }
        public int Mcc_4g { get; set; }
        public int Mnc_4g { get; set; }
        public long Eci_4g { get; set; }
        public int Pcid_4g_1 { get; set; }
        public int Pcid_4g_2 { get; set; }
        public int TAC { get; set; }
        public int Pcid_4g_3 { get; set; }

        public bool Is_2g_lbs { get; set; }
        public int Mcc_2g { get; set; }
        public int Mnc_2g { get; set; }
        public int Lac_2g_1 { get; set; }
        public int Ci_2g_1 { get; set; }
        public int Lac_2g_2 { get; set; }
        public int Ci_2g_2 { get; set; }
        public int Lac_2g_3 { get; set; }
        public int Ci_2g_3 { get; set; }

        public int Rssi { get; set; }
        public string Hardware { get; set; }
        public string Software { get; set; }
        public float Voltage { get; set; }
        public float SolarVoltage { get; set; }
        public float Temp { get; set; }
        public string DeviceId { get; set; }

        public bool IsCharging { get; set; }
        public bool IsChargingOverVoltage { get; set; }
        public bool IsLowPower { get; set; }
        public bool IsHighTemp { get; set; }
        public bool IsLowTemp { get; set; }
        public bool IsOpenLockCover { get; set; }
        public bool IsOpenBackCover { get; set; }
        public bool IsGpsPosition { get; set; }
        public bool IsGpsJamming { get; set; }
        public byte AlarmByte { get; set; }
    }
}
