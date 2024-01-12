import React from "react";
import { AppBar, Box, Divider, styled, Typography } from "@mui/material";
import ArrowBackIosNewIcon from "@mui/icons-material/ArrowBackIosNew";
import Section from "../components/other pages/Addplansectn";

const Typostyle = styled(Typography)({
  display: "flex",
  alignItems: "center",
});

const AddPlan = () => {
  return (
    <Box
      component="main"
      sx={{
        display: "flex",
        justifyContent: "flex-end",
        backgroundColor: "rgba(244, 244, 244, 1)",
        flexDirection: "column",
        alignItems: "center",
      }}
    >
      <AppBar
        position="static"
        sx={{
          width: { xs: "100%", md: "83%" },
          backgroundColor: "rgba(255, 255, 255, 1)",
        }}
      >
        <Typostyle
          variant="h6"
          sx={{
            color: "rgba(73, 73, 73, 1)",
            padding: "20px 30px",
            fontWeight: "400",
          }}
        >
          <ArrowBackIosNewIcon
            sx={{
              marginRight: "20px",
              display: { md: "none" },
              fontSize: "22px",
            }}
          />
          <Divider
            orientation="vertical"
            sx={{
              width: "5px",
              height: "20px",
              backgroundColor: "rgba(134, 197, 47, 1)",
              border: "none",
              margin: "0 10px",
            }}
          />
          Add New Plan
        </Typostyle>
      </AppBar>

      <Box
        sx={{
          width: { xs: "100%", md: "78%" },
          backgroundColor: "rgba(255, 255, 255, 1)",
          marginTop: "100px",
          padding: { xs: "0 20px", md: "40px" },
          marginRight: { md: "30px" },
          marginBottom: { md: "30px" },
          borderRadius: { md: "10px" },
        }}
      >
        <Box
          sx={{
            display: { xs: "none", md: "flex", alignItems: "center" },
          }}
        >
          <Divider
            orientation="vertical"
            sx={{
              width: "2px",
              height: "15px",
              backgroundColor: "rgba(134, 197, 47, 1)",
              border: "none",
              margin: "0 10px",
            }}
          />
          <Typography
            variant="p"
            component="p"
            sx={{
              display: { xs: "none", md: "flex", alignItems: "center" },
              fontSize: "15px",
            }}
          >
            Please add your new plan
          </Typography>
        </Box>

        <Section />
      </Box>
    </Box>
  );
};

export default AddPlan;
