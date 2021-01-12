using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    /// <summary>
    /// The type Config message.When the server sends a configuration command to the device,
    /// the device returns the result of the setup. This class describes the setting result
    /// </summary>
    public class ConfigMessage : Message  
    {
        private String configResultContent;
	    public String ConfigResultContent
	    {
		    get { return configResultContent; }
		    set { configResultContent = value; }
	    }

    }
}
