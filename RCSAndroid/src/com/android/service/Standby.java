/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Standby.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

public class Standby {
	private boolean status;

	public Standby(boolean s) {
		status = s;
	}

	public boolean getStatus() {
		return status;
	}
}
