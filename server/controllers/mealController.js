const { mealPlanValidation } = require("../utils/validation");
const {
  initGridDbTS,
  insert,
  queryByID,
  queryAll,
  deleteByID,
  editByID,
} = require("../config/db");
const { responseHandler } = require("../utils/responseHandler");
const { v4: uuidv4 } = require("uuid");

const addMealPlan = async (req, res) => {
  //validate req.body

  const { collectionDb, store, conInfo } = await initGridDbTS();

  const { details } = await mealPlanValidation(req.body);
  if (details) {
    let allErrors = details.map((detail) => detail.message.replace(/"/g, ""));
    return responseHandler(res, allErrors, 400, false, "");
  }

  try {
    const {
      title,
      calories,
      fat,
      cabs,
      protein,
      days,
      breakfast,
      lunch,
      dinner,
      snack1,
      snack2,
      snack3,
    } = req.body;

    const id = uuidv4();

    const data = [
      id,
      title,
      calories,
      fat,
      cabs,
      protein,
      days.join(";"),
      breakfast,
      lunch,
      dinner,
      snack1,
      snack2,
      snack3,
    ];

    const saveStatus = await insert(data, collectionDb);

    if (saveStatus.status) {
      const result = await queryByID(id, conInfo, store);
      return responseHandler(
        res,
        "Meal plan saved successfully",
        201,
        true,
        result
      );
    }

    return responseHandler(
      res,
      "Unable to save meal plan",
      400,
      false,
      saveStatus.error
    );
  } catch (error) {
    responseHandler(res, "Error saving meal plan", 400, false, error);
  }
};

const mealPlanDetails = async (req, res) => {
  const { store, conInfo } = await initGridDbTS();
  const { id } = req.params;

  const result = await queryByID(id, conInfo, store);

  return result
    ? responseHandler(res, "meal plan detail found", 200, true, result)
    : responseHandler(res, "No meal plan found", 400, false, "");
};

const editMealPlan = async (req, res) => {
  const { store, conInfo } = await initGridDbTS();
  const { id } = req.params;

  const result = await queryByID(id, conInfo, store);

  if (!result) {
    return responseHandler(res, "incorrect meal plan ID", 400, false, "");
  }

  const {
    title,
    calories,
    fat,
    cabs,
    protein,
    days,
    breakfast,
    lunch,
    dinner,
    snack1,
    snack2,
    snack3,
  } = req.body;

  const data = [
    id,
    title,
    calories,
    fat,
    cabs,
    protein,
    days.join(";"),
    breakfast,
    lunch,
    dinner,
    snack1,
    snack2,
    snack3,
  ];

  const check = await editByID(store, conInfo, data);

  if (check[0]) {
    const result2 = await queryByID(id, conInfo, store);

    return responseHandler(
      res,
      "meal plan edited successfully",
      200,
      true,
      result2
    );
  }
  return responseHandler(res, "Error editing meal plan", 400, false, "");
};

const deleteMealPlan = async (req, res) => {
  const { store, conInfo } = await initGridDbTS();
  const { id } = req.params;

  const result = await deleteByID(store, id, conInfo);

  return result[0]
    ? responseHandler(res, "meal plan deleted successfully", 200, true, "")
    : responseHandler(res, "Error deleting meal plan", 400, false, "");
};

const getAllMealPlans = async (req, res) => {
  try {
    const { store, conInfo } = await initGridDbTS();
    const result = await queryAll(conInfo, store);
    return responseHandler(
      res,
      "all meal plans in the database successfully retrieved",
      200,
      true,
      result.results
    );
  } catch (error) {
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
  addMealPlan,
  mealPlanDetails,
  editMealPlan,
  deleteMealPlan,
  getAllMealPlans,
};
