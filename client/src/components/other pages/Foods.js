import React from "react"
import Union from "../assets/images/Union.png"
import { Box, Card, Typography } from "@mui/material"

const Foods = ({ heading, image, name }) => {
  return (
    <Card
      orientation="horizontal"
      variant="plain"
      sx={{ minWidth: 200, flexGrow: 1, margin: "15px", minHeight: 100 }}
    >
      <Box
        sx={{
          display: {
            xs: "flex",
            justifyContent: "space-between",
            alignItems: "center"
          },
          padding: "20px 0"
        }}
      >
        <Typography component="h4">{heading}</Typography>
        <img src={Union} alt="" width={5} height={15} />
      </Box>
      <Box>
        <img src={image} alt="" width={200} height={70} />
      </Box>
      <Typography
        component="span"
        sx={{ fontSize: "15px", color: "rgba(80, 80, 80, 1)" }}
      >
        {name}
      </Typography>
    </Card>
  )
}

export default Foods
