import React, { useState } from "react";
import { Box } from "@mui/material";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import { Details } from "./data";
import FormDetails from "./FormDetails";
import { useForm } from "react-hook-form";
import {
  useAddMapItemMutation,
  useGetAllMapItemsQuery,
} from "../store/Features/mapItem/mapItemApiSlice";
import { toast } from "react-toastify";
import uniqid from "uniqid";

const FormInput = () => {
  const [addMapItem] = useAddMapItemMutation();

  const {
    register,
    reset,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const { data: mapItems } = useGetAllMapItemsQuery("mapItems", {
    pollingInterval: 60000,
    refetchOnFocus: true,
    refetchOnMountOrArgChange: true,
  });

  let items = [];

  if (mapItems) {
    const { entities } = mapItems;
    items = Object.values(entities);
  }

  const onSubmit = async (data) => {
    try {
      console.log({ data });
      let id = uniqid.process();

      const { message } = await addMapItem({
        id,
        source: items.length > 0 ? data.Source : uniqid.process("1-"),
        target: id,
        x: Number(data.PositionX),
        y: Number(data.PositionY),
        label: data.Label,
        lineId: uniqid.process("el-"),
      }).unwrap();
      reset();

      toast.success(`${message}`, {
        position: "top-right",
        autoClose: 5000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
      });

      // setTimeout(() => {
      //   window.location.reload();
      // }, 5000);
    } catch (error) {
      let msg =
        error.message ||
        (error.data && error.data.message) ||
        "An error occurred";
      toast.error(`${msg}`, {
        position: "top-right",
        autoClose: 5000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
      });
    }
  };
  return (
    <Box
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      sx={{
        position: "absolute",
        right: "0px",
        background: "white",
        height: { xs: "400px" },
        width: { xs: "40%", md: "20%" },
      }}
    >
      <Typography variant="h6" sx={{ color: "#009688" }}>
        Enter a new idea
      </Typography>
      <Box
        component="form"
        display="flex"
        flexDirection="column"
        alignItems="center"
        sx={{ width: "100%" }}
        onSubmit={handleSubmit(onSubmit)}
        noValidate
      >
        {Details.map((item, i) => (
          <FormDetails
            key={i}
            {...item}
            register={register}
            errors={errors}
            errorParams={item.errorParams}
          />
        ))}
        <Button
          type="submit"
          variant="contained"
          sx={{
            width: "80%",
            py: "6px",
            mt: "15px",
            background: "#009688",
            textTransform: "capitalize",
          }}
        >
          Add
        </Button>
      </Box>
    </Box>
  );
};

export default FormInput;
