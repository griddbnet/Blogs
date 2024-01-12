import React from "react";
import { Box } from "@mui/material";
import Planaddplan from "../components/other pages/Planaddplan";
import Planlistmain from "../components/other pages/Planlistmain";

const PlanList = () => {
  return (
    <Box
      component="main"
      sx={{ backgroundColor: { md: "rgba(244, 244, 244, 1)" } }}
    >
      <Box
        component="section"
        sx={{
          width: "95%",
          display: { xs: "none", md: "block" },
        }}
      >
        <Planaddplan />
      </Box>
      <Box
        component="section"
        sx={{
          margin: { md: "30px 0" },
          marginLeft: { md: "30px" },
        }}
      >
        <Planlistmain />
      </Box>
    </Box>
  );
};

export default PlanList;
