package com.galapea.techblog.fitnesstracking.entity;

import java.util.Date;

import com.toshiba.mwcloud.gs.RowKey;

import lombok.Data;

@Data
public class Snippet {

	@RowKey
	String id;

	String title;

	String storageId;

	String userId;

	Date createdAt;

	String contentSizeHumanReadable;

}
