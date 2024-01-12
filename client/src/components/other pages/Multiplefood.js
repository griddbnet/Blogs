import { Box, Card, Typography } from "@mui/material";
import React from "react";
import Union from "../assets/images/Union.png";
import healingTumeric from "../assets/images/healing-turmeric-cauliflower-soup.png";
import snack1Img from "../assets/images/snack1.png";
import lunchImg from "../assets/images/lunch.png";
import snack2Img from "../assets/images/snack2.png";
import dinnerImg from "../assets/images/dinner.png";
import snack3Img from "../assets/images/snack3.png";
import { getValidDayIndices } from "../assets/checkDays";

const Multiplefood = ({
  days,
  breakfast,
  lunch,
  dinner,
  snack1,
  snack2,
  snack3,
  i,
}) => {
  let gottenDaysIndex = getValidDayIndices(days);

  return (
    <Box>
      <Card
        orientation="horizontal"
        variant="outlined"
        sx={{
          width: "100%",
          minHeight: 100,
          padding: "15px",
          display: "flex",
          flexDirection: "column",
          margin: "10px 0",
          opacity: gottenDaysIndex.includes(i) ? "1" : "0.2",
        }}
      >
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "10px 0",
          }}
        >
          <Typography component="h4">Breakfast</Typography>
          <img src={Union} alt="" width={5} height={15} />
        </Box>
        <Box sx={{ borderRadius: "20px" }}>
          <img src={healingTumeric} alt="" width="100%" height={50} />
        </Box>
        <Typography
          component="span"
          sx={{ fontSize: "12px", color: "rgba(80, 80, 80, 1)" }}
        >
          {breakfast}
        </Typography>
      </Card>
      <Card
        orientation="horizontal"
        variant="outlined"
        sx={{
          width: "100%",
          minHeight: 100,
          padding: "15px",
          display: "flex",
          flexDirection: "column",
          margin: "10px 0",
          opacity: gottenDaysIndex.includes(i) ? "1" : "0.2",
        }}
      >
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "10px 0",
          }}
        >
          <Typography component="h4">Snack1</Typography>
          <img src={Union} alt="" width={5} height={15} />
        </Box>
        <Box sx={{ borderRadius: "20px" }}>
          <img src={snack1Img} alt="" width="100%" height={50} />
        </Box>
        <Typography
          component="span"
          sx={{ fontSize: "12px", color: "rgba(80, 80, 80, 1)" }}
        >
          {snack1}
        </Typography>
      </Card>
      <Card
        orientation="horizontal"
        variant="outlined"
        sx={{
          width: "100%",
          minHeight: 100,
          padding: "15px",
          display: "flex",
          flexDirection: "column",
          margin: "10px 0",
          opacity: gottenDaysIndex.includes(i) ? "1" : "0.2",
        }}
      >
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "10px 0",
          }}
        >
          <Typography component="h4">Lunch</Typography>
          <img src={Union} alt="" width={5} height={15} />
        </Box>
        <Box sx={{ borderRadius: "20px" }}>
          <img src={lunchImg} alt="" width="100%" height={50} />
        </Box>
        <Typography
          component="span"
          sx={{ fontSize: "12px", color: "rgba(80, 80, 80, 1)" }}
        >
          {lunch}
        </Typography>
      </Card>
      <Card
        orientation="horizontal"
        variant="outlined"
        sx={{
          width: "100%",
          minHeight: 100,
          padding: "15px",
          display: "flex",
          flexDirection: "column",
          margin: "10px 0",
          opacity: gottenDaysIndex.includes(i) ? "1" : "0.2",
        }}
      >
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "10px 0",
          }}
        >
          <Typography component="h4">Snack2</Typography>
          <img src={Union} alt="" width={5} height={15} />
        </Box>
        <Box sx={{ borderRadius: "20px" }}>
          <img src={snack2Img} alt="" width="100%" height={50} />
        </Box>
        <Typography
          component="span"
          sx={{ fontSize: "12px", color: "rgba(80, 80, 80, 1)" }}
        >
          {snack2}
        </Typography>
      </Card>
      <Card
        orientation="horizontal"
        variant="outlined"
        sx={{
          width: "100%",
          minHeight: 100,
          padding: "15px",
          display: "flex",
          flexDirection: "column",
          margin: "10px 0",
          opacity: gottenDaysIndex.includes(i) ? "1" : "0.2",
        }}
      >
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "10px 0",
          }}
        >
          <Typography component="h4">Dinner</Typography>
          <img src={Union} alt="" width={5} height={15} />
        </Box>
        <Box sx={{ borderRadius: "20px" }}>
          <img src={dinnerImg} alt="" width="100%" height={50} />
        </Box>
        <Typography
          component="span"
          sx={{ fontSize: "12px", color: "rgba(80, 80, 80, 1)" }}
        >
          {dinner}
        </Typography>
      </Card>
      <Card
        orientation="horizontal"
        variant="outlined"
        sx={{
          width: "100%",
          minHeight: 100,
          padding: "15px",
          display: "flex",
          flexDirection: "column",
          margin: "10px 0",
          opacity: gottenDaysIndex.includes(i) ? "1" : "0.2",
        }}
      >
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "10px 0",
          }}
        >
          <Typography component="h4">Snack3</Typography>
          <img src={Union} alt="" width={5} height={15} />
        </Box>
        <Box sx={{ borderRadius: "20px" }}>
          <img src={snack3Img} alt="" width="100%" height={50} />
        </Box>
        <Typography
          component="span"
          sx={{ fontSize: "12px", color: "rgba(80, 80, 80, 1)" }}
        >
          {snack3}
        </Typography>
      </Card>
    </Box>
  );
};

export default Multiplefood;
