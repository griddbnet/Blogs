package com.galapea.techblog.blogvoting.model;

import java.util.Date;
import lombok.Data;

@Data
public class VoteAggregate {
    private final Date timestamp;
    private final Long count;
    private final String label;
}
