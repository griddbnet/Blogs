package com.galapea.techblog.textstorage.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateSnippet {
    String title;
    String content;
    String userId;
}
