import React from "react";
import SideNav from "./SideNav";
import { Box } from "@mui/material";
import Header from "./Header";

export const Layout = ({ children }) => {
  return (
    <Box sx={styles.mainContianer}>
      <SideNav />
      <Box sx={styles.headAndMain}>
        <Header />
        {children}
      </Box>
    </Box>
  );
};

/** @type {import("@mui/material").SxProps} */
const styles = {
  mainContianer: {
    width: "100%",
    maxWidth: "100%",
    display: "flex",
    flexDirection: {
      xs: "column",
      md: "row",
    },
  },
  headAndMain: {
    display: "flex",
    flexDirection: "column",
    width: "100%",
  },
};
