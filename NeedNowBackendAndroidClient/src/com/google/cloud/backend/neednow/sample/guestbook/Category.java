package com.google.cloud.backend.neednow.sample.guestbook;

import java.util.Date;

import com.google.cloud.backend.neednow.core.CloudEntity;

public class Category {
	private CloudEntity cloudEntity;
	public static final String KEY_NAME = "name";
	public Category(String name) {
		this.cloudEntity = new CloudEntity("Category");
		this.setName(name);
	}
	public Category(CloudEntity e) {
		this.cloudEntity = e;
	}

	public CloudEntity asEntity() {
		return this.cloudEntity;
	}
	
	public String getName() {
		return (String) cloudEntity.get(KEY_NAME);
	}

	public void setName(String name) {
		cloudEntity.put(KEY_NAME, name);
	}
	public Date getUpdatedAt() {
		return cloudEntity.getUpdatedAt();
	}

}
