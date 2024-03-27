package com.galapea.techblog.blogvoting.entity;

import java.util.Date;
import lombok.Data;

@Data
public class VoteDownMetric {
    Date timestamp;
    String blogId;
    String userId;
}
