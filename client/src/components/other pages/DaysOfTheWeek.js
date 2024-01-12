import { Box, Divider, Typography, styled } from "@mui/material"
import React from "react"

const Dividerstyle = styled(Divider)({
  "MuiDivider-root:nth-of-type(7)": {
    display: "none"
  }
})

const DaysOfTheWeek = ({ day }) => {
  return (
    <Box
      sx={{
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        width: "100%"
      }}
    >
      <Typography
        sx={{
          color: "rgba(255, 255, 255, 1)",
          margin: "0 auto"
        }}
      >
        {day}
      </Typography>
      <Dividerstyle
        orientation="vertical"
        sx={{
          width: "2px",
          height: "40px",
          backgroundColor: "rgba(255, 255, 255, 1)"
        }}
      />
    </Box>
  )
}

export default DaysOfTheWeek
