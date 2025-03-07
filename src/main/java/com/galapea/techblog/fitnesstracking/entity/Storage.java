package com.galapea.techblog.fitnesstracking.entity;

import java.sql.Blob;

import com.toshiba.mwcloud.gs.RowKey;

import lombok.Data;

@Data
public class Storage {

	@RowKey
	String id;

	Blob content;

}
