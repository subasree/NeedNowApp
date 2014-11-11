package com.google.cloud.backend.neednow.sample.guestbook;

import java.util.List;

import android.app.Application;

import com.google.cloud.backend.neednow.core.CloudEntity;

public class CategoryList extends Application {

	List<CloudEntity> entities;
	CloudEntity myProfile;
	public void setCategoryList(List<CloudEntity> entity_list) {
		entities = entity_list;
	}

	public void setProfile(CloudEntity profile){
		myProfile = profile;
	}
	
	public List<CloudEntity> getCategoryList() {
		return entities;
	}
	public CloudEntity getProfile(){
		return myProfile;
	}

	public String getCategoryName(String key) {
		for (int i = 0; i < entities.size(); i++) {
			CloudEntity ce = entities.get(i);
			String entity_key = ce.getId();
			if (entity_key.equals(key))
				return ce.get("name").toString();
		}
		return "";
	}

}
