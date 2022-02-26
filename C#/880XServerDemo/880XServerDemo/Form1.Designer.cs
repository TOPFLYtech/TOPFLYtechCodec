namespace _880XServerDemo
{
    partial class topflytech880xServer
    {
        /// <summary>
        /// 必需的设计器变量。
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// 清理所有正在使用的资源。
        /// </summary>
        /// <param name="disposing">如果应释放托管资源，为 true；否则为 false。</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows 窗体设计器生成的代码

        /// <summary>
        /// 设计器支持所需的方法 - 不要
        /// 使用代码编辑器修改此方法的内容。
        /// </summary>
        private void InitializeComponent()
        {
            this.lbOnline = new System.Windows.Forms.ListBox();
            this.label3 = new System.Windows.Forms.Label();
            this.txtMsg = new System.Windows.Forms.TextBox();
            this.btnBeginListen = new System.Windows.Forms.Button();
            this.txtPort = new System.Windows.Forms.TextBox();
            this.label2 = new System.Windows.Forms.Label();
            this.txtIP = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.btnStopServer = new System.Windows.Forms.Button();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.rbObd = new System.Windows.Forms.RadioButton();
            this.rbNoObd = new System.Windows.Forms.RadioButton();
            this.rbPersonal = new System.Windows.Forms.RadioButton();
            this.groupBox1.SuspendLayout();
            this.SuspendLayout();
            // 
            // lbOnline
            // 
            this.lbOnline.FormattingEnabled = true;
            this.lbOnline.ItemHeight = 12;
            this.lbOnline.Location = new System.Drawing.Point(12, 76);
            this.lbOnline.Name = "lbOnline";
            this.lbOnline.Size = new System.Drawing.Size(134, 316);
            this.lbOnline.TabIndex = 18;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(12, 40);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(53, 12);
            this.label3.TabIndex = 17;
            this.label3.Text = "Online：";
            // 
            // txtMsg
            // 
            this.txtMsg.Location = new System.Drawing.Point(165, 69);
            this.txtMsg.Multiline = true;
            this.txtMsg.Name = "txtMsg";
            this.txtMsg.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.txtMsg.Size = new System.Drawing.Size(507, 330);
            this.txtMsg.TabIndex = 13;
            // 
            // btnBeginListen
            // 
            this.btnBeginListen.Location = new System.Drawing.Point(569, 8);
            this.btnBeginListen.Name = "btnBeginListen";
            this.btnBeginListen.Size = new System.Drawing.Size(103, 23);
            this.btnBeginListen.TabIndex = 12;
            this.btnBeginListen.Text = "Start Server";
            this.btnBeginListen.UseVisualStyleBackColor = true;
            this.btnBeginListen.Click += new System.EventHandler(this.btnBeginListen_Click);
            // 
            // txtPort
            // 
            this.txtPort.Location = new System.Drawing.Point(181, 10);
            this.txtPort.Name = "txtPort";
            this.txtPort.Size = new System.Drawing.Size(44, 21);
            this.txtPort.TabIndex = 10;
            this.txtPort.Text = "1001";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(143, 13);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(35, 12);
            this.label2.TabIndex = 8;
            this.label2.Text = "Port:";
            // 
            // txtIP
            // 
            this.txtIP.Location = new System.Drawing.Point(35, 10);
            this.txtIP.Name = "txtIP";
            this.txtIP.Size = new System.Drawing.Size(100, 21);
            this.txtIP.TabIndex = 11;
            this.txtIP.Text = "192.168.1.53";
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(10, 13);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(23, 12);
            this.label1.TabIndex = 9;
            this.label1.Text = "IP:";
            // 
            // btnStopServer
            // 
            this.btnStopServer.Location = new System.Drawing.Point(569, 40);
            this.btnStopServer.Name = "btnStopServer";
            this.btnStopServer.Size = new System.Drawing.Size(103, 23);
            this.btnStopServer.TabIndex = 19;
            this.btnStopServer.Text = "Stop Server";
            this.btnStopServer.UseVisualStyleBackColor = true;
            this.btnStopServer.Click += new System.EventHandler(this.btnStopServer_Click);
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.rbObd);
            this.groupBox1.Controls.Add(this.rbNoObd);
            this.groupBox1.Controls.Add(this.rbPersonal);
            this.groupBox1.Location = new System.Drawing.Point(254, 8);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(254, 44);
            this.groupBox1.TabIndex = 44;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Protocol";
            // 
            // rbObd
            // 
            this.rbObd.AutoSize = true;
            this.rbObd.Location = new System.Drawing.Point(75, 13);
            this.rbObd.Name = "rbObd";
            this.rbObd.Size = new System.Drawing.Size(41, 16);
            this.rbObd.TabIndex = 18;
            this.rbObd.Text = "OBD";
            this.rbObd.UseVisualStyleBackColor = true;
            // 
            // rbNoObd
            // 
            this.rbNoObd.AutoSize = true;
            this.rbNoObd.Checked = true;
            this.rbNoObd.Location = new System.Drawing.Point(16, 13);
            this.rbNoObd.Name = "rbNoObd";
            this.rbNoObd.Size = new System.Drawing.Size(53, 16);
            this.rbNoObd.TabIndex = 17;
            this.rbNoObd.TabStop = true;
            this.rbNoObd.Text = "NoObd";
            this.rbNoObd.UseVisualStyleBackColor = true;
            // 
            // rbPersonal
            // 
            this.rbPersonal.AutoSize = true;
            this.rbPersonal.Location = new System.Drawing.Point(122, 13);
            this.rbPersonal.Name = "rbPersonal";
            this.rbPersonal.Size = new System.Drawing.Size(71, 16);
            this.rbPersonal.TabIndex = 16;
            this.rbPersonal.Text = "Personal";
            this.rbPersonal.UseVisualStyleBackColor = true;
            // 
            // topflytech880xServer
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(684, 442);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.btnStopServer);
            this.Controls.Add(this.lbOnline);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.txtMsg);
            this.Controls.Add(this.btnBeginListen);
            this.Controls.Add(this.txtPort);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.txtIP);
            this.Controls.Add(this.label1);
            this.Name = "topflytech880xServer";
            this.Text = "Topflytech 880XServer V1.0";
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.ListBox lbOnline;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox txtMsg;
        private System.Windows.Forms.Button btnBeginListen;
        private System.Windows.Forms.TextBox txtPort;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox txtIP;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button btnStopServer;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.RadioButton rbObd;
        private System.Windows.Forms.RadioButton rbNoObd;
        private System.Windows.Forms.RadioButton rbPersonal;
    }
}

