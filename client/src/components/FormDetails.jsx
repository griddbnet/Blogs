import React from "react";
import { Typography, TextField, Box } from "@mui/material";
import { useGetAllMapItemsQuery } from "../store/Features/mapItem/mapItemApiSlice";

const FormDetails = ({ name, errors, register, errorParams, inputType }) => {
  const { data } = useGetAllMapItemsQuery("mapItems", {
    pollingInterval: 60000,
    refetchOnFocus: true,
    refetchOnMountOrArgChange: true,
  });

  let items = [];

  if (data) {
    const { entities } = data;
    items = Object.values(entities);
  }

  return (
    <Box
      sx={{
        width: "80%",
        display: "flex",
        flexDirection: "column",
      }}
    >
      <Typography variant="p" sx={{ fontSize: "12px", my: "5px" }}>
        {name}
      </Typography>
      {inputType === "select" ? (
        <select
          sx={{ color: "#aaa", height: 50, mb: "20px" }}
          {...register(name, errorParams)}
          defaultValue=""
          disabled={items.length === 0}
        >
          <option value="" disabled hidden>
            Select a source
          </option>
          {items.map((node) => (
            <option key={node.id} value={node.id}>
              {node.label}
            </option>
          ))}
        </select>
      ) : (
        <TextField
          id={name}
          type="text"
          name={name}
          size="small"
          sx={{
            "& .MuiOutlinedInput-input": {
              color: "#aaa",
              height: 10,
            },
          }}
          {...register(name, errorParams)}
        />
      )}
      {errors && errors[name] && (
        <Typography
          variant="p"
          sx={{ color: "red", fontSize: "10px", mt: "10px" }}
        >
          {errors[name].message || `${name} field is invalid`}
        </Typography>
      )}
    </Box>
  );
};

export default FormDetails;
