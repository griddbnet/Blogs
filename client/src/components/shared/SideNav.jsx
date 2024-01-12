import {
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  styled,
  Typography,
} from "@mui/material";
import home from "../assets/images/home.svg";
import React from "react";
import { Link } from "react-router-dom";

const Typomenu = styled(Typography)(({ theme }) => ({
  fontSize: "1rem",
  fontWeight: "300",
  [theme.breakpoints.down("md")]: {
    display: "none",
  },
}));

const Listul = styled(List)(({ theme }) => ({ theme }) => ({
  [theme.breakpoints.down("md")]: {
    display: "none",
  },
}));

const Cookstyle = styled("div")(({ theme }) => ({
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  backgroundColor: "rgba(12, 19, 37, 1)",
  color: "rgba(255, 255, 255, 1)",
  minwidth: "100%",
  height: "150px",
  fontSize: "1.5rem",

  a: {
    textDecoration: "none",
  },

  [theme.breakpoints.down("md")]: {
    height: "100px",
    fontSize: "1rem",
  },
}));

const SideNav = () => {
  return (
    <Box
      component="aside"
      sx={{
        height: { xs: "none", md: "100vh" },
        borderRight: 2,
        borderColor: "rgba(228, 227, 231, 1)",
        width: { xs: "100%", md: "15%" },
      }}
    >
      <Cookstyle>
        <Link to="/">
          <Typography variant="h4">cookplan</Typography>
        </Link>
      </Cookstyle>
      <Typomenu
        variant="h6"
        sx={{
          color: "rgba(88, 100, 116, 1)",
          pt: "30px",
          pl: "30px",
          pb: "10px",
        }}
      >
        MENU
      </Typomenu>
      <Listul
        sx={{
          backgroundColor: "rgba(236, 248, 242, 1)",
          borderRight: 3,
          borderColor: "rgba(129, 197, 36, 1)",
        }}
      >
        <ListItem disablePadding>
          <ListItemButton sx={{ padding: "0", pl: "30px" }} href="#home">
            <ListItemIcon sx={{ minWidth: "0" }}>
              <img src={home} alt="home" />
            </ListItemIcon>
            <ListItemText
              primary="Home"
              sx={{
                pl: "20px",
                color: "rgba(129, 197, 36, 1)",
                fontWeight: "700",
              }}
            />
          </ListItemButton>
        </ListItem>
      </Listul>
    </Box>
  );
};

export default SideNav;
