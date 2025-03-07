package com.galapea.techblog.fitnesstracking.entity;

import java.util.Date;

import com.toshiba.mwcloud.gs.RowKey;

import lombok.Data;

@Data
public class User {

	@RowKey
	String id;

	String email;

	String fullName;

	Date createdAt;

}
