using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    /// <summary>
    /// The type Accident acceleration message.Accident Data (AST Command Control, Default Disable the feature,From Tracker).Protocol number is 25 25 08.
    /// </summary>
    public class AccidentAccelerationMessage : Message
    {
        private List<AccelerationData> accelerationList;
        public List<AccelerationData> AccelerationList
        {
            get { return accelerationList; }
            set { accelerationList = value; }
        }
    }
}
