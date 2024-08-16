import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

const baseQuery = fetchBaseQuery({
  baseUrl: "http://localhost:4000/api/",
});

export const apiSlice = createApi({
  baseQuery,
  tagTypes: ["mapItems"],
  endpoints: (builder) => ({}),
});
