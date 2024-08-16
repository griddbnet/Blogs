const { mapItemValidation } = require("../utils/validation");
const {
  initGridDbTS,
  insert,
  queryByID,
  queryAll,
  deleteByID,
  editByID,
} = require("../config/db");
const { responseHandler } = require("../utils/responseHandler");

const addMealItem = async (req, res) => {
  //validate req.body

  try {
    const { collectionDb, store, conInfo } = await initGridDbTS();

    const { details } = await mapItemValidation(req.body);
    if (details) {
      let allErrors = details.map((detail) => detail.message.replace(/"/g, ""));
      return responseHandler(res, allErrors, 400, false, "");
    }

    const { id, source, target, x, y, label, lineId } = req.body;

    const data = [id, source, target, x, y, label, lineId];

    const saveStatus = await insert(data, collectionDb);

    if (saveStatus.status) {
      const result = await queryByID(id, conInfo, store);
      let returnData = {
        id: result[0],
        source: result[1],
        target: result[2],
        x: result[3],
        y: result[4],
        label: result[5],
        lineId: result[6],
      };

      return responseHandler(
        res,
        "Map Item saved successfully",
        201,
        true,
        returnData
      );
    }

    return responseHandler(
      res,
      "Unable to save map item",
      400,
      false,
      saveStatus.error
    );
  } catch (error) {
    responseHandler(res, "Error saving map item", 400, false, error.message);
  }
};

const mapItemDetails = async (req, res) => {
  try {
    const { store, conInfo } = await initGridDbTS();
    const { id } = req.params;

    const result = await queryByID(id, conInfo, store);

    let returnData = {
      id: result[0],
      source: result[1],
      target: result[2],
      x: result[3],
      y: result[4],
      label: result[5],
      lineId: result[6],
    };

    return result
      ? responseHandler(res, "map item detail found", 200, true, returnData)
      : responseHandler(res, "No map item detail found", 400, false, "");
  } catch (error) {
    responseHandler(res, "Error saving map item", 400, false, error.message);
  }
};

const editMapItem = async (req, res) => {
  try {
    const { store, conInfo } = await initGridDbTS();
    const { id } = req.params;

    const result = await queryByID(id, conInfo, store);

    if (!result) {
      return responseHandler(res, "incorrect map item ID", 400, false, "");
    }

    const { source, target, x, y, label, lineId } = req.body;

    const data = [id, source, target, x, y, label, lineId];

    const check = await editByID(store, conInfo, data);

    if (check[0]) {
      const result2 = await queryByID(id, conInfo, store);

      let returnData = {
        id: result2[0],
        source: result2[1],
        target: result2[2],
        x: result2[3],
        y: result2[4],
        label: result2[5],
        lineId: result2[6],
      };

      return responseHandler(
        res,
        "map item edited successfully",
        200,
        true,
        returnData
      );
    }
    return responseHandler(res, "Error editing map item ", 400, false, "");
  } catch (error) {
    responseHandler(res, "Error saving map item", 400, false, error.message);
  }
};

const deleteMapItem = async (req, res) => {
  try {
    const { store, conInfo } = await initGridDbTS();
    const { id } = req.params;

    const result = await deleteByID(store, id, conInfo);

    return result[0]
      ? responseHandler(res, "map item deleted successfully", 200, true, "")
      : responseHandler(res, "Error deleting map item", 400, false, "");
  } catch (error) {
    responseHandler(res, "Error saving map item", 400, false, error.message);
  }
};

const getAllMapItems = async (req, res) => {
  try {
    const { store, conInfo } = await initGridDbTS();
    const result = await queryAll(conInfo, store);
    let data = [];

    result.results.forEach((result) => {
      let returnData = {
        id: result[0],
        source: result[1],
        target: result[2],
        x: result[3],
        y: result[4],
        label: result[5],
        lineId: result[6],
      };
      data.push(returnData);

      return result;
    });

    return responseHandler(
      res,
      "all map items in the database successfully retrieved",
      200,
      true,
      data
    );
  } catch (error) {
    console.log(error);
    return responseHandler(
      res,
      "Unable to retrieve meal plans",
      400,
      false,
      ""
    );
  }
};

module.exports = {
  addMealItem,
  mapItemDetails,
  editMapItem,
  deleteMapItem,
  getAllMapItems,
};
