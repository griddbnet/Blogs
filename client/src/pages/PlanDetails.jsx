import React, { useEffect } from "react";
import PlanDetailSubnav from "../components/other pages/PlanDetailSubnav";
import PlanDetailMain from "../components/other pages/PlanDetailMain";
import PlanDetailDesktop from "../components/other pages/PlanDetailDesktop";
import { Box } from "@mui/material";
import { useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { getAmealPlan } from "../store/features/plan/planSlice";

const PlanDetails = () => {
  let { id } = useParams();
  const dispatch = useDispatch();

  const { singlePlan } = useSelector((state) => state.plans);

  const title = singlePlan[1];

  useEffect(() => {
    dispatch(getAmealPlan(id));
  }, [dispatch, id]);

  return (
    <Box component="main">
      <Box
        sx={{
          display: "flex",
          justifyContent: "flex-end",
          backgroundColor: "rgba(244, 244, 244, 1)",
        }}
      >
        <PlanDetailSubnav id={id} title={title} item={singlePlan} />
      </Box>
      <PlanDetailMain />
      <PlanDetailDesktop singlePlan={singlePlan} />
    </Box>
  );
};

export default PlanDetails;
