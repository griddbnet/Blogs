import { AppBar, Box, Button, Divider, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import React from "react";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";

const Planaddplan = () => {
  const { plans } = useSelector((state) => state.plans);

  return (
    <AppBar
      position="static"
      sx={{
        backgroundColor: "rgba(255, 255, 255, 1)",
        display: "flex",
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "10px 40px",
      }}
    >
      <Box sx={{ display: "flex" }}>
        <Divider
          orientation="vertical"
          sx={{
            width: "5px",
            height: "15px",
            backgroundColor: "rgba(134, 197, 47, 1)",
            border: "none",
            margin: "0 8px",
            marginTop: "4px",
          }}
        />
        <Box>
          <Typography
            sx={{
              color: "rgba(73, 73, 73, 1)",
              fontWeight: "400",
              fontSize: "16px",
            }}
          >
            Add Plan
          </Typography>
          <Typography
            sx={{
              color: "rgba(192, 192, 192, 1)",
              fontWeight: "400",
              fontSize: "13px",
            }}
          >
            {plans.length} plans
          </Typography>
        </Box>
      </Box>
      <Box>
        <Link to="add-plan">
          <Button
            sx={{
              backgroundColor: "rgba(134, 197, 47, 1)",
              borderRadius: "7px",
              padding: "10px 20px",
            }}
          >
            <AddIcon sx={{ color: "rgba(255, 255, 255, 1)" }} />
            <Typography
              sx={{
                color: "rgba(255, 255, 255, 1)",
                textTransform: "capitalize",
              }}
            >
              Add plan
            </Typography>
          </Button>
        </Link>
      </Box>
    </AppBar>
  );
};

export default Planaddplan;
