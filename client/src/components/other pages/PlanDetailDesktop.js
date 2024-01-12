import React, { Fragment } from "react";
import { Date } from "../assets/data";
import { Box, Divider } from "@mui/material";
import Diets from "./Diets";
import Multiplefood from "./Multiplefood";
import DaysOfTheWeek from "./DaysOfTheWeek";

const PlanDetailDesktop = ({ singlePlan }) => {
  const [
    uid,
    title,
    calories,
    fat,
    cabs,
    protein,
    days,
    breakfast,
    lunch,
    dinner,
    snack1,
    snack2,
    snack3,
  ] = singlePlan;

  return (
    <Box
      sx={{
        backgroundColor: "rgba(244, 244, 244, 1)",
        display: {
          xs: "none",
          md: "flex",
          flexDirection: "column",
          alignItems: "flex-end",
        },
        width: "100%",
        padding: "20px",
      }}
    >
      <Box
        component="section"
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          height: "200px",
          width: "90%",
          backgroundColor: "rgba(255, 255, 255, 1)",
          borderRadius: "9px",
          padding: "0 30px",
        }}
      >
        {Array.from({ length: 7 }, (_, i) => (
          <Fragment key={i}>
            <Diets
              calories={calories}
              fat={fat}
              cabs={cabs}
              protein={protein}
            />
            {i !== 6 && (
              <Divider orientation="vertical" sx={{ height: "150px" }} />
            )}
          </Fragment>
        ))}
      </Box>

      <Box
        component="section"
        sx={{
          display: "flex",
          justifyContent: "space-around",
          marginTop: "20px",
          alignItems: "center",
          borderRadius: "10px",
          width: "90%",
          height: "60px",
          backgroundColor: "rgba(135, 197, 49, 1)",
        }}
      >
        {Date.map((item, i) => (
          <DaysOfTheWeek key={i} {...item} />
        ))}
      </Box>

      <Box
        component="section"
        sx={{
          display: "flex",
          justifyContent: "center",
          gap: "10px",
          margin: "20px 0",
          width: "90%",
        }}
      >
        {Array.from({ length: 7 }, (_, i) => (
          <Multiplefood
            days={days}
            breakfast={breakfast}
            lunch={lunch}
            dinner={dinner}
            snack1={snack1}
            snack2={snack2}
            snack3={snack3}
            i={i}
            key={i}
          />
        ))}
      </Box>
    </Box>
  );
};

export default PlanDetailDesktop;
