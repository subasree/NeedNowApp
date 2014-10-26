package com.google.cloud.backend.neednow.sample.guestbook;

import java.util.Date;

import com.google.cloud.backend.neednow.core.CloudEntity;

public class Profile {
	private CloudEntity cloudEntity;
	
	public static final String KEY_NAME = "name";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_ZIPCODE = "zipcode";
	public static final String KEY_DISCLOSEINFO = "discloseinfo";
	public static final String KEY_ACTIVE = "active";
	
	public Profile(String name, String phone, String zipcode, boolean discloseinfo, boolean active) {
		this.cloudEntity = new CloudEntity("Profile");
		this.setName(name);
		this.setPhone(phone);
		this.setZipcode(zipcode);
		this.setDiscloseinfo(discloseinfo);
		this.setActive(active);
	}
	
	public Profile(CloudEntity e) {
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

	public String getPhone() {
		return (String) cloudEntity.get(KEY_PHONE);
	}

	public void setPhone(String phone) {
		cloudEntity.put(KEY_PHONE, phone);
	}

	public String getZipcode() {
		return (String) cloudEntity.get(KEY_ZIPCODE);
	}

	public void setZipcode(String zipcode) {
		cloudEntity.put(KEY_ZIPCODE, zipcode);
	}
	
	public boolean getDiscloseinfo() {
		return (Boolean) cloudEntity.get(KEY_DISCLOSEINFO);
	}

	public void setDiscloseinfo(boolean discloseinfo) {
		cloudEntity.put(KEY_DISCLOSEINFO, discloseinfo);
	}
	
	public boolean getActive() {
		return (Boolean) cloudEntity.get(KEY_ACTIVE);
	}

	public void setActive(boolean active) {
		cloudEntity.put(KEY_ACTIVE, active);
	}

	public Date getUpdatedAt() {
		return cloudEntity.getUpdatedAt();
	}

	
}
