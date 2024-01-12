import React, { useState } from "react";
import { AppBar, Box, Button, Divider, Typography } from "@mui/material";
import ArrowBackIosNewIcon from "@mui/icons-material/ArrowBackIosNew";
import Edit from "../assets/images/editImg.png";
import Delete from "../assets/images/deleteImg.png";
import Share from "../assets/images/shareImg.png";
import EditPopUp from "./EditPopUp";
import { useDispatch } from "react-redux";
import { deleteAMealPlan } from "../../store/features/plan/planSlice";
import { useNavigate } from "react-router-dom";

const PlanDetailSubnav = ({ id, title, item }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const [openEdit, setOpenEdit] = useState(false);

  const handleDelete = () => {
    dispatch(deleteAMealPlan(id));
    navigate("/");
  };

  return (
    <AppBar
      position="static"
      sx={{
        width: { xs: "100%", md: "95%" },
        backgroundColor: "rgba(255, 255, 255, 1)",
        display: "flex",
        flexDirection: "row",
        justifyContent: "space-between",
        padding: "10px 20px",
      }}
    >
      <Box
        component="section"
        variant="h6"
        sx={{
          color: "rgba(73, 73, 73, 1)",
          padding: "20px 0",
          fontWeight: "400",
          display: "flex",
        }}
      >
        <ArrowBackIosNewIcon
          sx={{
            marginRight: "20px",
            display: { md: "none" },
            fontSize: "22px",
          }}
        />
        <Divider
          orientation="vertical"
          sx={{
            width: "5px",
            height: "20px",
            backgroundColor: "rgba(134, 197, 47, 1)",
            border: "none",
            margin: "0 10px",
          }}
        />
        <Box>
          <Typography component="p" textTransform="uppercase">
            {title}
          </Typography>
        </Box>
      </Box>

      <Box
        component="section"
        sx={{
          display: {
            xs: "none",
            md: "flex",
            justifyContent: "center",
            alignItems: "center",
          },
        }}
      >
        <Box sx={{ cursor: "pointer" }} onClick={() => setOpenEdit(true)}>
          <img src={Edit} alt="editImg" width={12} height={12} />
          <Typography
            component="span"
            sx={{
              color: "rgba(149, 149, 149, 1)",
              fontWeight: "300",
              fontSize: "15px",
              marginLeft: "5px",
            }}
          >
            Edit
          </Typography>
        </Box>

        <Box
          sx={{ padding: "0 10px", cursor: "pointer" }}
          onClick={handleDelete}
        >
          <img src={Delete} alt="deleteImg" width={12} height={12} />
          <Typography
            component="span"
            sx={{
              color: "rgba(149, 149, 149, 1)",
              fontWeight: "300",
              fontSize: "15px",
              marginLeft: "5px",
            }}
          >
            Delete
          </Typography>
        </Box>

        <Box>
          <Button
            sx={{
              backgroundColor: "rgba(134, 197, 47, 1)",
              padding: "15px 20px",
              display: "flex",
              alignItems: "center",
            }}
          >
            <img src={Share} alt="shareImg" width={20} height={20} />
            <Typography
              component="span"
              variant="body2"
              sx={{
                color: "rgba(255, 255, 255, 1)",
                textTransform: "capitalize",
                fontWeight: "300",
                fontSize: "18px",
                marginLeft: "5px",
              }}
            >
              Share
            </Typography>
          </Button>
        </Box>
      </Box>

      {openEdit && (
        <EditPopUp setOpenEdit={setOpenEdit} openEdit={openEdit} item={item} />
      )}
    </AppBar>
  );
};

export default PlanDetailSubnav;
