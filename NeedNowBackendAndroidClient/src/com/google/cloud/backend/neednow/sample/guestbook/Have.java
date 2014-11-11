package com.google.cloud.backend.neednow.sample.guestbook;

import com.google.cloud.backend.neednow.core.CloudEntity;

public class Have {
	private CloudEntity cloudEntity;

	public static final String KEY_ITEMNAME = "itemname";
	public static final String KEY_CATEGID = "categid";
	public static final String KEY_DESC = "desc";
	public static final String KEY_ZIPCODE = "zipcode";
	
	public Have(String itemname, String categid, String desc, String zipcode) {
		this.cloudEntity = new CloudEntity("Have");
		this.setItemName(itemname);
		this.setCategID(categid);
		this.setDesc(desc);
		this.setZipcode(zipcode);
	}
	
	public Have(CloudEntity e) {
		this.cloudEntity = e;
	}

	public CloudEntity asEntity() {
		return this.cloudEntity;
	}
	
	public String getItemName() {
		return (String) cloudEntity.get(KEY_ITEMNAME);
	}

	public void setItemName(String name) {
		cloudEntity.put(KEY_ITEMNAME, name);
	}
	
	public String getCategID() {
		return (String) cloudEntity.get(KEY_CATEGID);
	}

	public void setCategID(String categid) {
		cloudEntity.put(KEY_CATEGID, categid);
	}
	
	public String getDesc() {
		return (String) cloudEntity.get(KEY_DESC);
	}

	public void setDesc(String desc) {
		cloudEntity.put(KEY_DESC, desc);
	}
	
	public String getZipcode() {
		return (String) cloudEntity.get(KEY_ZIPCODE);
	}

	public void setZipcode(String zipcode) {
		cloudEntity.put(KEY_ZIPCODE, zipcode);
	}
}
