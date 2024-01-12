import { Box, Divider, Typography } from "@mui/material";
import React from "react";
import { deleteIcon, editIcon, singlePlanIcon } from "../utils/icons";

const Plan = ({ planName, lastUpdated }) => {
  return (
    <Box>
      <Box>
        {singlePlanIcon()}
        <Box>
          <Typography variant="h6">{planName}</Typography>
          <Typography>Last edit was {lastUpdated} ago</Typography>
        </Box>
      </Box>
      <Box>
        <Box>
          {editIcon()}
          <Typography>Edit</Typography>
        </Box>
        <Divider orientation="vertical" />
        <Box>
          {deleteIcon()}
          <Typography>Delete</Typography>
        </Box>
      </Box>
    </Box>
  );
};

export default Plan;
