using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    /// <summary>
    /// Driver Behavior Via Acceleration (AST Command Control, Default Disable the feature).Protocol number is 25 25 07.
    /// </summary>
    public class AccelerationDriverBehaviorMessage : Message
    {
        protected AccelerationData accelerationData;
        public TopflytechCodec.Entities.AccelerationData AccelerationData
        {
            get { return accelerationData; }
            set { accelerationData = value; }
        }
        private int behaviorType;
        public int BehaviorType
        {
            get { return behaviorType; }
            set { behaviorType = value; }
        }
        
        /// <summary>
        /// The constant BEHAVIOR_TURN_AND_BRAKE.
        /// </summary>
        public static int BEHAVIOR_TURN_AND_BRAKE = 0;

        /// <summary>
        ///  The constant BEHAVIOR_ACCELERATE.
        /// </summary>
        public static int BEHAVIOR_ACCELERATE = 1;

    }
}
