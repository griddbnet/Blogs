import {
  Autocomplete,
  Box,
  Button,
  FormControl,
  FormLabel,
  InputAdornment,
  TextField,
  Typography,
  styled,
} from "@mui/material";
import { Days } from "../assets/data";
import Paper from "@mui/material/Paper";
import ClearIcon from "@mui/icons-material/Clear";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { createAMealPlan } from "../../store/features/plan/planSlice";
import { useNavigate } from "react-router-dom";
import Validation from "./Validation";

const Sectionstyle = styled("form")(({ theme }) => ({
  margin: "0 auto",
  width: "100%",
  [theme.breakpoints.up("md")]: {
    display: "flex",
    justifyContent: "space-evenly",
    margin: "unset",
    padding: "40px 0",
    paddingLeft: "80px",
  },
}));

const Divstyle = styled("div")(({ theme }) => ({
  [theme.breakpoints.up("md")]: {
    width: "50%",
  },
}));

const Addplansectn = () => {
  const [selectedValues, setSelectedValues] = useState([]);
  const [errors, setErrors] = useState({});

  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.currentTarget));
    let newData = {
      title: data.title,
      breakfast: data.breakfast,
      cabs: Number(data.cabs),
      calories: Number(data.calories),
      days: selectedValues,
      dinner: data.dinner,
      fat: Number(data.fat),
      lunch: data.lunch,
      protein: Number(data.protein),
      snack1: data.snack1,
      snack2: data.snack2,
      snack3: data.snack3,
    };

    setErrors(Validation(newData));

    if (Object.getOwnPropertyNames(Validation(newData)).length === 0) {
      dispatch(createAMealPlan(newData));
    }
  };

  const handleAutocompleteChange = (event, newValue) => {
    setSelectedValues(newValue);
  };

  const { isSuccess } = useSelector((state) => state.plans);

  if (isSuccess) {
    setTimeout(() => {
      navigate("/");
    }, 3000);
  }

  return (
    <Sectionstyle onSubmit={handleSubmit}>
      <Divstyle>
        <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
          <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Title</FormLabel>
          <TextField
            placeholder="Enter Title"
            sx={{ width: { xs: "100%", md: "90%" }, borderRadius: "7px" }}
            name="title"
          />
          {errors.title && (
            <Typography component="p" sx={{ color: "red" }}>
              {errors.title}
            </Typography>
          )}
        </FormControl>

        <Box sx={{ margin: "30px 0" }}>
          <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Days</FormLabel>
          <Autocomplete
            multiple
            defaultValue={[Days[0], Days[1]]}
            disablePortal
            ChipProps={{
              deleteIcon: <ClearIcon />,
            }}
            id="combo-box-demo"
            options={Days}
            sx={{
              width: { xs: "100%", md: "90%" },
              color: " rgba(127, 197, 32, 1)",
              fieldset: {
                border: "1px solid rgba(214, 214, 214, 1)",
                borderRadius: "7px",
              },
              "& .MuiOutlinedInput-root": {
                padding: "20px",
              },
              "& .MuiAutocomplete-tag": {
                backgroundColor: "rgba(129, 197, 37, 1)",
                borderRadius: "7px",
              },
              "& .MuiChip-label": {
                color: "rgba(255, 255, 255, 1)",
              },
              "& .MuiChip-deleteIcon": {
                color: "rgba(255, 255, 255, 1)",
              },
            }}
            onChange={handleAutocompleteChange}
            renderInput={(params) => (
              <TextField name="days" {...params} label="" />
            )}
            PaperComponent={(props) => (
              <Paper
                sx={{
                  background: "rgba(129, 197, 37, 1)",
                  color: "rgba(255, 255, 255, 1)",
                  fontSize: "25px",
                  "&:hover": {
                    border: "1px solid #00FF00",
                    color: "gray",
                    backgroundColor: "white",
                  },
                }}
                {...props}
              />
            )}
            value={selectedValues}
          />
          {errors.days && (
            <Typography component="p" sx={{ color: "red" }}>
              {errors.days}
            </Typography>
          )}
        </Box>

        <Typography variant="h6" sx={{ fontWeight: "300" }}>
          Meals
        </Typography>

        <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
          <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Breakfast</FormLabel>
          <TextField
            placeholder="Enter Breakfast"
            sx={{ width: { xs: "100%", md: "90%" }, borderRadius: "7px" }}
            name="breakfast"
          />
          {errors.breakfast && (
            <Typography component="p" sx={{ color: "red" }}>
              {errors.breakfast}
            </Typography>
          )}
        </FormControl>

        <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
          <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Snack 1</FormLabel>
          <TextField
            placeholder="Enter Snack 1"
            sx={{ width: { xs: "100%", md: "90%" }, borderRadius: "7px" }}
            name="snack1"
          />
          {errors.snack1 && (
            <Typography component="p" sx={{ color: "red" }}>
              {errors.snack1}
            </Typography>
          )}
        </FormControl>

        <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
          <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Lunch</FormLabel>
          <TextField
            placeholder="Enter Lunch"
            sx={{ width: { xs: "100%", md: "90%" }, borderRadius: "7px" }}
            name="lunch"
          />
          {errors.lunch && (
            <Typography component="p" sx={{ color: "red" }}>
              {errors.lunch}
            </Typography>
          )}
        </FormControl>

        <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
          <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Snack 2</FormLabel>
          <TextField
            placeholder="Enter Snack 2"
            sx={{ width: { xs: "100%", md: "90%" }, borderRadius: "7px" }}
            name="snack2"
          />
          {errors.snack2 && (
            <Typography component="p" sx={{ color: "red" }}>
              {errors.snack2}
            </Typography>
          )}
        </FormControl>

        <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
          <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Dinner</FormLabel>
          <TextField
            placeholder="Enter Dinner"
            sx={{ width: { xs: "100%", md: "90%" }, borderRadius: "7px" }}
            name="dinner"
          />
          {errors.dinner && (
            <Typography component="p" sx={{ color: "red" }}>
              {errors.dinner}
            </Typography>
          )}
        </FormControl>
        <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
          <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Snack 3</FormLabel>
          <TextField
            placeholder="Enter Snack 3"
            sx={{ width: { xs: "100%", md: "90%" }, borderRadius: "7px" }}
            name="snack3"
          />
          {errors.snack3 && (
            <Typography component="p" sx={{ color: "red" }}>
              {errors.snack3}
            </Typography>
          )}
        </FormControl>
      </Divstyle>

      <Divstyle>
        <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
          <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Calories</FormLabel>
          <TextField
            sx={{ width: { xs: "100%", md: "90%" } }}
            type="number"
            name="calories"
          />
          {errors.calories && (
            <Typography component="p" sx={{ color: "red" }}>
              {errors.calories}
            </Typography>
          )}
        </FormControl>

        <Box sx={{ display: { md: "flex" }, width: { md: "93%" } }}>
          <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
            <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Fat</FormLabel>
            <TextField
              type="number"
              InputProps={{
                endAdornment: (
                  <InputAdornment
                    position="start"
                    sx={{
                      color: "rgba(177, 177, 177, 1)",
                      fontWeight: "400",
                      display: { xs: "none", md: "flex", alignItems: "center" },
                    }}
                  >
                    G
                  </InputAdornment>
                ),
              }}
              sx={{ width: { xs: "100%", md: "90%" } }}
              name="fat"
            />
            {errors.fat && (
              <Typography component="p" sx={{ color: "red", fontSize: "10px" }}>
                {errors.fat}
              </Typography>
            )}
          </FormControl>
          <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
            <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Cabs</FormLabel>
            <TextField
              type="number"
              InputProps={{
                endAdornment: (
                  <InputAdornment
                    position="start"
                    sx={{
                      color: "rgba(177, 177, 177, 1)",
                      fontWeight: "400",
                      display: { xs: "none", md: "flex", alignItems: "center" },
                    }}
                  >
                    G
                  </InputAdornment>
                ),
              }}
              sx={{ width: { xs: "100%", md: "90%" } }}
              name="cabs"
            />
            {errors.cabs && (
              <Typography component="p" sx={{ color: "red", fontSize: "10px" }}>
                {errors.cabs}
              </Typography>
            )}
          </FormControl>
          <FormControl sx={{ width: "100%", paddingTop: "30px" }}>
            <FormLabel sx={{ color: "rgba(48, 48, 48, 1)" }}>Protein</FormLabel>
            <TextField
              type="number"
              InputProps={{
                endAdornment: (
                  <InputAdornment
                    position="start"
                    sx={{
                      color: "rgba(177, 177, 177, 1)",
                      fontWeight: "400",
                      display: { xs: "none", md: "flex", alignItems: "center" },
                    }}
                  >
                    G
                  </InputAdornment>
                ),
              }}
              sx={{ width: { xs: "100%", md: "90%" } }}
              name="protein"
            />
            {errors.protein && (
              <Typography component="p" sx={{ color: "red", fontSize: "10px" }}>
                {errors.protein}
              </Typography>
            )}
          </FormControl>
        </Box>
        <Box sx={{ display: { md: "flex", justifyContent: "center" } }}>
          <Button
            sx={{
              backgroundColor: "rgba(127, 197, 32, 1)",
              width: { xs: "100%", md: "60%" },
              margin: "30px 0",
              padding: "20px 0",
              marginRight: "45px",
              borderRadius: "7px",
            }}
            type="submit"
          >
            <Typography
              variant="body2"
              sx={{
                color: "rgba(255, 255, 255, 1)",
                textTransform: "capitalize",
              }}
            >
              Create Plan
            </Typography>
          </Button>
        </Box>
      </Divstyle>
    </Sectionstyle>
  );
};

export default Addplansectn;
