/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Ac.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

public class Ac {
	private boolean status;
	
	public Ac(boolean s) {
		status = s;
	}
	
	public boolean getStatus() {
		return status;
	}
}
