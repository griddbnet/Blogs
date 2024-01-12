import { Box, Typography } from "@mui/material"
import React from "react"

const Weeks = ({ color, diet, number }) => {
  return (
    <Box
      className="weeks"
      sx={{ margin: "0 30px", display: "flex", alignItems: "center" }}
    >
      <img src={color} alt="" height={15} />
      <Box
        sx={{
          width: "150px",
          display: "flex",
          justifyContent: "space-between",
          paddingLeft: "10px"
        }}
      >
        <Typography component="span" sx={{}}>
          {diet}
        </Typography>
        <Typography component="span" sx={{ color: "rgba(177, 177, 177, 1)" }}>
          {number}
        </Typography>
      </Box>
    </Box>
  )
}

export default Weeks
