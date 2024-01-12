import { configureStore } from "@reduxjs/toolkit";
import plans from "./features/plan/planSlice";

let store = configureStore({
  reducer: {
    plans,
  },
});

export default store;
