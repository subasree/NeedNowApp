package com.google.cloud.backend.neednow.sample.guestbook;

import java.util.ArrayList;
import java.util.List;

import com.google.cloud.backend.neednow.core.CloudEntity;

public class needlist {
	private CloudEntity cloudEntity;
	public static final String KEY_ITEMNAME = "itemname";
	
	public static final String KEY_IDLIST = "idlist";
	
	public needlist(String itemname, ArrayList<String> idlist) {
		this.cloudEntity = new CloudEntity("needlist");
		this.setItemName(itemname);
		this.setIdList(idlist);
		
	}
	public needlist(CloudEntity e) {
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
	
	public void setIdList(List<String> idlist) {
		cloudEntity.put(KEY_IDLIST, idlist);
	}
	
	public ArrayList<String> getIdList() {
		return (ArrayList<String>) cloudEntity.get(KEY_IDLIST);
	}
	
}
