package com.galapea.techblog.textstorage.entity;

import com.toshiba.mwcloud.gs.RowKey;
import java.util.Date;
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
