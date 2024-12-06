package com.galapea.techblog.textstorage.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EditSnippet {
    String id;
    String title;
    String content;
}
