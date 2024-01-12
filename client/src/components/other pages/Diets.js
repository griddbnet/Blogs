import { Box } from "@mui/material";
import React from "react";
import DesktopWeeks from "./DesktopWeeks";

const Diets = ({ calories, fat, cabs, protein }) => {
  return (
    <Box>
      {Array.from({ length: 6 }, (_, i) => (
        <DesktopWeeks
          key={i}
          calories={calories}
          fat={fat}
          cabs={cabs}
          protein={protein}
          i={i}
        />
      ))}
    </Box>
  );
};

export default Diets;
