package com.remvp.library.bean;

import java.io.Serializable;

public class ViewMessage implements Serializable {
	/**
	 * 每个事件对应的id
	 */
	public int event;
	/**
	 * 事件所携带的信息
	 */
	public Object data;

	public ViewMessage() {
		super();
	}

	public ViewMessage(int event, Object data) {
		super();
		this.event = event;
		this.data = data;
	}

	@Override
	public String toString() {
		return "ViewMessage [event=" + event + ", data=" + data + "]";
	}

}
