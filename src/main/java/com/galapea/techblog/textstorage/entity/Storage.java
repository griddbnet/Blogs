package com.galapea.techblog.textstorage.entity;

import com.toshiba.mwcloud.gs.RowKey;
import java.sql.Blob;
import lombok.Data;

@Data
public class Storage {
    @RowKey
    String id;

    Blob content;
}
