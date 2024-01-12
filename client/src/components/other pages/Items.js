import { Box, Divider, Typography } from "@mui/material";
import React, { useState } from "react";
import { Link } from "react-router-dom";
import mealImage from "../assets/images/mealimage.svg";
import deleteImg from "../assets/images/deleteImg.png";
import editImg from "../assets/images/editImg.png";
import { useDispatch } from "react-redux";
import { deleteAMealPlan } from "../../store/features/plan/planSlice";
import EditPopUp from "./EditPopUp";

const Items = ({ item }) => {
  const [id, title] = item;

  const dispatch = useDispatch();
  const [openEdit, setOpenEdit] = useState(false);

  const handleDelete = () => {
    dispatch(deleteAMealPlan(id));
  };

  return (
    <Box
      sx={{
        display: "flex",
        alignItems: "center",
        padding: { xs: "8px 30px", md: " 15px" },
        width: { md: "40%" },
        borderBottom: { md: 1 },
        borderColor: { md: "rgba(236, 236, 236, 1)" },
      }}
    >
      <Link to={`plan/detail/${id}`}>
        <img src={mealImage} width={60} alt={title} />
      </Link>

      <Box
        sx={{
          width: "100%",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <Link to={`plan/detail/${id}`}>
          <Box
            sx={{
              display: "flex",
              flexDirection: "column",
              paddingLeft: "10px",
            }}
          >
            <Typography
              variant="p"
              sx={{
                fontSize: { xs: "18px", md: "14px" },
                color: "rgba(0, 0, 0, 1)",
                fontWeight: "400",
              }}
            >
              {title}
            </Typography>
          </Box>
        </Link>

        <Box sx={{ display: "flex", alignItems: "center" }}>
          <Box
            sx={{
              paddingRight: { xs: "20px", md: "0" },
              display: "flex",
              alignItems: "center",
              cursor: "pointer",
            }}
            onClick={() => setOpenEdit(true)}
          >
            <img src={editImg} alt="" width={10} height={10} />
            <Typography
              sx={{
                display: { xs: "none", md: "block" },
                color: "rgba(149, 149, 149, 1)",
                fontWeight: "400",
                fontSize: "12px",
                paddingLeft: "3px",
                paddingTop: { md: "5px" },
              }}
            >
              Edit
            </Typography>
          </Box>

          <Divider
            orientation="vertical"
            sx={{
              width: "2px",
              height: "15px",
              backgroundColor: "rgba(236, 236, 236, 1)",
              border: "none",
              margin: "0 15px",
              display: { xs: "none", md: "block" },
            }}
          />

          <Box
            sx={{ display: "flex", alignItems: "center", cursor: "pointer" }}
            onClick={handleDelete}
          >
            <img src={deleteImg} alt="" />
            <Typography
              sx={{
                display: { xs: "none", md: "block" },
                color: "rgba(149, 149, 149, 1)",
                fontWeight: "400",
                fontSize: "12px",
                paddingLeft: "3px",
                paddingTop: { md: "5px" },
              }}
            >
              Delete
            </Typography>
          </Box>
        </Box>
      </Box>
      {openEdit && (
        <EditPopUp setOpenEdit={setOpenEdit} openEdit={openEdit} item={item} />
      )}
    </Box>
  );
};

export default Items;
