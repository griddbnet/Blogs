import { createEntityAdapter, createSelector } from "@reduxjs/toolkit";
import { apiSlice } from "../../api/apiSlice";

const mapItemAdapter = createEntityAdapter({});

const initialState = mapItemAdapter.getInitialState();

export const mapItemApiSlice = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    getAllMapItems: builder.query({
      query: () => ({
        url: "all-map-items",
        method: "GET",
        validateStatus: (response, result) => {
          return response.status === 200 && !result.isError;
        },
      }),
      transformResponse: (responseData) => {
        return mapItemAdapter.setAll(initialState, responseData.data);
      },
      providesTags: (result, error, arg) => {
        if (result?.ids) {
          return [
            { type: "mapItems", id: "LIST" },
            ...result.ids.map((id) => ({ type: "mapItems", id })),
          ];
        } else return [{ type: "mapItems", id: "LIST" }];
      },
    }),
    addMapItem: builder.mutation({
      query: (credentials) => ({
        url: "add-map-item",
        method: "POST",
        body: { ...credentials },
        validateStatus: (response, result) => {
          return response.status === 201 && !result.isError;
        },
      }),
      invalidatesTags: [{ type: "mapItems", id: "LIST" }],
    }),
    deleteMapItem: builder.mutation({
      query: ({ id }) => ({
        url: `delete-map-item/${id}`,
        method: "DELETE",
        validateStatus: (response, result) => {
          return response.status === 200 && !result.isError;
        },
      }),
      invalidatesTags: (result, error, arg) => [
        { type: "mapItems", id: arg.id },
      ],
    }),
    editMapItem: builder.mutation({
      query: (data) => ({
        url: `edit-map-item/${data.id}`,
        method: "PUT",
        body: {
          ...data,
        },
        validateStatus: (response, result) => {
          return response.status === 200 && !result.isError;
        },
      }),
      invalidatesTags: (result, error, arg) => [
        { type: "mapItems", id: arg.id },
      ],
    }),
  }),
});

export const {
  useGetAllMapItemsQuery,
  useAddMapItemMutation,
  useDeleteMapItemMutation,
  useEditMapItemMutation,
} = mapItemApiSlice;

export const selectMapItemsResult =
  mapItemApiSlice.endpoints.getAllMapItems.select();

const selectMapItemsData = createSelector(
  selectMapItemsResult,
  (MapItemsResult) => MapItemsResult.data
);

export const {
  selectAll: selectAllMapItemss,
  selectById: selectMapItemById,
  selectIds: selecttMapItemIds,
} = mapItemAdapter.getSelectors(
  (state) => selectMapItemsData(state) ?? initialState
);
