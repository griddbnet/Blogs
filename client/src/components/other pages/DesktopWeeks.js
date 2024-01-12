import { Box, Typography } from "@mui/material";
import React from "react";

const DesktopWeeks = ({ calories, fat, cabs, protein, i }) => {
  const backgroundColors = [
    "#FF922E",
    "#FF5959",
    "#5C63FF",
    "#87C531",
    "#5C63FF",
    "#87C531",
  ];

  return (
    <Box
      className="weeks"
      sx={{
        display: "flex",
        alignItems: "center",
        justifyContent: "space-around",
        gap: "8px",
      }}
    >
      <Box
        sx={{
          width: "9px",
          height: "9px",
          transform: "rotate(-45deg)",
          transformOrigin: "0 0",
          backgroundColor: backgroundColors[i],
          borderRadius: "2px",
        }}
      ></Box>
      <Box
        sx={{
          width: "100px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <Typography
          component="span"
          sx={{
            color: "rgba(0, 0, 0, 1)",
            fontSize: "12px",
            fontWeight: "400",
          }}
        >
          {i === 0
            ? "Calories"
            : i === 1
            ? "Fat"
            : i === 2
            ? "Carbs"
            : i === 3
            ? "Fiber"
            : i === 4
            ? "Sugar"
            : "Protein"}
        </Typography>
        <Typography
          component="span"
          sx={{
            color: "rgba(177, 177, 177, 1)",
            paddingLeft: "30px",
            fontSize: "10px",
          }}
        >
          {i === 0
            ? calories
            : i === 1
            ? fat
            : i === 2
            ? cabs
            : i === 3
            ? fat + cabs
            : i === 4
            ? cabs + calories
            : protein}
          g
        </Typography>
      </Box>
    </Box>
  );
};

export default DesktopWeeks;
