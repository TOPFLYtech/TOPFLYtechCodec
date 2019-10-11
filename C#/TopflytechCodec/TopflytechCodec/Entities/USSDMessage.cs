using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    /// <summary>
    /// The type USSD message.New devices like 8806+ support this message.Old device like 8806,8803Pro does not support this message.
    /// </summary>
    public class USSDMessage : Message
    {
        private String content;
        public String Content
        {
            get { return content; }
            set { content = value; }
        }
    }
}
